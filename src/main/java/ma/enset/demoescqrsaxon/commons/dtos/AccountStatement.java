package ma.enset.demoescqrsaxon.commons.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ma.enset.demoescqrsaxon.query.entities.Account;
import ma.enset.demoescqrsaxon.query.entities.Operation;

import java.util.List;
@AllArgsConstructor @Getter
public class AccountStatement {
    private Account account;
    private List<Operation> operations;
}