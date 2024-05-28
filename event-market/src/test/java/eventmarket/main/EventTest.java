package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    private EntityManagerFactory emf;
    private EntityManager em;
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
        em.getTransaction().commit();
    }

    @Test
    void testEventCreation() {
        em.getTransaction().begin();
        creator = new Creator("Event Creator", "creator@example.com", "password");
        em.persist(creator);
        Location location = new Location("123 Main St", "Anytown", "12345", "State", "Country");
        em.persist(location);
        newEvent = new Event(
            "Summer Festival",
            location,
            CreateDate.createDate(2000, 0, 0, 13, 0, 0),
            CreateDate.createDate(2000, 0, 0, 15, 0, 0),
            "Fun in the sun", "Come join us!"
        );
        newEvent.setCreator(creator);
        em.persist(newEvent);
        em.getTransaction().commit();
        Event retrievedEvent = em.find(Event.class, newEvent.getId());
        assertNotNull(retrievedEvent);
        assertEquals("Summer Festival", retrievedEvent.getName());
        assertEquals(creator.getId(), retrievedEvent.getCreator().getId());
    }
    

    @Test
    void testEventUpdate() {
        em.getTransaction().begin();
        // Create and persist a Creator
        creator = new Creator("Event Creator", "creator@example.com", "password");
        em.persist(creator);
        
        // Assuming location is already persisted in setUp, if not persist it here
        newEvent = new Event(
            "Summer Festival",
            location,
            CreateDate.createDate(2000, 0, 0, 13, 0, 0),
            CreateDate.createDate(2000, 0, 0, 15, 0, 0),
            "Fun in the sun", "Come join us!"
        );
        newEvent.setCreator(creator);
        em.persist(newEvent);
        em.getTransaction().commit();
        Event retrievedEvent = em.find(Event.class, newEvent.getId());
        assertNotNull(retrievedEvent);
        em.getTransaction().begin();
        retrievedEvent.setName("Updated Event");
        retrievedEvent.setPublicDescription("Updated Description");
        em.getTransaction().commit();
        Event updatedEvent = em.find(Event.class, retrievedEvent.getId());
        assertNotNull(updatedEvent);
        assertEquals("Updated Event", updatedEvent.getName());
        assertEquals("Updated Description", updatedEvent.getPublicDescription());
    }

    @Test
    void testEventDeletion() {
        em.getTransaction().begin();
        creator = new Creator("Event Creator", "creator@example.com", "password");
        em.persist(creator);
        newEvent = new Event(
            "Summer Festival",
            location,
            CreateDate.createDate(2000, 0, 0, 13, 0, 0),
            CreateDate.createDate(2000, 0, 0, 15, 0, 0),
            "Fun in the sun", "Come join us!"
        );
        newEvent.setCreator(creator);
        em.persist(newEvent);
        em.getTransaction().commit();
        Event retrievedEvent = em.find(Event.class, newEvent.getId());
        assertNotNull(retrievedEvent);
        em.getTransaction().begin();
        em.remove(retrievedEvent);
        em.getTransaction().commit();
        Event deletedEvent = em.find(Event.class, newEvent.getId());
        assertNull(deletedEvent);
    }

    @AfterEach
    void tearDown() {
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
