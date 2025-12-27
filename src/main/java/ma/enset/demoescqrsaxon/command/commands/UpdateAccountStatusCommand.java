package ma.enset.demoescqrsaxon.command.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ma.enset.demoescqrsaxon.commons.enums.AccountStatus;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter @AllArgsConstructor
public class UpdateAccountStatusCommand {
    @TargetAggregateIdentifier
    private String id;
    private AccountStatus accountStatus;
}
