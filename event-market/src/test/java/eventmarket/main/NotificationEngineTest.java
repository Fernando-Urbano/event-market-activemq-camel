package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEngineTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private User user;
    private Creator creator;
    private Event event;
    private TicketRequest ticketRequest;
    private NotificationEngine notificationEngine;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
        em.getTransaction().begin();

        user = new User("Test User", "testuser@example.com", "password", CreateDate.createDate(1990, 1, 1));
        em.persist(user);

        creator = new Creator("Creator Name", "creator@example.com", "securePassword");
        em.persist(creator);

        Location location = new Location("123 Main St", "City", "12345", "State", "Country");
        em.persist(location);

        event = new Event("Event Name", location, CreateDate.createDate(2023, 1, 1, 0, 0, 0), CreateDate.createDate(2023, 1, 2), "Description", "Tagline");
        event.setCreator(creator);
        em.persist(event);

        TicketType ticketType = new TicketType("VIP", "VIP access", 100, 18, 200.0, event);
        em.persist(ticketType);

        Recipient recipient = new Recipient("Recipient Name", "recipient@example.com", CreateDate.createDate(1990, 1, 1));
        em.persist(recipient);

        FinancialInformation financialInformation = new FinancialInformation(new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123"));
        financialInformation.setUser(user);
        user.addFinancialInformation(financialInformation);
        em.persist(financialInformation);

        ticketRequest = new TicketRequest(recipient, financialInformation, ticketType);
        ticketRequest.setUser(user);
        user.saveTicketRequest(ticketRequest);
        em.persist(ticketRequest);

        em.getTransaction().commit();

        notificationEngine = new NotificationEngine();
    }

    @Test
    void testUserCreated() {
        notificationEngine.userCreated(user);
        String expectedMessage = "🎉 Welcome to EventMarket, Test User! 🎉 We're absolutely thrilled to have you with us. Start exploring the amazing events we have and dive into a world of excitement!";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testCreatorCreated() {
        notificationEngine.creatorCreated(creator);
        String expectedMessage = "👋 Hello Creator Name! Thank you for joining EventMarket as a creator. Start creating events and reach out to a wider audience. Let's make something incredible together!";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testTicketPaymentApproved() {
        notificationEngine.ticketPaymentApproved(user, ticketRequest);
        String expectedMessage = "✅ Hi Test User, Your payment for the ticket type 'VIP' has been approved! Get ready to enjoy 'Event Name'. See you there!";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testTicketPaymentRejected() {
        notificationEngine.ticketPaymentRejected(user, ticketRequest);
        String expectedMessage = "❌ Hi Test User, We're sorry to inform you that your payment for the ticket type 'VIP' for the event 'Event Name' has been rejected. Please try again or contact support.";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testTicketPaymentInProcess() {
        notificationEngine.ticketPaymentInProcess(user, ticketRequest);
        String expectedMessage = "⏳ Hi Test User, Your payment for the ticket type 'VIP' for the event 'Event Name' is currently being processed. You will be notified once it's approved. Stay tuned!";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testRecommendation() {
        List<Event> events = new ArrayList<>();
        events.add(event);
        notificationEngine.eventRecommendation(user, events);
        String expectedMessage = "🌟 Hi Test User, based on your interests, we highly recommend the following events: 'Event Name' on Sun Jan 01 00:00:00 CST 2023. Don't miss out on these exciting opportunities!";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testCreatorEventUpdate() {
        notificationEngine.creatorEventUpdate(creator, event);
        String expectedMessage = "👏 Hello Creator Name, Your event 'Event Name' has been successfully updated! You've sold 0 tickets so far. Keep up the fantastic work!";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }

    @Test
    void testEventCreated() {
        notificationEngine.eventCreated(creator, event);
        String expectedMessage = "🎉 Hi Creator Name, Your event 'Event Name' has been successfully created! Share it with the world and let the magic begin. 🌟";
        assertEquals(expectedMessage, notificationEngine.getMessage());
    }


    @AfterEach
    void tearDown() {
        if (em != null) {
            em.getTransaction().begin();

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

            if (event != null && em.contains(event)) {
                em.remove(event);
            }

            if (creator != null && em.contains(creator)) {
                em.remove(creator);
            }

            em.getTransaction().commit();
            em.close();
        }

        if (emf != null) {
            emf.close();
        }
    }
}
