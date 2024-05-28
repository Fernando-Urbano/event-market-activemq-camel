package camelinaction;

import eventmarket.main.User;
import eventmarket.main.EventRecommender;
import eventmarket.main.NlpRecommenderModel;
import eventmarket.main.PopularityRecommenderModel;
import eventmarket.main.Event;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.jms.ConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class RecommendationEngineProducer {
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("EventMarketPU");
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        // Configure JMS component
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        // Add routes
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("jms:queue:recommender.builder.queue")
                    .log("Received message: ${body}")
                    .unmarshal().json(JsonLibrary.Jackson, Map.class)  // Unmarshal JSON to Map
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            EntityManager em = emf.createEntityManager();
                            em.getTransaction().begin(); // Start the transaction

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

                                em.getTransaction().commit(); // Commit the transaction
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
                    .log("Sending recommendations to notification.producer.recommendations queue and user.recommendations topic")
                    .to("jms:queue:notification.producer.recommendations.queue")
                    .to("jms:topic:user.recommendations");
            }
        });

        // Start the Camel context
        context.start();
        Thread.sleep(10000);

        // Stop the Camel context
        context.stop();
    }
}
