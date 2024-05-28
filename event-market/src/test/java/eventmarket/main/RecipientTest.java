package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class RecipientTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private Recipient newRecipient;
    private User newUser;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
    }

    @Test
    void testRecipientCreation() {
        em.getTransaction().begin();
        newRecipient = new Recipient("Test Recipient", "recipient@test.com", new Date());
        em.persist(newRecipient);
        em.getTransaction().commit();
        
        Recipient retrievedRecipient = em.find(Recipient.class, newRecipient.getId());
        assertNotNull(retrievedRecipient);
        assertEquals("Test Recipient", retrievedRecipient.getName());
        assertEquals("recipient@test.com", retrievedRecipient.getEmail());
    }

    @Test
    void testRecipientUpdate() {
        em.getTransaction().begin();
        newRecipient = new Recipient("Original Name", "original@test.com", new Date());
        em.persist(newRecipient);
        em.getTransaction().commit();

        Recipient retrievedRecipient = em.find(Recipient.class, newRecipient.getId());
        assertNotNull(retrievedRecipient);
        em.getTransaction().begin();
        retrievedRecipient.setName("Updated Name");
        retrievedRecipient.setEmail("updated@test.com");
        em.getTransaction().commit();

        Recipient updatedRecipient = em.find(Recipient.class, retrievedRecipient.getId());
        assertNotNull(updatedRecipient);
        assertEquals("Updated Name", updatedRecipient.getName());
        assertEquals("updated@test.com", updatedRecipient.getEmail());
    }

    @Test
    void testRecipientDeletion() {
        em.getTransaction().begin();
        newRecipient = new Recipient("To Delete", "delete@test.com", new Date());
        em.persist(newRecipient);
        em.getTransaction().commit();

        Recipient retrievedRecipient = em.find(Recipient.class, newRecipient.getId());
        assertNotNull(retrievedRecipient);

        em.getTransaction().begin();
        em.remove(retrievedRecipient);
        em.getTransaction().commit();

        Recipient deletedRecipient = em.find(Recipient.class, newRecipient.getId());
        assertNull(deletedRecipient);
    }

    @Test
    void testTransformUserInRecipient() {
        em.getTransaction().begin();
        newUser = new User("Test", "test@test.com", "securePassword123", CreateDate.createDate(2000, 1, 1));
        em.persist(newUser);
        em.getTransaction().commit();
        Recipient newRecipient = newUser.transformToRecipient();
        assertNotNull(newRecipient);
        assertEquals(newUser.getName(), newRecipient.getName());
        assertEquals(newUser.getEmail(), newRecipient.getEmail());
    }        

    @Test
    void testConstructorRecipientFromUser() {
        em.getTransaction().begin();
        newUser = new User("Test", "test@test.com", "securePassword123", CreateDate.createDate(2000, 1, 1));
        em.persist(newUser);
        em.getTransaction().commit();
        Recipient newRecipient = new Recipient(newUser);
        assertNotNull(newRecipient);
        assertEquals(newUser.getName(), newRecipient.getName());
        assertEquals(newUser.getEmail(), newRecipient.getEmail());
    }

    @AfterEach
    void tearDown() {
        if (newRecipient != null && newRecipient.getId() != 0) {
            em.getTransaction().begin();
            newRecipient = em.find(Recipient.class, newRecipient.getId());
            if (newRecipient != null) {
                em.remove(newRecipient);
            }
            em.getTransaction().commit();
        }
        // do the same for user
        if (newUser != null && newUser.getId() != 0) {
            em.getTransaction().begin();
            newUser = em.find(User.class, newUser.getId());
            if (newUser != null) {
                em.remove(newUser);
            }
            em.getTransaction().commit();
        }
        em.close();
        emf.close();
    }
}
