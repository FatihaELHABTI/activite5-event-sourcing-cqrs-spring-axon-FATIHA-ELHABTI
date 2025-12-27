package ma.enset.demoescqrsaxon.commons.events;

public record AccountDebitedEvent(String accountId, double amount) {
}