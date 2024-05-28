package eventmarket.main;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date endTime;

    @Column(nullable = false)
    private int purchaseCount;

    @Column(nullable = false)
    private int checkedInCount;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketType> ticketTypes;

    @Column(nullable = false)
    private String publicDescription;

    @Column(nullable = false)
    private String publicTagline;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Transient
    private EntityManager entityManager;

    public Event() {
    }

    public Event(
            String name, Location location, Date startTime, Date endTime, 
            String publicDescription, String publicTagline
    ) {
        this.name = name;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ticketTypes = new ArrayList<>();
        this.publicDescription = publicDescription;
        this.publicTagline = publicTagline;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getCheckedInCount() {
        return checkedInCount;
    }

    public void setCheckedInCount(int checkedInCount) {
        this.checkedInCount = checkedInCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<TicketType> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }
    
    public void addTicketType(TicketType ticketType) {
        ticketTypes.add(ticketType);
        ticketType.setEvent(this);
    }

    public void removeTicketType(TicketType ticketType) {
        ticketTypes.remove(ticketType);
        ticketType.setEvent(null);
    }

    public String getPublicDescription() {
        return publicDescription;
    }

    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    public String getPublicTagline() {
        return publicTagline;
    }

    public void setPublicTagline(String publicTagline) {
        this.publicTagline = publicTagline;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Business methods

    public int getEventDurationInHours() {
        return (int) ((endTime.getTime() - startTime.getTime()) / 3600000);
    }

    public String getEventDuration() {
        int hours = getEventDurationInHours();
        int minutes = (int) ((endTime.getTime() - startTime.getTime()) / 60000) % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public String display() {
        return String.format("Event: %s, Location: %s, Start: %s, End: %s", name, location.display(), startTime, endTime);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) { // only set for new entities
            createdAt = new Date();
        }
    }

    public int getPurchaseCount() {
        if (entityManager == null) {
            return 0;
        }
        Long count = entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.ticketRequest.ticketType.event = :event", Long.class)
            .setParameter("event", this)
            .getSingleResult();
        return count.intValue();
    }
}
