package camelinaction;

import eventmarket.main.NotificationEngine;
import eventmarket.main.User;
import eventmarket.main.Creator;
import eventmarket.main.TicketRequest;
import eventmarket.main.Event;

import javax.jms.ConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationEngineProducer {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // Initialize JPA
        emf = Persistence.createEntityManagerFactory("EventMarketPU");
        em = emf.createEntityManager();

        Processor processor = new Processor() {
            public void process(Exchange exchange) throws Exception {
                String jsonMessage = exchange.getIn().getBody(String.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> messageData = objectMapper.readValue(jsonMessage, Map.class);
                String messageType = (String) messageData.get("message");

                NotificationEngine notificationEngine = new NotificationEngine();
                User user = null;
                Creator creator = null;
                boolean isValidMessage = true;

                switch (messageType) {
                    case "user-creation":
                        int userId = (int) messageData.get("userId");
                        user = em.find(User.class, userId);
                        if (user != null) {
                            notificationEngine.userCreated(user);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "creator-creation":
                        int creatorId = (int) messageData.get("creatorId");
                        creator = em.find(Creator.class, creatorId);
                        if (creator != null) {
                            notificationEngine.creatorCreated(creator);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "ticket-payment-in-process":
                        userId = (int) messageData.get("userId");
                        user = em.find(User.class, userId);
                        if (user != null) {
                            int ticketRequestId = (int) messageData.get("ticketRequestId");
                            TicketRequest ticketRequest = em.find(TicketRequest.class, ticketRequestId);
                            notificationEngine.ticketPaymentInProcess(user, ticketRequest);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "ticket-payment-approved":
                        userId = (int) messageData.get("userId");
                        user = em.find(User.class, userId);
                        if (user != null) {
                            int ticketRequestId = (int) messageData.get("ticketRequestId");
                            TicketRequest ticketRequest = em.find(TicketRequest.class, ticketRequestId);
                            notificationEngine.ticketPaymentApproved(user, ticketRequest);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "ticket-payment-rejected":
                        userId = (int) messageData.get("userId");
                        user = em.find(User.class, userId);
                        if (user != null) {
                            int ticketRequestId = (int) messageData.get("ticketRequestId");
                            TicketRequest ticketRequest = em.find(TicketRequest.class, ticketRequestId);
                            notificationEngine.ticketPaymentRejected(user, ticketRequest);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "event-recommendation":
                        userId = (int) messageData.get("userId");
                        user = em.find(User.class, userId);
                        if (user != null) {
                            @SuppressWarnings("unchecked")
                            List<Integer> eventIds = (List<Integer>) messageData.get("eventIds");
                            List<Event> events = eventIds.stream().map(id -> em.find(Event.class, id)).collect(Collectors.toList());
                            notificationEngine.eventRecommendation(user, events);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "creator-update-event":
                        creatorId = (int) messageData.get("creatorId");
                        creator = em.find(Creator.class, creatorId);
                        if (creator != null) {
                            int eventId = (int) messageData.get("eventId");
                            Event event = em.find(Event.class, eventId);
                            notificationEngine.creatorEventUpdate(creator, event);
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    case "event-creation":
                        creatorId = (int) messageData.get("creatorId");
                        creator = em.find(Creator.class, creatorId);
                        if (creator != null) {
                            int eventId = (int) messageData.get("eventId");
                            Event event = em.find(Event.class, eventId);
                            if (event != null) {
                                notificationEngine.eventCreated(creator, event);
                            } else {
                                isValidMessage = false;
                            }
                        } else {
                            isValidMessage = false;
                        }
                        break;
                    default:
                        isValidMessage = false;
                        break;
                }

                if (isValidMessage) {
                    String message = notificationEngine.getMessage();
                    if (message != null) {
                        Map<String, Object> notificationJson = new HashMap<>();
                        notificationJson.put("message", message);
                        notificationJson.put("email", notificationEngine.getRecipient());

                        if (user != null) {
                            notificationJson.put("receivesEmails", user.getReceivesEmails());
                            notificationJson.put("receivesPushs", user.getReceivesPushs());
                        } else if (creator != null) {
                            notificationJson.put("receivesEmails", true);
                            notificationJson.put("receivesPushs", true);
                        }

                        String notificationJsonString = objectMapper.writeValueAsString(notificationJson);
                        exchange.getIn().setHeader("validMessage", true);
                        exchange.getIn().setBody(notificationJsonString);
                    }
                } else {
                    // Route to dead letter queue
                    exchange.getIn().setHeader("validMessage", false);
                    exchange.getIn().setBody(jsonMessage);
                }
            }
        };

        // Create Camel context
        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        // Add routes
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("jms:queue:notification.producer.user.creation.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.user.creation.queue");

                from("jms:queue:notification.producer.creator.creation.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.creator.creation.queue");

                from("jms:queue:notification.producer.event.creation.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.event.creation.queue");

                from("jms:queue:notification.producer.ticketpayment.inprocess.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.ticketpayment.inprocess.queue");

                from("jms:queue:notification.producer.ticketpayment.rejected.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.ticketpayment.rejected.queue");

                from("jms:queue:notification.producer.ticketpayment.approved.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.ticketpayment.approved.queue");

                from("jms:queue:notification.producer.recommendations.queue")
                    .process(processor)
                    .choice()
                        .when(header("validMessage").isEqualTo(false))
                            .to("jms:queue:notification.deadletter.queue")
                        .otherwise()
                            .to("jms:queue:notification.builder.recommendations.queue");
            }
        });

        // Start the context
        context.start();
        Thread.sleep(500000);

        // Stop the context
        context.stop();

        // Close JPA
        em.close();
        emf.close();
    }
}
