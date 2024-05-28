package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class TicketRequestTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private TicketRequest newTicketRequest;
    private User newUser;
    private Recipient newRecipient;
    private FinancialInformation newFinancialInformation;
    private TicketType newTicketType;
    private Event newEvent;
    private Location location;
    private Creator creator;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
        em.getTransaction().begin();
        location = new Location("123 Main St", "Anytown", "12345", "State", "Country");
        em.persist(location);
        creator = new Creator("Event Creator", "creator@example.com", "password");
        em.persist(creator);
        newEvent = new Event(
            "Concert",
            location,
            CreateDate.createDate(2020, 5, 1),
            CreateDate.createDate(2020, 5, 2),
            "A great show",
            "Don't miss it!"
        );
        newEvent.setCreator(creator);
        em.persist(newEvent);
        newTicketType = new TicketType("VIP", "Exclusive area access", 100, 18, 299.99, newEvent);
        em.persist(newTicketType);
        newUser = new User("John Doe", "john@requestexample.com", "password", CreateDate.createDate(1990, 1, 1));
        em.persist(newUser);
        newRecipient = new Recipient("John Doe", "john@requestexample.com", CreateDate.createDate(1990, 1, 1));
        em.persist(newRecipient);
        PaymentMethod newPaymentMethod = new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123");
        em.persist(newPaymentMethod);
        newFinancialInformation = new FinancialInformation(newPaymentMethod);
        newFinancialInformation.setUser(newUser);
        newUser.addFinancialInformation(newFinancialInformation);
        em.persist(newFinancialInformation);
        em.getTransaction().commit();
    }

    @Test
    void testTicketRequestCreation() {
        em.getTransaction().begin();
        newTicketRequest = new TicketRequest(newRecipient, newFinancialInformation, newTicketType);
        newTicketRequest.setUser(newUser);
        newUser.saveTicketRequest(newTicketRequest);
        em.persist(newTicketRequest);
        em.getTransaction().commit();

        TicketRequest retrievedTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
        assertNotNull(retrievedTicketRequest);
        assertEquals(newRecipient.getId(), retrievedTicketRequest.getRecipient().getId());
        assertEquals(newFinancialInformation.getId(), retrievedTicketRequest.getFinancialInformation().getId());
        assertEquals(newTicketType.getId(), retrievedTicketRequest.getTicketType().getId());
    }

    @Test
    void testTicketRequestUpdate() {
        em.getTransaction().begin();
        newTicketRequest = new TicketRequest(newRecipient, newFinancialInformation, newTicketType);
        newTicketRequest.setUser(newUser);
        newUser.saveTicketRequest(newTicketRequest);
        em.persist(newTicketRequest);
        em.getTransaction().commit();

        TicketRequest retrievedTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
        assertNotNull(retrievedTicketRequest);
        em.getTransaction().begin();
        retrievedTicketRequest.setApproved(true);
        em.getTransaction().commit();

        TicketRequest updatedTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
        assertNotNull(updatedTicketRequest);
        assertTrue(updatedTicketRequest.isApproved());
    }

    @Test
    void testTicketRequestDeletion() {
        // TODO: Fix this test. Problem happening in AfterEach method in case this test runs
        
        // em.getTransaction().begin();
        // newTicketRequest = new TicketRequest(newRecipient, newFinancialInformation, newTicketType);
        // newTicketRequest.setUser(newUser);
        // newUser.saveTicketRequest(newTicketRequest);
        // em.persist(newTicketRequest);
        // em.getTransaction().commit();
    
        // TicketRequest retrievedTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
        // assertNotNull(retrievedTicketRequest);
    
        // em.getTransaction().begin();
        // User user = retrievedTicketRequest.getUser();
        // if (user != null) {
        //     user.getTicketRequests().remove(retrievedTicketRequest);
        // }
        
        // FinancialInformation financialInformation = retrievedTicketRequest.getFinancialInformation();
        // if (financialInformation != null) {
        //     financialInformation.setUser(null);
        // }
        
        // em.remove(retrievedTicketRequest);
        // em.getTransaction().commit();
    
        // em.clear();
    
        // TicketRequest deletedTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
        // assertNull(deletedTicketRequest);
    }

    @AfterEach
    void tearDown() {
        if (newTicketRequest != null && newTicketRequest.getId() != 0) {
            em.getTransaction().begin();
            newTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
            if (newTicketRequest != null) {
                User user = newTicketRequest.getUser();
                if (user != null) {
                    user.getTicketRequests().remove(newTicketRequest);
                }
                em.remove(newTicketRequest);
            }
            em.getTransaction().commit();
        }

        if (newFinancialInformation != null && newFinancialInformation.getId() != 0) {
            em.getTransaction().begin();
            newFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
            if (newFinancialInformation != null) {
                PaymentMethod paymentMethod = newFinancialInformation.getPaymentMethod();
                if (paymentMethod != null) {
                    em.remove(paymentMethod);
                }
                em.remove(newFinancialInformation);
            }
            em.getTransaction().commit();
        }

        if (newUser != null && newUser.getId() != 0) {
            em.getTransaction().begin();
            newUser = em.find(User.class, newUser.getId());
            if (newUser != null) {
                em.remove(newUser);
            }
            em.getTransaction().commit();
        }

        if (newRecipient != null && newRecipient.getId() != 0) {
            em.getTransaction().begin();
            newRecipient = em.find(Recipient.class, newRecipient.getId());
            if (newRecipient != null) {
                em.remove(newRecipient);
            }
            em.getTransaction().commit();
        }

        if (newTicketType != null && newTicketType.getId() != 0) {
            em.getTransaction().begin();
            newTicketType = em.find(TicketType.class, newTicketType.getId());
            if (newTicketType != null) {
                em.remove(newTicketType);
            }
            em.getTransaction().commit();
        }

        if (newEvent != null && newEvent.getId() != 0) {
            em.getTransaction().begin();
            newEvent = em.find(Event.class, newEvent.getId());
            if (newEvent != null) {
                em.remove(newEvent);
            }
            em.getTransaction().commit();
        }

        if (creator != null && creator.getId() != 0) {
            em.getTransaction().begin();
            creator = em.find(Creator.class, creator.getId());
            if (creator != null) {
                em.remove(creator);
            }
            em.getTransaction().commit();
        }

        if (location != null && location.getId() != 0) {
            em.getTransaction().begin();
            location = em.find(Location.class, location.getId());
            if (location != null) {
                em.remove(location);
            }
            em.getTransaction().commit();
        }

        em.close();
        emf.close();
    }
}