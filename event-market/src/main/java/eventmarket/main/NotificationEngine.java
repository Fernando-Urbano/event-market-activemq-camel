package eventmarket.main;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationEngine {
    private String message;
    private String recipient;

    public NotificationEngine() {
        this.message = "";
        this.recipient = "";
    }

    private void setRecipient(User user){
        this.recipient = user.getEmail();
    }

    private void setRecipient(Creator creator){
        this.recipient = creator.getEmail();
    }

    public void userCreated(User user) {
        this.message = String.format(
            "🎉 Welcome to EventMarket, %s! 🎉 We're absolutely thrilled to have you with us. Start exploring the amazing events we have and dive into a world of excitement!",
            user.getName()
        );
        this.setRecipient(user);
    }

    public void creatorCreated(Creator creator) {
        this.message = String.format(
            "👋 Hello %s! Thank you for joining EventMarket as a creator. Start creating events and reach out to a wider audience. Let's make something incredible together!",
            creator.getName()
        );
        this.setRecipient(creator);
    }

    public void ticketPaymentApproved(User user, TicketRequest ticketRequest) {
        this.message = String.format(
            "✅ Hi %s, Your payment for the ticket type '%s' has been approved! Get ready to enjoy '%s'. See you there!",
            user.getName(), 
            ticketRequest.getTicketType().getName(), 
            ticketRequest.getTicketType().getEvent().getName()
        );
        this.setRecipient(user);
    }

    public void ticketPaymentRejected(User user, TicketRequest ticketRequest) {
        this.message = String.format(
            "❌ Hi %s, We're sorry to inform you that your payment for the ticket type '%s' for the event '%s' has been rejected. Please try again or contact support.",
            user.getName(), 
            ticketRequest.getTicketType().getName(), 
            ticketRequest.getTicketType().getEvent().getName()
        );
        this.setRecipient(user);
    }

    public void ticketPaymentInProcess(User user, TicketRequest ticketRequest) {
        this.message = String.format(
            "⏳ Hi %s, Your payment for the ticket type '%s' for the event '%s' is currently being processed. You will be notified once it's approved. Stay tuned!",
            user.getName(), 
            ticketRequest.getTicketType().getName(), 
            ticketRequest.getTicketType().getEvent().getName()
        );
        this.setRecipient(user);
    }

    public void eventRecommendation(User user, List<Event> events) {
        String eventList = events.stream()
            .map(event -> String.format("'%s' on %s", event.getName(), event.getStartTime()))
            .collect(Collectors.joining(", "));

        this.message = String.format(
            "🌟 Hi %s, based on your interests, we highly recommend the following events: %s. Don't miss out on these exciting opportunities!",
            user.getName(), eventList
        );
        this.setRecipient(user);
    }

    public void creatorEventUpdate(Creator creator, Event event) {
        this.message = String.format(
            "👏 Hello %s, Your event '%s' has been successfully updated! You've sold %d tickets so far. Keep up the fantastic work!",
            creator.getName(), event.getName(), event.getPurchaseCount()
        );
        this.setRecipient(creator);
    }

    public void eventCreated(Creator creator, Event event) {
        this.message = String.format(
            "🎉 Hi %s, Your event '%s' has been successfully created! Share it with the world and let the magic begin. 🌟",
            creator.getName(), event.getName()
        );
        this.setRecipient(creator);
    }

    public String getMessage() {
        return this.message;
    }

    public String getRecipient() {
        return this.recipient;
    }
}
