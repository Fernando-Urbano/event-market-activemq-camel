package camelinaction;

import eventmarket.main.User;
import eventmarket.main.TicketRequest;
import eventmarket.main.Ticket;

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
import java.util.Map;

public class TicketValidationProducer {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("EventMarketPU");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        // Configure JMS component
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        // Add routes
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("jms:queue:ticketvalidation.completion.queue")
                    .log("Received message: ${body}")
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
                    .process(new TicketValidationProcessor())
                    .choice()
                        .when(simple("${body[message]} == 'ticket-payment-approved'"))
                            .log("Ticket approved. Sending to notification.producer.ticketpayment.approved.queue")
                            .marshal().json(JsonLibrary.Jackson)
                            .to("jms:queue:notification.producer.ticketpayment.approved.queue")
                        .otherwise()
                            .log("Issue in processing. Sending to ticketvalidation.deadletter.queue")
                            .marshal().json(JsonLibrary.Jackson)
                            .to("jms:queue:ticketvalidation.deadletter.queue");
            }
        });

        // Start the Camel context
        context.start();
        Thread.sleep(30000);

        // Stop the Camel context
        context.stop();
    }

    private static class TicketValidationProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            try {
                Map<String, Object> messageData = exchange.getIn().getBody(Map.class);
                int userId = (int) messageData.get("userId");
                int ticketRequestId = (int) messageData.get("ticketRequestId");

                // Fetch TicketRequest from the database
                TicketRequest ticketRequest = em.find(TicketRequest.class, ticketRequestId);
                if (ticketRequest == null) {
                    throw new Exception("TicketRequest not found for id: " + ticketRequestId);
                }

                User user = em.find(User.class, userId);
                User matchingUser = ticketRequest.getUser();

                if (matchingUser == null || matchingUser.getId() != userId) {
                    throw new Exception("TicketRequest does not belong to the user");
                }

                // Transform TicketRequest to Ticket and save it
                ticketRequest.approveRequest();
                Ticket newTicket = new Ticket(ticketRequest);
                em.persist(newTicket);
                user.receiveTicket(newTicket);
                em.getTransaction().commit();

                // Send success message
                messageData.put("message", "ticket-payment-approved");
                exchange.getIn().setBody(messageData);
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                // Send to dead letter queue
                Map<String, Object> messageData = exchange.getIn().getBody(Map.class);
                messageData.put("message", "ticket-payment-rejected");
                exchange.getIn().setBody(messageData);
            } finally {
                em.close();
            }
        }
    }
}