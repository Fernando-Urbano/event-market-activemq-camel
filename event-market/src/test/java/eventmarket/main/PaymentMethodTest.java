package eventmarket.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private PaymentMethod newPaymentMethod;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU-Test");
        em = emf.createEntityManager();
    }

    @Test
    void testCardPaymentCreation() {
        em.getTransaction().begin();
        newPaymentMethod = new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123");
        em.persist(newPaymentMethod);
        em.getTransaction().commit();
        
        PaymentMethod retrievedPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
        assertNotNull(retrievedPaymentMethod);
        assertTrue(retrievedPaymentMethod instanceof CardPayment);
        CardPayment cardPayment = (CardPayment) retrievedPaymentMethod;
        assertEquals("1234567890123456", cardPayment.getCardNumber());
        assertEquals("John Doe", cardPayment.getCardHolder());
    }

    @Test
    void testCheckingAccountPaymentCreation() {
        em.getTransaction().begin();
        newPaymentMethod = new CheckingAccountPayment("123456789", "987654321");
        em.persist(newPaymentMethod);
        em.getTransaction().commit();
        
        PaymentMethod retrievedPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
        assertNotNull(retrievedPaymentMethod);
        assertTrue(retrievedPaymentMethod instanceof CheckingAccountPayment);
        CheckingAccountPayment checkingAccountPayment = (CheckingAccountPayment) retrievedPaymentMethod;
        assertEquals("123456789", checkingAccountPayment.getAccountNumber());
        assertEquals("987654321", checkingAccountPayment.getRoutingNumber());
    }

    @Test
    void testPaymentMethodDeletion() {
        em.getTransaction().begin();
        newPaymentMethod = new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123");
        em.persist(newPaymentMethod);
        em.getTransaction().commit();

        PaymentMethod retrievedPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
        assertNotNull(retrievedPaymentMethod);

        em.getTransaction().begin();
        em.remove(retrievedPaymentMethod);
        em.getTransaction().commit();

        PaymentMethod deletedPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
        assertNull(deletedPaymentMethod);
    }

    @Test
    void testPaymentMethodUpdate() {
        em.getTransaction().begin();
        newPaymentMethod = new CardPayment("1234567890123456", "John Doe", CreateDate.createDate(2025, 12, 1), "123");
        em.persist(newPaymentMethod);
        em.getTransaction().commit();

        PaymentMethod retrievedPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
        assertNotNull(retrievedPaymentMethod);
        assertTrue(retrievedPaymentMethod instanceof CardPayment);
        
        em.getTransaction().begin();
        CardPayment cardPayment = (CardPayment) retrievedPaymentMethod;
        cardPayment.setCardNumber("9876543210987654");
        cardPayment.setCardHolder("Jane Smith");
        em.getTransaction().commit();

        PaymentMethod updatedPaymentMethod = em.find(PaymentMethod.class, newPaymentMethod.getId());
        assertNotNull(updatedPaymentMethod);
        assertTrue(updatedPaymentMethod instanceof CardPayment);
        CardPayment updatedCardPayment = (CardPayment) updatedPaymentMethod;
        assertEquals("9876543210987654", updatedCardPayment.getCardNumber());
        assertEquals("Jane Smith", updatedCardPayment.getCardHolder());
    }

    @AfterEach
    void tearDown() {
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