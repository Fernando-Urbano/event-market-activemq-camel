package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class TicketTypeTest {
    private EntityManagerFactory emf;
    private EntityManager em;
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
        em.getTransaction().commit();
    }

    @Test
    void testTicketTypeCreation() {
        em.getTransaction().begin();
        newTicketType = new TicketType("VIP", "Exclusive area access", 100, 18, 299.99, newEvent);
        em.persist(newTicketType);
        em.getTransaction().commit();
        
        TicketType retrievedTicketType = em.find(TicketType.class, newTicketType.getId());
        assertNotNull(retrievedTicketType);
        assertEquals("VIP", retrievedTicketType.getName());
        assertEquals(299.99, retrievedTicketType.getPrice());
    }

    @Test
    void testTicketTypeUpdate() {
        em.getTransaction().begin();
        newTicketType = new TicketType("Standard", "Access to general areas", 500, 18, 99.99, newEvent);
        em.persist(newTicketType);
        em.getTransaction().commit();

        TicketType retrievedTicketType = em.find(TicketType.class, newTicketType.getId());
        assertNotNull(retrievedTicketType);
        em.getTransaction().begin();
        retrievedTicketType.setPrice(89.99);
        em.getTransaction().commit();

        TicketType updatedTicketType = em.find(TicketType.class, retrievedTicketType.getId());
        assertNotNull(updatedTicketType);
        assertEquals(89.99, updatedTicketType.getPrice());
    }

    @Test
    void testTicketTypeDeletion() {
        em.getTransaction().begin();
        newTicketType = new TicketType(
            "Early Bird",
            "Early access tickets",
            200,
            18,
            49.99, newEvent
        );
        em.persist(newTicketType);
        em.getTransaction().commit();
    
        TicketType retrievedTicketType = em.find(TicketType.class, newTicketType.getId());
        assertNotNull(retrievedTicketType);
    
        em.getTransaction().begin();
        newEvent.removeTicketType(retrievedTicketType);
        em.remove(retrievedTicketType);
        em.getTransaction().commit();
    
        TicketType deletedTicketType = em.find(TicketType.class, newTicketType.getId());
        assertNull(deletedTicketType);
    }
    
    

    @Test
    void testAddTicketTypeToEvent() {
        em.getTransaction().begin();
        newTicketType = new TicketType("General Admission", "Access to general areas", 1000, 18, 19.99, newEvent);
        em.persist(newTicketType);
        em.getTransaction().commit();
        Event retrievedEvent = em.find(Event.class, newEvent.getId());
        assertNotNull(retrievedEvent);
        assertEquals(1, retrievedEvent.getTicketTypes().size());
    }

    @Test
    void testAddTicketTypeToEventSaveAndRetrieveEvent() {
        em.getTransaction().begin();
        newTicketType = new TicketType("General Admission", "Access to general areas", 1000, 18, 19.99, newEvent);
        em.persist(newTicketType);
        em.getTransaction().commit();
        Event retrievedEvent = em.find(Event.class, newEvent.getId());
        assertNotNull(retrievedEvent);
        assertEquals(1, retrievedEvent.getTicketTypes().size());
        TicketType retrievedTicketType = retrievedEvent.getTicketTypes().get(0);
        assertNotNull(retrievedTicketType);
        assertEquals("General Admission", retrievedTicketType.getName());
        em.getTransaction().begin();
        em.persist(retrievedEvent);
        em.getTransaction().commit();
        Event savedEvent = em.find(Event.class, newEvent.getId());
        assertNotNull(savedEvent);
        assertEquals(1, savedEvent.getTicketTypes().size());
        TicketType savedTicketType = savedEvent.getTicketTypes().get(0);
        assertNotNull(savedTicketType);
        assertEquals("General Admission", savedTicketType.getName());
    }

    @AfterEach
    void tearDown() {
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
