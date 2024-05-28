package eventmarket.main;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "ticket_requests")
public class TicketRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @OneToOne
    @JoinColumn(name = "financial_information_id", nullable = true)
    private FinancialInformation financialInformation;

    @Column(nullable = false)
    private boolean approved;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public TicketRequest() {
    }

    public TicketRequest(Recipient recipient, FinancialInformation financialInformation, TicketType ticketType) {
        this.recipient = recipient;
        this.financialInformation = financialInformation;
        this.ticketType = ticketType;
        this.approved = false;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        // Ensure the user's ticketRequests collection includes this TicketRequest
        if (!user.getTicketRequests().contains(this)) {
            user.getTicketRequests().add(this);
        }
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public FinancialInformation getFinancialInformation() {
        return financialInformation;
    }

    public void setFinancialInformation(FinancialInformation financialInformation) {
        this.financialInformation = financialInformation;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods

    public String display() {
        return String.format("TicketRequest: %d, Approved: %s", id, approved ? "Yes" : "No");
    }

    public Ticket transformToTicket() {
        if (approved) {
            return new Ticket(this);
        }
        return null;
    }

    public void approveRequest() {
        this.approved = true;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
}