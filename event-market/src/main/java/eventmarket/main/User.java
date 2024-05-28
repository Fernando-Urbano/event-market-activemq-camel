package eventmarket.main;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(name = "receives_emails", nullable = false)
    private boolean receivesEmails;

    @Column(name = "receives_pushs", nullable = false)
    private boolean receivesPushs;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketRequest> ticketRequests;

    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinancialInformation> financialInformation = new ArrayList<>();

    public User() {
        this.receivesEmails = true;
        this.receivesPushs = true;
    }

    public User(String name, String email, String password, Date birthDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.tickets = new ArrayList<>();
        this.ticketRequests = new ArrayList<>();
        this.financialInformation = new ArrayList<>();
        this.receivesEmails = true;
        this.receivesPushs = true;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getReceivesEmails() {
        return receivesEmails;
    }

    public void setReceivesEmails(boolean receivesEmails) {
        this.receivesEmails = receivesEmails;
    }

    public boolean getReceivesPushs() {
        return receivesPushs;
    }

    public void setReceivesPushs(boolean receivesPushs) {
        this.receivesPushs = receivesPushs;
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

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<TicketRequest> getTicketRequests() {
        return ticketRequests;
    }

    public void setTicketRequests(List<TicketRequest> ticketRequests) {
        this.ticketRequests = ticketRequests;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public List<FinancialInformation> getFinancialInformation() {
        return financialInformation;
    }
    
    public void setFinancialInformation(List<FinancialInformation> financialInformation) {
        this.financialInformation = financialInformation;
    }

    public void addFinancialInformation(FinancialInformation financialInformation) {
        this.financialInformation.add(financialInformation);
        financialInformation.setUser(this);
    }

    // Business methods

    public String display() {
        return String.format("User: %s, Email: %s", name, email);
    }

    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        Calendar now = Calendar.getInstance(); // Get the current date
        Calendar dob = Calendar.getInstance();
        dob.setTime(birthDate); // Set the date of birth

        int year1 = now.get(Calendar.YEAR);
        int year2 = dob.get(Calendar.YEAR);
        int age = year1 - year2; // Basic year subtraction

        int month1 = now.get(Calendar.MONTH);
        int month2 = dob.get(Calendar.MONTH);
        if (month2 > month1) {
            age--; // If birth month is greater than current month, decrement age
        } else if (month1 == month2) {
            int day1 = now.get(Calendar.DAY_OF_MONTH);
            int day2 = dob.get(Calendar.DAY_OF_MONTH);
            if (day2 > day1) {
                age--; // If birth day is greater than current day, decrement age
            }
        }
        return age;
    }

    public Recipient transformToRecipient() {
        return new Recipient(this.name, this.email, this.birthDate);
    }

    public void saveTicketRequest(TicketRequest ticketRequest) {
        ticketRequests.add(ticketRequest);
        ticketRequest.setUser(this);
    }

    public void receiveTicket(Ticket ticket) {
        tickets.add(ticket);

    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) { // only set for new entities
            createdAt = new Date();
        }
    }
}