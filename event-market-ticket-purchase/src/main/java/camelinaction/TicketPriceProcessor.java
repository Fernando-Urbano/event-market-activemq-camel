package camelinaction;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class TicketPriceProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = exchange.getIn().getBody(Map.class);

        Double ticketPrice = (Double) body.get("ticketPrice");

        if (ticketPrice == null) {
            body.put("validPrice", false);
            body.put("positivePrice", false);
        } else if (ticketPrice <= 0) {
            body.put("validPrice", true);
            body.put("positivePrice", false);
        } else {
            body.put("validPrice", true);
            body.put("positivePrice", true);
        }
        exchange.getIn().setBody(body);
    }
}
