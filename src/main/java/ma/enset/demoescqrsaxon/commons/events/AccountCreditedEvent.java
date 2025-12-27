package ma.enset.demoescqrsaxon.commons.events;

public record AccountCreditedEvent(String accountId, double amount) {
}