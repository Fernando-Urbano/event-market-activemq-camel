package eventmarketinput;

import eventmarket.main.Creator;
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

    public void createCreators() {
        em.getTransaction().begin();

        Creator creator1 = new Creator("Summer Parties", "summerparties@music.com", "password");
        Creator creator2 = new Creator("Classic Music Concerts", "classicmusic@concerts.com", "password");
        Creator creator3 = new Creator("Charlie Music Festivals", "charliefest@gmail.com", "password");
        Creator creator4 = new Creator("Lollapaloza", "mainaccount@lollapaloza.com", "password");

        em.persist(creator1);
        em.persist(creator2);
        em.persist(creator3);
        em.persist(creator4);

        em.getTransaction().commit();

        writeFile(creator1);
        writeFile(creator2);
        writeFile(creator3);
        writeFile(creator4);

        em.close();
        emf.close();
    }

    private void writeFile(Creator creator) {
        try {
            File outboxDir = new File("data/outbox");
            if (!outboxDir.exists()) {
                outboxDir.mkdirs();
            }

            String formattedCreatorId = String.format("%010d", creator.getId());
            File creatorFile = new File(outboxDir, "creator-" + formattedCreatorId + "-creation.json");

            Map<String, Object> jsonContent = new HashMap<>();
            jsonContent.put("creatorId", creator.getId());
            jsonContent.put("message", "creator-creation");

            try (FileWriter writer = new FileWriter(creatorFile)) {
                objectMapper.writeValue(writer, jsonContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventMarketInput creatorCreation = new EventMarketInput();
        creatorCreation.createCreators();
    }
}
