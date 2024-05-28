package eventmarket.main;

import java.util.ArrayList;
import java.util.List;

public class EventRecommender {
    private int id;
    private User user;
    private AbstractRecommenderModel model;
    private int numberRecommendations;
    private List<Event> eventRecommendations;

    public EventRecommender(AbstractRecommenderModel model) {
        this.user = model.getUser();
        this.model = model;
        this.numberRecommendations = 3;
        this.eventRecommendations = new ArrayList<>();
    }

    public List<Event> sendRecommendation() {
        return eventRecommendations;
    }

    public void setNumberRecommendations(int numberRecommendations) {
        this.numberRecommendations = numberRecommendations;
    }

    public int getNumberRecommendations() {
        return numberRecommendations;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AbstractRecommenderModel getModel() {
        return model;
    }

    public void setModel(AbstractRecommenderModel model) {
        this.model = model;
    }

    public void requestRecommendation() {
        eventRecommendations = model.recommend(numberRecommendations);
    }

}
