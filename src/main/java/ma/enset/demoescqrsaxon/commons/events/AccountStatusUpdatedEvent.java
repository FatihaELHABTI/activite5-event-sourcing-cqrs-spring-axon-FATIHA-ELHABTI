package ma.enset.demoescqrsaxon.commons.events;

import ma.enset.demoescqrsaxon.commons.enums.AccountStatus;

public record AccountStatusUpdatedEvent(String accountId, AccountStatus fromStatus, AccountStatus toStatus) {
}