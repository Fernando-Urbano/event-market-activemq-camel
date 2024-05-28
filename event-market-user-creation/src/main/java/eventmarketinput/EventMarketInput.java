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

    public void createUsers() {
        em.getTransaction().begin();

        User user1 = new User("Elvis Presley", "elvis@outlook.com", "password", CreateDate.createDate(1997, 10, 16));
        User user2 = new User("Airton Senna", "airton@gmail.com", "password", CreateDate.createDate(1997, 10, 16));
        User user3 = new User("Charlie Leclerc", "charlie@gmail.com", "password", CreateDate.createDate(1997, 10, 16));
        User user4 = new User("Fernando Urbano", "fernando@uchicago.edu", "password", CreateDate.createDate(1997, 10, 16));

        user1.setReceivesEmails(false);
        user2.setReceivesPushs(false);
        user4.setReceivesEmails(false);
        user4.setReceivesPushs(false);

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        em.persist(user4);

        em.getTransaction().commit();

        writeFile(user1);
        writeFile(user2);
        writeFile(user3);
        writeFile(user4);

        em.close();
        emf.close();
    }

    private void writeFile(User user) {
        try {
            File outboxDir = new File("data/outbox");
            if (!outboxDir.exists()) {
                outboxDir.mkdirs();
            }

            String formattedUserId = String.format("%010d", user.getId());
            File userFile = new File(outboxDir, "user-" + formattedUserId + "-creation.json");

            Map<String, Object> jsonContent = new HashMap<>();
            jsonContent.put("userId", user.getId());
            jsonContent.put("message", "user-creation");

            try (FileWriter writer = new FileWriter(userFile)) {
                objectMapper.writeValue(writer, jsonContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventMarketInput userCreation = new EventMarketInput();
        userCreation.createUsers();
    }
}
