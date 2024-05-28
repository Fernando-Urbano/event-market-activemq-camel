package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class TicketTypeIteratorTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private Event event;
    private Creator newCreator;
    private TicketTypeIterator iterator;
    private TicketType vip;
    private TicketType general;
    private TicketType earlyBird;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
    
        em.getTransaction().begin();
        Creator creator = new Creator("Creator Name", "creator@example.com", "securePassword");
        em.persist(creator);
    
        event = new Event(
            "Test Event",
            new Location("Address", "City", "12345", "State", "Country"),
            CreateDate.createDate(2023, 1, 1), CreateDate.createDate(2023, 1, 2),
            "Test Description", "Test Tagline"
        );
        event.setCreator(creator);
        em.persist(event);
    
        vip = new TicketType("VIP", "VIP access", 100, 18, 200.0, event);
        general = new TicketType("General", "General admission", 500, 18, 100.0, event);
        earlyBird = new TicketType("Early Bird", "Early bird special", 200, 18, 80.0, event);
    
        em.persist(vip);
        em.persist(general);
        em.persist(earlyBird);
    
        em.getTransaction().commit();
    
        event = em.find(Event.class, event.getId());
        iterator = new TicketTypeIterator(event);
    }

    @AfterEach
    void tearDown() {
        if (event != null) {
            em.getTransaction().begin();
            event = em.find(Event.class, event.getId());
            if (event != null) {
                for (TicketType ticketType : event.getTicketTypes()) {
                    em.remove(ticketType);
                }
                Creator creator = event.getCreator();
                event.setCreator(null);
                em.remove(event);
                if (creator != null) {
                    em.remove(creator);
                }
            }
            em.getTransaction().commit();
        }

        if (em != null) {
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    @Test
    void testNext() {
        event = em.find(Event.class, event.getId());
        iterator = new TicketTypeIterator(event);
        assertEquals("VIP", iterator.next().getName());
        assertEquals("General", iterator.next().getName());
        assertEquals("Early Bird", iterator.next().getName());
    }

    @Test
    void testNextWhenDone() {
        iterator.next();
        iterator.next();
        iterator.next();
        assertThrows(IllegalStateException.class, () -> iterator.next());
    }

    @Test
    void testGetSteps() {
        assertEquals(0, iterator.getSteps());
        iterator.next();
        assertEquals(1, iterator.getSteps());
        iterator.next();
        assertEquals(2, iterator.getSteps());
    }

    @Test
    void testSetSteps() {
        iterator.setSteps(1);
        assertEquals("General", iterator.current().getName());
    }

    @Test
    void testSetStepsInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> iterator.setSteps(-1));
        assertThrows(IllegalArgumentException.class, () -> iterator.setSteps(3));
    }

    @Test
    void testIsDone() {
        assertFalse(iterator.isDone());
        iterator.next();
        assertFalse(iterator.isDone());
        iterator.next();
        assertFalse(iterator.isDone());
        iterator.next();
        assertTrue(iterator.isDone());
    }

    @Test
    void testCurrent() {
        assertEquals("VIP", iterator.current().getName());
        iterator.next();
        assertEquals("General", iterator.current().getName());
    }

    @Test
    void testCurrentWhenDone() {
        iterator.next();
        iterator.next();
        iterator.next();
        assertThrows(IllegalStateException.class, () -> iterator.current());
    }

    @Test
    void testFirst() {
        iterator.next();
        iterator.next();
        assertEquals("Early Bird", iterator.current().getName());
        assertEquals("VIP", iterator.first().getName());
        assertEquals("VIP", iterator.current().getName());
    }

}