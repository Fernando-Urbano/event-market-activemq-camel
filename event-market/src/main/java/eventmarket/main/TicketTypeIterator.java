package eventmarket.main;

import java.util.List;

public class TicketTypeIterator extends AbstractIterator<TicketType> {
    private Event event;
    private int currentIndex;
    private List<TicketType> ticketTypes;

    public TicketTypeIterator(Event event) {
        this.event = event;
        this.ticketTypes = event.getTicketTypes();
        this.currentIndex = 0;
    }

    @Override
    public TicketType next() {
        if (isDone()) {
            throw new IllegalStateException("No more ticket types");
        }
        TicketType ticketType = ticketTypes.get(currentIndex);
        currentIndex++;
        return ticketType;
    }

    @Override
    public int getSteps() {
        return currentIndex;
    }

    @Override
    public void setSteps(int step) {
        if (step < 0 || step >= ticketTypes.size()) {
            throw new IllegalArgumentException("Invalid step value");
        }
        this.currentIndex = step;
    }

    @Override
    public boolean isDone() {
        return currentIndex >= ticketTypes.size();
    }

    @Override
    public TicketType current() {
        if (isDone()) {
            throw new IllegalStateException("No more elements");
        }
        return ticketTypes.get(currentIndex);
    }

    @Override
    public TicketType first() {
        currentIndex = 0;
        return ticketTypes.get(currentIndex);
    }
}