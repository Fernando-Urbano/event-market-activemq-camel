package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import eventmarket.main.CreateDate;

import static org.junit.jupiter.api.Assertions.*;

class CreatorTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private Creator newCreator;
    private Event newEvent;
    private Location location;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
    }

    @Test
    void testCreatorCreation() {
        em.getTransaction().begin();
        newCreator = new Creator("Creator Name", "creator@example.com", "securePassword");
        em.persist(newCreator);
        em.getTransaction().commit();
        
        Creator retrievedCreator = em.find(Creator.class, newCreator.getId());
        assertNotNull(retrievedCreator);
        assertEquals("Creator Name", retrievedCreator.getName());
        assertEquals("creator@example.com", retrievedCreator.getEmail());
    }

    @Test
    void testCreatorUpdate() {
        em.getTransaction().begin();
        newCreator = new Creator("Original Creator", "original@creator.com", "password123");
        em.persist(newCreator);
        em.getTransaction().commit();

        Creator retrievedCreator = em.find(Creator.class, newCreator.getId());
        assertNotNull(retrievedCreator);
        em.getTransaction().begin();
        retrievedCreator.setName("Updated Creator");
        retrievedCreator.setEmail("updated@creator.com");
        em.getTransaction().commit();

        Creator updatedCreator = em.find(Creator.class, retrievedCreator.getId());
        assertNotNull(updatedCreator);
        assertEquals("Updated Creator", updatedCreator.getName());
        assertEquals("updated@creator.com", updatedCreator.getEmail());
    }

    @Test
    void testCreatorDeletion() {
        em.getTransaction().begin();
        newCreator = new Creator("To Delete", "delete@creator.com", "password");
        em.persist(newCreator);
        em.getTransaction().commit();

        Creator retrievedCreator = em.find(Creator.class, newCreator.getId());
        assertNotNull(retrievedCreator);

        em.getTransaction().begin();
        em.remove(retrievedCreator);
        em.getTransaction().commit();

        Creator deletedCreator = em.find(Creator.class, newCreator.getId());
        assertNull(deletedCreator);
    }

    @Test
    void testAddEvent() {
        em.getTransaction().begin();
        newCreator = new Creator("Test Creator", "testcreator@creator.com", "password");
        em.persist(newCreator);
        em.getTransaction().commit();
        location = new Location("Main Streem", "New York", "60931", "CT", "USA");
        newEvent = new Event(
            "Event Name",
            location,
            CreateDate.createDate(0, 0, 0),
            CreateDate.createDate(1, 0, 0),
            "Event Description",
            "Event tagline"
        );
        newCreator.addEvent(newEvent);
        em.getTransaction().begin();
        em.persist(newEvent);
        em.getTransaction().commit();
    }

    @Test
    void testCreatorDeletionAfterAddedEvent() {
        em.getTransaction().begin();
        newCreator = new Creator("Test Creator", "testcreator@creator.com", "password");
        em.persist(newCreator);
        em.getTransaction().commit();
        location = new Location("Main Streem", "New York", "60931", "CT", "USA");   
        newEvent = new Event(
            "Event Name",
            location,
            CreateDate.createDate(0, 0, 0),
            CreateDate.createDate(1, 0, 0),
            "Event Description",
            "Event tagline"
        );  
        newCreator.addEvent(newEvent);
        em.getTransaction().begin();    
        em.persist(newEvent);
        em.getTransaction().commit();
        em.getTransaction().begin(); 
        em.remove(newCreator);
        em.getTransaction().commit();
        Creator deletedCreator = em.find(Creator.class, newCreator.getId());
        assertNull(deletedCreator);
        Event retrievedEvent = em.find(Event.class, newEvent.getId());
        assertNull(retrievedEvent);
    }

    @AfterEach
    void tearDown() {
        if (newCreator != null && newCreator.getId() != 0) {
            em.getTransaction().begin();
            newCreator = em.find(Creator.class, newCreator.getId());
            if (newCreator != null) {
                em.remove(newCreator);
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
