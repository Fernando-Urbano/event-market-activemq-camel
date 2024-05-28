package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventRecommenderTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private User user;
    private Event event1, event2, event3, event4;
    private Ticket ticket;
    private TicketRequest ticketRequest;
    private NlpRecommenderModel model;
    private EventRecommender eventRecommender;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
        em.getTransaction().begin();

        user = new User("Test User", "testuser@example.com", "password", CreateDate.createDate(1990, 1, 1));
        em.persist(user);

        Creator creator = new Creator("Creator Name", "creator@example.com", "securePassword");
        em.persist(creator);

        Location location1 = new Location("Address 1", "City 1", "12345", "State 1", "Country 1");
        em.persist(location1);

        Location location2 = new Location("Address 2", "City 2", "67890", "State 2", "Country 2");
        em.persist(location2);

        Location location3 = new Location("Address 3", "City 3", "54321", "State 3", "Country 3");
        em.persist(location3);

        Location location4 = new Location("Address 4", "City 4", "98765", "State 4", "Country 4");
        em.persist(location4);

        event1 = new Event("Event 1", location1, CreateDate.createDate(2023, 1, 1), CreateDate.createDate(2023, 1, 2), "Description 1", "Tagline 1");
        event1.setCreator(creator);
        em.persist(event1);

        event2 = new Event("Event 2", location2, CreateDate.createDate(2023, 2, 1), CreateDate.createDate(2023, 2, 2), "Description 2", "Tagline 2");
        event2.setCreator(creator);
        em.persist(event2);

        event3 = new Event("Event 3", location3, CreateDate.createDate(2023, 3, 1), CreateDate.createDate(2023, 3, 2), "Description 3", "Tagline 3");
        event3.setCreator(creator);
        em.persist(event3);

        event4 = new Event("Event 4", location4, CreateDate.createDate(2023, 4, 1), CreateDate.createDate(2023, 4, 2), "Description 4", "Tagline 4");
        event4.setCreator(creator);
        em.persist(event4);

        TicketType ticketType1 = new TicketType("VIP", "VIP access", 100, 18, 200.0, event1);
        em.persist(ticketType1);

        TicketType ticketType2 = new TicketType("General", "General access", 100, 18, 100.0, event2);
        em.persist(ticketType2);

        Recipient recipient = new Recipient("Recipient Name", "recipient@example.com", CreateDate.createDate(1990, 1, 1));
        em.persist(recipient);

        FinancialInformation financialInformation = new FinancialInformation(new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123"));
        financialInformation.setUser(user);
        user.addFinancialInformation(financialInformation);
        em.persist(financialInformation);

        ticketRequest = new TicketRequest(recipient, financialInformation, ticketType1);
        ticketRequest.setUser(user);
        user.saveTicketRequest(ticketRequest);
        em.persist(ticketRequest);

        ticket = new Ticket(ticketRequest);
        ticket.setUser(user);
        user.receiveTicket(ticket);
        em.persist(ticket);

        em.getTransaction().commit();

        model = new NlpRecommenderModel(user);
        model.setEntityManager(em);
        eventRecommender = new EventRecommender(model);
    }

    @Test
    void testRequestRecommendation() {
        eventRecommender.requestRecommendation();
        List<Event> recommendedEvents = eventRecommender.sendRecommendation();
        assertEquals(3, recommendedEvents.size());
        assertFalse(recommendedEvents.contains(event1));
    }

    @Test
    void testRequestRecommendationWithCustomCount() {
        eventRecommender.setNumberRecommendations(2);
        eventRecommender.requestRecommendation();
        List<Event> recommendedEvents = eventRecommender.sendRecommendation();
        assertEquals(2, recommendedEvents.size());
        assertFalse(recommendedEvents.contains(event1));
    }

    @AfterEach
    void tearDown() {
        if (em != null) {
            em.getTransaction().begin();

            if (ticket != null && em.contains(ticket)) {
                em.remove(ticket);
            }

            if (ticketRequest != null && em.contains(ticketRequest)) {
                em.remove(ticketRequest);
            }

            if (user != null && em.contains(user)) {
                for (FinancialInformation financialInformation : user.getFinancialInformation()) {
                    if (financialInformation != null && em.contains(financialInformation)) {
                        PaymentMethod paymentMethod = financialInformation.getPaymentMethod();
                        if (paymentMethod != null && em.contains(paymentMethod)) {
                            em.remove(paymentMethod);
                        }
                        em.remove(financialInformation);
                    }
                }
                em.remove(user);
            }
            if (event1 != null && em.contains(event1)) {
                em.remove(event1);
            }
            if (event2 != null && em.contains(event2)) {
                em.remove(event2);
            }
            if (event3 != null && em.contains(event3)) {
                em.remove(event3);
            }
            if (event4 != null && em.contains(event4)) {
                em.remove(event4);
            }
            em.getTransaction().commit();
            em.close();
        }

        if (emf != null) {
            emf.close();
        }
    }
}
