package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class FinancialInformationTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private FinancialInformation newFinancialInformation;
    private User newUser;
    private PaymentMethod newPaymentMethod;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
        em.getTransaction().begin();
        newUser = new User("John Doe", "john@financialexample.com", "password", CreateDate.createDate(1990, 1, 1));
        em.persist(newUser);
        newPaymentMethod = new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123");
        em.persist(newPaymentMethod);
        em.getTransaction().commit();
    }

    @Test
    void testFinancialInformationCreation() {
        em.getTransaction().begin();
        em.persist(newPaymentMethod);
        newFinancialInformation = new FinancialInformation(newPaymentMethod);
        newFinancialInformation.setUser(newUser);
        newUser.addFinancialInformation(newFinancialInformation);
        em.persist(newFinancialInformation);
        em.getTransaction().commit();
    
        FinancialInformation retrievedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNotNull(retrievedFinancialInformation);
        assertEquals(newUser.getId(), retrievedFinancialInformation.getUser().getId());
        assertEquals(newPaymentMethod.getId(), retrievedFinancialInformation.getPaymentMethod().getId());
    }

    @Test
    void testFinancialInformationUpdate() {
        em.getTransaction().begin();
        newFinancialInformation = new FinancialInformation(newPaymentMethod);
        newUser.addFinancialInformation(newFinancialInformation);
        em.persist(newFinancialInformation);
        em.getTransaction().commit();

        FinancialInformation retrievedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNotNull(retrievedFinancialInformation);

        em.getTransaction().begin();
        PaymentMethod updatedPaymentMethod = new CheckingAccountPayment("123456789", "987654321");
        em.persist(updatedPaymentMethod);
        retrievedFinancialInformation.setPaymentMethod(updatedPaymentMethod);
        em.getTransaction().commit();

        FinancialInformation updatedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNotNull(updatedFinancialInformation);
        assertEquals(updatedPaymentMethod.getId(), updatedFinancialInformation.getPaymentMethod().getId());
    }

    @Test
    void testFinancialInformationDeletion() {
        em.getTransaction().begin();
        newFinancialInformation = new FinancialInformation(newPaymentMethod);
        newUser.addFinancialInformation(newFinancialInformation);
        em.persist(newFinancialInformation);
        em.getTransaction().commit();
    
        FinancialInformation retrievedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNotNull(retrievedFinancialInformation);
    
        em.getTransaction().begin();
        User user = newFinancialInformation.getUser();
        if (user != null) {
            user.getFinancialInformation().remove(newFinancialInformation);
        }
        em.remove(retrievedFinancialInformation);
        em.getTransaction().commit();
    
        em.clear(); // Clear the persistence context cache
    
        FinancialInformation deletedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNull(deletedFinancialInformation);
    }

    @Test
    void testFinancialInformationDeletionCascadesPaymentMethod() {
        em.getTransaction().begin();
        newFinancialInformation = new FinancialInformation(newPaymentMethod);
        newUser.addFinancialInformation(newFinancialInformation);
        em.persist(newFinancialInformation);
        em.getTransaction().commit();

        FinancialInformation retrievedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNotNull(retrievedFinancialInformation);

        int paymentMethodId = retrievedFinancialInformation.getPaymentMethod().getId();

        em.getTransaction().begin();
        em.remove(retrievedFinancialInformation);
        User user = newFinancialInformation.getUser();
        if (user != null) {
            user.getFinancialInformation().remove(newFinancialInformation);
        }
        em.getTransaction().commit();

        FinancialInformation deletedFinancialInformation = em.find(FinancialInformation.class, newFinancialInformation.getId());
        assertNull(deletedFinancialInformation);

        PaymentMethod deletedPaymentMethod = em.find(PaymentMethod.class, paymentMethodId);
        assertNull(deletedPaymentMethod);
    }

    @AfterEach
    void tearDown() {
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
        if (newPaymentMethod != null && newPaymentMethod.getId() != 0) {
            em.getTransaction().begin();
            newPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
            if (newPaymentMethod != null) {
                em.remove(newPaymentMethod);
            }
            em.getTransaction().commit();
        }
        em.close();
        emf.close();
    }
}