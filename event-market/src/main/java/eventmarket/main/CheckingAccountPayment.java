package eventmarket.main;

import javax.persistence.*;

@Entity
@Table(name = "checking_account_payments")
@AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "checking_account_payment_id"))
})
public class CheckingAccountPayment extends PaymentMethod {
    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String routingNumber;

    public CheckingAccountPayment() {
    }

    public CheckingAccountPayment(String accountNumber, String routingNumber) {
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
    }

    // Getters and Setters

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    // Business methods

    public String display() {
        return String.format("Checking Account Payment: Account Number: %s, Routing Number: %s", accountNumber, routingNumber);
    }
}
