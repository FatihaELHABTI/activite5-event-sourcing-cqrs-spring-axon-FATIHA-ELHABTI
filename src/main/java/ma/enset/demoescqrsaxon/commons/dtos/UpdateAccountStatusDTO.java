package ma.enset.demoescqrsaxon.commons.dtos;

import ma.enset.demoescqrsaxon.commons.enums.AccountStatus;

public record UpdateAccountStatusDTO(String accountId, AccountStatus accountStatus) {
}
