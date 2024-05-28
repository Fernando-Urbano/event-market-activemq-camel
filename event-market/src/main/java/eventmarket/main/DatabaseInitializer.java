package eventmarket.main;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DatabaseInitializer {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("EventMarketPU");
        emf.close();
    }
}