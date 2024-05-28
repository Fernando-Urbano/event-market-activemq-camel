package eventmarketinput;

import eventmarket.main.User;
import eventmarket.main.CreateDate;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventMarketInput {
    private EntityManagerFactory emf;
    private EntityManager em;
    private ObjectMapper objectMapper;

    public EventMarketInput() {
        emf = Persistence.createEntityManagerFactory("EventMarketPU");
        em = emf.createEntityManager();
        objectMapper = new ObjectMapper();
    }
    public static void main(String[] args) {
        // EventMarketInput userCreation = new EventMarketInput();
    }
}
