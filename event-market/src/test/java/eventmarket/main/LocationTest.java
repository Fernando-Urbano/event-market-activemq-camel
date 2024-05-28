package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private Location newLocation;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
    }

    @Test
    void testLocationCreation() {
        em.getTransaction().begin();
        newLocation = new Location("123 Festival Lane", "Festival City", "98765", "FunState", "FestCountry");
        em.persist(newLocation);
        em.getTransaction().commit();
        
        Location retrievedLocation = em.find(Location.class, newLocation.getId());
        assertNotNull(retrievedLocation);
        assertEquals("123 Festival Lane", retrievedLocation.getAddress());
        assertEquals("Festival City", retrievedLocation.getCity());
        assertEquals("98765", retrievedLocation.getZipcode());
        assertEquals("FunState", retrievedLocation.getState());
        assertEquals("FestCountry", retrievedLocation.getCountry());
    }

    @Test
    void testLocationUpdate() {
        em.getTransaction().begin();
        newLocation = new Location("123 Initial Lane", "Initial City", "11111", "InitialState", "InitialCountry");
        em.persist(newLocation);
        em.getTransaction().commit();

        Location retrievedLocation = em.find(Location.class, newLocation.getId());
        assertNotNull(retrievedLocation);
        em.getTransaction().begin();
        retrievedLocation.setAddress("Updated Address");
        retrievedLocation.setCity("Updated City");
        em.getTransaction().commit();

        Location updatedLocation = em.find(Location.class, retrievedLocation.getId());
        assertNotNull(updatedLocation);
        assertEquals("Updated Address", updatedLocation.getAddress());
        assertEquals("Updated City", updatedLocation.getCity());
    }

    @Test
    void testLocationDeletion() {
        em.getTransaction().begin();
        newLocation = new Location("To Delete Lane", "Delete City", "00000", "DeleteState", "DeleteCountry");
        em.persist(newLocation);
        em.getTransaction().commit();

        Location retrievedLocation = em.find(Location.class, newLocation.getId());
        assertNotNull(retrievedLocation);

        em.getTransaction().begin();
        em.remove(retrievedLocation);
        em.getTransaction().commit();

        Location deletedLocation = em.find(Location.class, newLocation.getId());
        assertNull(deletedLocation);
    }

    @AfterEach
    void tearDown() {
        if (newLocation != null && newLocation.getId() != 0) {
            em.getTransaction().begin();
            newLocation = em.find(Location.class, newLocation.getId());
            if (newLocation != null) {
                em.remove(newLocation);
            }
            em.getTransaction().commit();
        }
        em.close();
        emf.close();
    }
}
