# EIP Patterns
1. Message Channel and Endpoint: how we connect an application to the channel. It is used in every single application.

2. Content-Based Router: routes each message to the correct recipient based on message content.

3. Dead-Letter Channel: the messaging system determines that it cannot or should not deliver a message. It is used (in our case and generally) for messages that cannot be processed successfully after a certain amount of tries.

4. Invalid-Message Channel: channel for messages that are malformed or fail validation checks and cannot be processed as expected.

5. Message Translator: used between other filters or applications to translate one data format into another.

6. Wipe Tap: inserts a simple Recipient List into the channel that publishes each incoming message to the main channel and a secondary channel.

7. Normalizer: routes each message type through a custom Message Translator so that the resulting messages match a common format.

8. Content Enricher: access an external data source in order to augment a message with missing information.

9. Multicast: allows a message to be sent to multiple recipients or destinations simultaneously (not in the book, but included in Camel: https://camel.apache.org/components/4.4.x/eips/multicast-eip.html).

# event-market-user-creation: UserCreationProducer
1. Message Channel
```
to("jms:queue:notification.producer.user.creation.queue")
```

2. Point-to-Point Channel
Inherited implemented by this type of queue.

# event-market-creator-creation: CreatorCreationProducer
1. Message Channel
```
to("jms:queue:notification.producer.creator.creation.queue")
```

2. Point-to-Point Channel
Inherited implemented by this type of queue.

# event-market-event-creation: EventCreationProducer
1. Message Channel
```
to("jms:queue:notification.producer.event.creation.queue")
```

2. Point-to-Point Channel
Inherited implemented by this type of queue.

# event-market-notification-engine: NotificationEngineProducer
1. Message Channel
```
from("jms:queue:notification.producer.user.creation.queue")

from("jms:queue:notification.producer.creator.creation.queue")

from("jms:queue:notification.producer.event.creation.queue")

from("jms:queue:notification.producer.ticketpayment.inprocess.queue")

from("jms:queue:notification.producer.ticketpayment.rejected.queue")

from("jms:queue:notification.producer.ticketpayment.approved.queue")

from("jms:queue:notification.producer.recommendations.queue")
```

2. Content-Based Router
```
choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.user.creation.queue")

choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.creator.creation.queue")

choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.event.creation.queue")

choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.ticketpayment.inprocess.queue")

choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.ticketpayment.rejected.queue")

choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.ticketpayment.approved.queue")

choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
    .otherwise()
        .to("jms:queue:notification.builder.recommendations.queue")
```

3. Point-to-Point Channel
Inherited implemented by this type of queue.

4. Content Enricher
```
if (user != null) {
    notificationJson.put("receivesEmails", user.getReceivesEmails());
    notificationJson.put("receivesPushs", user.getReceivesPushs());
} else if (creator != null) {
    notificationJson.put("receivesEmails", true);
    notificationJson.put("receivesPushs", true);
}
```

5. Dead-Letter Channel
```
choice()
    .when(header("validMessage").isEqualTo(false))
        .to("jms:queue:notification.deadletter.queue")
```

# event-market-ticket-purchase: TicketPurchaseProducer
1. Message Channel
```
to("jms:queue:ticketvalidation.deadletter.queue")

to("jms:queue:ticketvalidation.financial.queue")

to("jms:queue:ticketvalidation.completion.queue")

to("jms:queue:notification.producer.ticketpayment.inprocess.queue")

to("jms:queue:recommender.builder.queue")
```

2. Content-Based Router
```
.when(simple("${body[validPrice]} == false"))
    .to("jms:queue:ticketvalidation.deadletter.queue")
.otherwise()
    .multicast().stopOnException()
        .parallelProcessing()
        .to("direct:processTicket", "direct:inProcessNotification", "direct:inProcessRecommender")
    .end()
.end()
```

3. Invalid-Message Channel
```
.choice()
    .when(simple("${body[validPrice]} == false"))
        .log("Sending to ticketvalidation.invalidmessage.queue")
        .to("jms:queue:ticketvalidation.invalidmessage.queue")
```

4. Wire Tap
```
.to("direct:processTicket", "direct:inProcessNotification", "direct:inProcessRecommender")
```

5. Message Translator
```
.marshal().json(JsonLibrary.Jackson)  // Convert Map to 
```

6. Multicast
```
.otherwise()
    .multicast().stopOnException()
        .parallelProcessing()
        .to("direct:processTicket", "direct:inProcessNotification", "direct:inProcessRecommender")
    .end()
.end()
```

# event-market-financial-validation: FinancialValidationProducer
1. Message Channel
```
from("jms:queue:ticketvalidation.financial.queue")
```

2. Content-Based Router
```
choice()
    .when(simple("${body[financiallyValid]} == true"))
        .to("jms:queue:ticketvalidation.completion.queue")
    .otherwise()
        .to("jms:queue:notification.producer.ticketpayment.rejected.queue")
```

# event-market-ticket-validation: TicketValidationProducer
1. Message Channel
```
from("jms:queue:ticketvalidation.completion.queue")
```

2. Point-to-Point Channel
Inherited implemented by this type of queue.

3. Content-Based Router
```
choice()
    .when(simple("${body[message]} == 'ticket-payment-approved'"))
        .to("jms:queue:notification.producer.ticketpayment.approved.queue")
    .otherwise()
        .to("jms:queue:ticketvalidation.deadletter.queue")
```

4. Message Translator
```
.marshal().json(JsonLibrary.Jackson)

.marshal().json(JsonLibrary.Jackson)
```

5. Normalizer
```
.process(exchange -> {
    Object body = exchange.getIn().getBody();
    if (body instanceof String) {
        String jsonBody = (String) body;
        @SuppressWarnings("unchecked")
        Map<String, Object> messageData = objectMapper.readValue(jsonBody, Map.class);
        exchange.getIn().setBody(messageData);
    } else if (body instanceof Map) {
        // No conversion needed, body is already a Map
    } else {
        throw new IllegalArgumentException("Unsupported message format: " + body.getClass());
    }
})
```

6. Dead-Letter Channel
```
.choice()
    .when(simple("${body[message]} == 'ticket-payment-approved'"))
        .to("jms:queue:notification.producer.ticketpayment.approved.queue")
    .otherwise()
        .to("jms:queue:ticketvalidation.deadletter.queue")
```

# event-market-recommendation-engine: RecommendationEngineProducer
1. Message Channel
```
from("jms:queue:recommender.builder.queue")
```

2. Message Translator
```
.unmarshal().json(JsonLibrary.Jackson, Map.class)  // Unmarshal 
```

3. Content Enricher
```
.process(new Processor() {
    @Override
    public void process(Exchange exchange) throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = exchange.getIn().getBody(Map.class);
            int userId = (int) messageData.get("userId");

            // Fetch User from the database
            User user = em.find(User.class, userId);
            if (user == null) {
                throw new Exception("User not found for id: " + userId);
            }

            // Create the recommender model and set EntityManager
            NlpRecommenderModel model = new NlpRecommenderModel(user);
            model.setEntityManager(em);
            
            // Or use PopularityRecommenderModel if needed
            // PopularityRecommenderModel model = new PopularityRecommenderModel(user);
            // model.setEntityManager(em);

            EventRecommender eventRecommender = new EventRecommender(model);
            eventRecommender.requestRecommendation();
            List<Event> recommendations = eventRecommender.sendRecommendation();

            // Extract event IDs from the recommendations
            List<Integer> eventIds = recommendations.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

            // Prepare recommendation message
            Map<String, Object> recommendationMessage = new HashMap<>();
            recommendationMessage.put("eventIds", eventIds);
            recommendationMessage.put("userId", userId);
            recommendationMessage.put("message", "event-recommendation");

            String jsonMessage = objectMapper.writeValueAsString(recommendationMessage);
            exchange.getIn().setBody(jsonMessage);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
})
```