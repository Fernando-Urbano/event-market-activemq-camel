package eventmarket.main;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NlpRecommenderModel extends AbstractRecommenderModel {

    private User user;

    @PersistenceContext
    private EntityManager entityManager;

    public NlpRecommenderModel(User user) {
        this.user = user;
    }

    @Override
    public List<Event> recommend(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Invalid value for n");
        }
        return findEventsBasedOnNlpAnalysis(n);
    }

    @Override public List<Event> recommend() {
        return findEventsBasedOnNlpAnalysis(3);
    }

    private List<Event> findEventsBasedOnNlpAnalysis(int n) {
        List<Event> allEvents = fetchAllEvents();
        List<Event> userEvents = fetchUserEvents();

        allEvents.removeAll(userEvents);
        return selectRandomEvents(allEvents, n);
    }

    private List<Event> fetchAllEvents() {
        return entityManager.createQuery("SELECT e FROM Event e", Event.class).getResultList();
    }

    private List<Event> fetchUserEvents() {
        List<Event> userEvents = new ArrayList<>();

        List<Ticket> userTickets = entityManager.createQuery(
                "SELECT t FROM Ticket t WHERE t.user = :user", Ticket.class)
                .setParameter("user", user)
                .getResultList();
        for (Ticket ticket : userTickets) {
            userEvents.add(ticket.getTicketRequest().getTicketType().getEvent());
        }

        List<TicketRequest> userTicketRequests = entityManager.createQuery(
                "SELECT tr FROM TicketRequest tr WHERE tr.user = :user", TicketRequest.class)
                .setParameter("user", user)
                .getResultList();
        for (TicketRequest ticketRequest : userTicketRequests) {
            userEvents.add(ticketRequest.getTicketType().getEvent());
        }

        return userEvents;
    }

    private List<Event> selectRandomEvents(List<Event> events, int count) {
        Random rand = new Random();
        List<Event> selectedEvents = new ArrayList<>();
        for (int i = 0; i < count && !events.isEmpty(); i++) {
            int randomIndex = rand.nextInt(events.size());
            selectedEvents.add(events.remove(randomIndex));
        }
        return selectedEvents;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
