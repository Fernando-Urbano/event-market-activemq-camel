package eventmarket.main;

import java.util.List;

public abstract class AbstractRecommenderModel {
    protected int id;
    protected User user;

    public abstract List<Event> recommend(int n);
    public abstract List<Event> recommend();

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
}
