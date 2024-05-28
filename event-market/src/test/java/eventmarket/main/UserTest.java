package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Date;

import eventmarket.main.CreateDate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private User newUser;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
    }

    @Test
    void testUserCreation() {
        em.getTransaction().begin();
        newUser = new User("Test", "test@test.com", "securePassword123", CreateDate.createDate(2000, 1, 1));
        em.persist(newUser);
        em.getTransaction().commit();
        User newUserFromDatabase = em.find(User.class, newUser.getId());
        assertNotNull(newUserFromDatabase);
        assertEquals(newUser.getName(), newUserFromDatabase.getName());
        assertEquals(newUser.getEmail(), newUserFromDatabase.getEmail());
    }

    @Test
    void testUserUpdate() {
        em.getTransaction().begin();
        newUser = new User("Test", "test@test.com", "securePassword123", CreateDate.createDate(2020, 1, 1));
        em.persist(newUser);
        em.getTransaction().commit();
        User newUserFromDatabase = em.find(User.class, newUser.getId());
        assertNotNull(newUserFromDatabase);
        assertEquals(newUser.getName(), newUserFromDatabase.getName());
        assertEquals(newUser.getEmail(), newUserFromDatabase.getEmail());
        em.getTransaction().begin();
        newUserFromDatabase.setName("New Test");
        newUserFromDatabase.setEmail("newtest@test.com");
        em.getTransaction().commit();
        User updatedUserFromDatabase = em.find(User.class, newUser.getId());
        assertNotNull(updatedUserFromDatabase);
        assertEquals(newUserFromDatabase.getName(), updatedUserFromDatabase.getName());
        assertEquals(newUserFromDatabase.getEmail(), updatedUserFromDatabase.getEmail());
    }

    @Test
    void testUserDeletion() {
        em.getTransaction().begin();
        newUser = new User("Test", "test@test.com", "securePassword123", CreateDate.createDate(2020, 1, 1));
        em.persist(newUser);
        em.getTransaction().commit();
        User newUserFromDatabase = em.find(User.class, newUser.getId());
        assertNotNull(newUserFromDatabase);
        em.getTransaction().begin();
        em.remove(newUserFromDatabase);
        em.getTransaction().commit();
        User deletedUserFromDatabase = em.find(User.class, newUser.getId());
        assertNull(deletedUserFromDatabase);
        newUser = null;
    }

    @Test
    void testGetAge() {
        em.getTransaction().begin();
        newUser = new User("Test", "test@test.com", "securePassword123", CreateDate.createDate(2000, 1, 1));
        int userAge = newUser.getAge();
        assertTrue(userAge >= 24);
    }

    @AfterEach
    void tearDown() {
        if (newUser != null && newUser.getId() != 0) {
            em.getTransaction().begin();
            newUser = em.find(User.class, newUser.getId());
            if (newUser != null) {
                em.remove(newUser);
                em.getTransaction().commit();
            }
        }
        em.close();
        emf.close();
    }
}
