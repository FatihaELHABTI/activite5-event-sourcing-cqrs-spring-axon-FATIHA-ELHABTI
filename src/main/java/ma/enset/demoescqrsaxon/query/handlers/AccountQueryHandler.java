package ma.enset.demoescqrsaxon.query.handlers;


import io.axoniq.axonserver.grpc.SerializedObject;
import io.axoniq.axonserver.grpc.event.Event;
import lombok.extern.slf4j.Slf4j;

import ma.enset.demoescqrsaxon.commons.dtos.AccountStatement;
import ma.enset.demoescqrsaxon.query.dtos.AccountEvent;
import ma.enset.demoescqrsaxon.query.entities.Account;
import ma.enset.demoescqrsaxon.query.entities.Operation;
import ma.enset.demoescqrsaxon.query.queries.GetAccountStatement;
import ma.enset.demoescqrsaxon.query.queries.GetAllAccounts;
import ma.enset.demoescqrsaxon.query.queries.WatchEventQuery;
import ma.enset.demoescqrsaxon.query.repository.AccountRepository;
import ma.enset.demoescqrsaxon.query.repository.OperationRepository;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccountQueryHandler {
    private AccountRepository accountRepository;
    private OperationRepository operationRepository;

    public AccountQueryHandler(AccountRepository accountRepository, OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }
    @QueryHandler
    public List<Account> on(GetAllAccounts query){
        return accountRepository.findAll();
    }
    @QueryHandler
    public AccountStatement on(GetAccountStatement query){
        Account account = accountRepository.findById(query.getAccountId()).get();
        List<Operation> operations = operationRepository.findByAccountId(query.getAccountId());
        return new AccountStatement(account, operations);
    }

    @QueryHandler
    public AccountEvent on(WatchEventQuery query){
        return AccountEvent.builder().build();
    }


}
