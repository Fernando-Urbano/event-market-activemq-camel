package eventmarket.main;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "ticket_request_id", nullable = false)
    private TicketRequest ticketRequest;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean checkedIn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public Ticket() {
    }

    public Ticket(TicketRequest ticketRequest) {
        this.ticketRequest = ticketRequest;
        this.checkedIn = false;
        this.user = ticketRequest.getUser();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TicketRequest getTicketRequest() {
        return ticketRequest;
    }

    public void setTicketRequest(TicketRequest ticketRequest) {
        this.ticketRequest = ticketRequest;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods

    public String display() {
        return String.format("Ticket ID: %d, Checked-In: %s", id, checkedIn ? "Yes" : "No");
    }

    public void setUser(User user) {
        this.user = user;
        if (!user.getTickets().contains(this)) {
            user.getTickets().add(this);
        }
        this.ticketRequest.setUser(user);
    }

    public User getUser() {
        return this.user;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) { // only set for new entities
            createdAt = new Date();
        }
    }
}
