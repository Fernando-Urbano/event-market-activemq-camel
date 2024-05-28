package camelinaction;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import eventmarket.main.FinancialValidator;
import eventmarket.main.TicketRequest;

public class FinancialValidationProducer {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("jms:queue:ticketvalidation.financial.queue")
                    .process(new Processor() {
                        private Random random = new Random();
                        private EntityManagerFactory emf = Persistence.createEntityManagerFactory("EventMarketPU");
                        private EntityManager em = emf.createEntityManager();
                        private ObjectMapper objectMapper = new ObjectMapper();

                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Parse the message body to a Map
                            String bodyString = exchange.getIn().getBody(String.class);
                            @SuppressWarnings("unchecked")
                            Map<String, Object> body = objectMapper.readValue(bodyString, Map.class);

                            FinancialValidator financialValidator = FinancialValidator.getInstance();
                            int ticketRequestId = (int) body.get("ticketRequestId");
                            TicketRequest ticketRequest = em.find(TicketRequest.class, ticketRequestId);
                            boolean financiallyValid = financialValidator.requestValidation(ticketRequest);

                            // Additional random validation step
                            if (financiallyValid) {
                                financiallyValid = random.nextBoolean();
                                if (!financiallyValid) {
                                    financiallyValid = random.nextBoolean();
                                }
                            }

                            body.put("financiallyValid", financiallyValid);
                            exchange.getIn().setBody(body);
                        }
                    })
                    .choice()
                        .when(simple("${body[financiallyValid]} == true"))
                            .log("Ticket is financially valid. Sending to ticketvalidation.completion.queue")
                            .to("jms:queue:ticketvalidation.completion.queue")
                        .otherwise()
                            .log("Ticket is not financially valid. Sending to notification.producer.ticketpayment.rejected.queue")
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> body = exchange.getIn().getBody(Map.class);
                                    Map<String, Object> rejectedMessage = new HashMap<>();
                                    rejectedMessage.put("message", "ticket-payment-rejected");
                                    rejectedMessage.put("userId", body.get("userId"));
                                    rejectedMessage.put("ticketRequestId", body.get("ticketRequestId"));
                                    exchange.getIn().setBody(rejectedMessage);
                                }
                            })
                            .to("jms:queue:notification.producer.ticketpayment.rejected.queue");
            }
        });

        context.start();
        Thread.sleep(30000);

        context.stop();
    }
}
