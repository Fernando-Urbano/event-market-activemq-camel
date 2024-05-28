package eventmarketinput;

import eventmarket.main.Location;
import eventmarket.main.Creator;
import eventmarket.main.Event;
import eventmarket.main.TicketType;
import eventmarket.main.CreateDate;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
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

    public void createEvents() {
        em.getTransaction().begin();

        Creator creator1 = findCreatorByEmail("summerparties@music.com");
        Creator creator2 = findCreatorByEmail("classicmusic@concerts.com");
        Creator creator3 = findCreatorByEmail("charliefest@gmail.com");
        Creator creator4 = findCreatorByEmail("mainaccount@lollapaloza.com");

        Location location1 = new Location("Rua das Dalias", "Belo Horizonte", "56789", "MG", "Brazil");
        Location location2 = new Location("6126 S Ellis", "Chicago", "60637", "IL", "USA");

        em.persist(location1);
        em.persist(location2);

        createEventForCreator(creator1, location1, "Beach Bash", "A wild beach party with non-stop music and fun.", 100.0, 20.0);
        createEventForCreator(creator1, location1, "Sunset Rave", "Dance the night away as the sun sets over the horizon.", 150.0, 30.0);
        
        createEventForCreator(creator2, location2, "Symphony Under the Stars", "A magical night of classical music under the open sky.", 200.0, 40.0);
        createEventForCreator(creator2, location2, "Mozart Gala", "A grand celebration of Mozart's greatest works.", 250.0, 50.0);

        createEventForCreator(creator3, location1, "Rock 'n Roll Extravaganza", "A high-energy rock festival with top bands.", 120.0, 25.0);
        createEventForCreator(creator3, location1, "Jazz in the Park", "A soothing evening of jazz music in the park.", 180.0, 0.0);

        createEventForCreator(creator4, location1, "Lollapaloza Kickoff", "The biggest music festival of the year starts here.", 10.0, 0.0);
        createEventForCreator(creator4, location1, "Electro Carnival", "A vibrant electronic music festival with stunning visuals.", 20.0, 0.0);

        em.getTransaction().commit();

        em.close();
        emf.close();
    }

    private Creator findCreatorByEmail(String email) {
        TypedQuery<Creator> query = em.createQuery("SELECT c FROM Creator c WHERE c.email = :email", Creator.class);
        query.setParameter("email", email);
        return query.getSingleResult();
    }

    private void createEventForCreator(Creator creator, Location location, String eventName, String eventDescription, double vipPrice, double generalPrice) {
        Event event = new Event(
            eventName,
            location,
            CreateDate.createDate(2023, 6, 10, 12, 0, 0),
            CreateDate.createDate(2023, 6, 10, 18, 0, 0),
            eventDescription,
            "Don't miss " + eventName + "!"
        );
        event.setCreator(creator);

        TicketType vipTicket = new TicketType("VIP", "Exclusive access", 50, 18, vipPrice, event);
        TicketType generalTicket = new TicketType("General Admission", "General access", 200, 18, generalPrice, event);

        em.persist(event);
        em.persist(vipTicket);
        em.persist(generalTicket);

        writeFile(event);
    }

    private void writeFile(Event event) {
        try {
            File outboxDir = new File("data/outbox");
            if (!outboxDir.exists()) {
                outboxDir.mkdirs();
            }

            String formattedEventId = String.format("%010d", event.getId());
            File eventFile = new File(outboxDir, "event-" + formattedEventId + "-creation.json");

            Map<String, Object> jsonContent = new HashMap<>();
            jsonContent.put("eventId", event.getId());
            jsonContent.put("creatorId", event.getCreator().getId());
            jsonContent.put("message", "event-creation");

            try (FileWriter writer = new FileWriter(eventFile)) {
                objectMapper.writeValue(writer, jsonContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventMarketInput eventCreation = new EventMarketInput();
        eventCreation.createEvents();
    }
}
