package eventmarket.main;

import java.util.ArrayList;
import java.util.List;

public class FinancialValidator {
    private static FinancialValidator instance;
    private List<Integer> recentlyCheckedRequestIds;

    private FinancialValidator() {
        recentlyCheckedRequestIds = new ArrayList<>();
    }

    public static FinancialValidator getInstance() {
        if (instance == null) {
            instance = new FinancialValidator();
        }
        return instance;
    }

    public boolean requestValidation(TicketRequest ticketRequest) {
        if (isRecentlyChecked(ticketRequest.getId())) {
            return false;
        }
        addRecentlyCheckedRequestId(ticketRequest.getId());
        return true;
    }

    public void addRecentlyCheckedRequestId(int ticketRequestId) {
        recentlyCheckedRequestIds.add(ticketRequestId);
    }

    public boolean isRecentlyChecked(int ticketRequestId) {
        return recentlyCheckedRequestIds.contains(ticketRequestId);
    }
}
