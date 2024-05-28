package eventmarket.main;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "creators")
public class Creator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> eventsCreated;

    public Creator() {
    }

    public Creator(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.eventsCreated = new ArrayList<>();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public List<Event> getEventsCreated() {
        return eventsCreated;
    }

    public void setEventsCreated(List<Event> eventsCreated) {
        this.eventsCreated = eventsCreated;
    }

    public void addEvent(Event event) {
        eventsCreated.add(event);
        event.setCreator(this);
    }

    public void removeEvent(Event event) {
        eventsCreated.remove(event);
        event.setCreator(null);
    }

    // Business methods

    public String display() {
        return String.format("Creator: %s, Email: %s", name, email);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) { // only set for new entities
            createdAt = new Date();
        }
    }
}
