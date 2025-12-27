package ma.enset.demoescqrsaxon.commons.events;

import ma.enset.demoescqrsaxon.commons.enums.AccountStatus;

public record AccountCreatedEvent(String accountId, double initialBalance, String currency, AccountStatus accountStatus) {
}
