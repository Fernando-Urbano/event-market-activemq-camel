package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TicketTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private Ticket newTicket;
    private User newUser;
    private TicketRequest newTicketRequest;
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
        newUser = new User("John Doe", "john@example.com", "password", CreateDate.createDate(1990, 1, 1));
        em.persist(newUser);
        Recipient newRecipient = new Recipient("John Doe", "john@example.com", CreateDate.createDate(1990, 1, 1));
        em.persist(newRecipient);
        FinancialInformation newFinancialInformation = new FinancialInformation(new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123"));
        newFinancialInformation.setUser(newUser);
        newUser.addFinancialInformation(newFinancialInformation);
        em.persist(newFinancialInformation);
        newTicketRequest = new TicketRequest(newRecipient, newFinancialInformation, newTicketType);
        newTicketRequest.setUser(newUser);
        newUser.saveTicketRequest(newTicketRequest);
        em.persist(newTicketRequest);
        em.getTransaction().commit();
    }

    @Test
    void testTicketCreation() {
        em.getTransaction().begin();
        newTicketRequest.approveRequest();
        newTicket = newTicketRequest.transformToTicket();
        em.persist(newTicket);
        em.getTransaction().commit();

        Ticket retrievedTicket = em.find(Ticket.class, newTicket.getId());
        assertNotNull(retrievedTicket);
        assertEquals(newTicketRequest.getId(), retrievedTicket.getTicketRequest().getId());
        assertEquals(newUser.getId(), retrievedTicket.getUser().getId());
    }

    @Test
    void testTicketUpdate() {
        em.getTransaction().begin();
        newTicketRequest.approveRequest();
        newTicket = newTicketRequest.transformToTicket();
        em.persist(newTicket);
        em.getTransaction().commit();

        Ticket retrievedTicket = em.find(Ticket.class, newTicket.getId());
        assertNotNull(retrievedTicket);
        em.getTransaction().begin();
        retrievedTicket.setCheckedIn(true);
        em.getTransaction().commit();

        Ticket updatedTicket = em.find(Ticket.class, newTicket.getId());
        assertNotNull(updatedTicket);
        assertTrue(updatedTicket.isCheckedIn());
    }

    @Test
    void testTicketDeletion() {
        em.getTransaction().begin();
        newTicketRequest.approveRequest();
        newTicket = newTicketRequest.transformToTicket();
        em.persist(newTicket);
        em.getTransaction().commit();

        Ticket retrievedTicket = em.find(Ticket.class, newTicket.getId());
        assertNotNull(retrievedTicket);

        em.getTransaction().begin();
        User user = retrievedTicket.getUser();
        if (user != null) {
            user.getTickets().remove(retrievedTicket);
        }
        em.remove(retrievedTicket);
        em.getTransaction().commit();

        em.clear();

        Ticket deletedTicket = em.find(Ticket.class, newTicket.getId());
        assertNull(deletedTicket);
    }

    @AfterEach
    void tearDown() {
        if (newTicket != null && newTicket.getId() != 0) {
            em.getTransaction().begin();
            newTicket = em.find(Ticket.class, newTicket.getId());
            if (newTicket != null) {
                em.remove(newTicket);
            }
            em.getTransaction().commit();
        }

        if (newTicketRequest != null && newTicketRequest.getId() != 0) {
            em.getTransaction().begin();
            newTicketRequest = em.find(TicketRequest.class, newTicketRequest.getId());
            if (newTicketRequest != null) {
                em.remove(newTicketRequest);
            }
            em.getTransaction().commit();
        }

        if (newUser != null && newUser.getId() != 0) {
            em.getTransaction().begin();
            newUser = em.find(User.class, newUser.getId());
            if (newUser != null) {
                List<FinancialInformation> financialInformationList = newUser.getFinancialInformation();
                for (FinancialInformation financialInformation : financialInformationList) {
                    PaymentMethod paymentMethod = financialInformation.getPaymentMethod();
                    if (paymentMethod != null) {
                        em.remove(paymentMethod);
                    }
                    em.remove(financialInformation);
                }
                em.remove(newUser);
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