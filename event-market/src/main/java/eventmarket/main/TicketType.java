package eventmarket.main;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "ticket_types")
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int maxNumberOfPeople;

    @Column(nullable = false)
    private int minimumAgeToAttend;

    @Column(nullable = false)
    private double price;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public TicketType() {
    }

    public TicketType(String name, String description, int maxNumberOfPeople, int minimumAgeToAttend, double price, Event event) {
        this.name = name;
        this.description = description;
        this.maxNumberOfPeople = maxNumberOfPeople;
        this.minimumAgeToAttend = minimumAgeToAttend;
        this.price = price;
        this.event = event;
        this.event.addTicketType(this);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxNumberOfPeople() {
        return maxNumberOfPeople;
    }

    public void setMaxNumberOfPeople(int maxNumberOfPeople) {
        this.maxNumberOfPeople = maxNumberOfPeople;
    }

    public int getMinimumAgeToAttend() {
        return minimumAgeToAttend;
    }

    public void setMinimumAgeToAttend(int minimumAgeToAttend) {
        this.minimumAgeToAttend = minimumAgeToAttend;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods

    public String display() {
        return String.format("Ticket Type: %s, Price: %.2f, Min Age: %d", name, price, minimumAgeToAttend);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) { // only set for new entities
            createdAt = new Date();
        }
    }
}
