package ma.enset.demoescqrsaxon.query.handlers;


import io.axoniq.axonserver.grpc.event.Event;
import lombok.extern.slf4j.Slf4j;

import ma.enset.demoescqrsaxon.commons.events.AccountCreatedEvent;
import ma.enset.demoescqrsaxon.commons.events.AccountCreditedEvent;
import ma.enset.demoescqrsaxon.commons.events.AccountDebitedEvent;
import ma.enset.demoescqrsaxon.commons.events.AccountStatusUpdatedEvent;
import ma.enset.demoescqrsaxon.query.dtos.AccountEvent;
import ma.enset.demoescqrsaxon.query.entities.Account;
import ma.enset.demoescqrsaxon.query.entities.Operation;
import ma.enset.demoescqrsaxon.query.entities.OperationType;
import ma.enset.demoescqrsaxon.query.repository.AccountRepository;
import ma.enset.demoescqrsaxon.query.repository.OperationRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountEventHandler {
    private AccountRepository accountRepository;
    private OperationRepository operationRepository;
    private QueryUpdateEmitter queryUpdateEmitter;

    public AccountEventHandler(AccountRepository accountRepository, OperationRepository operationRepository, QueryUpdateEmitter queryUpdateEmitter) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }



    @EventHandler
    public void on(AccountCreatedEvent event, EventMessage eventMessage){
        log.info("################# AccountCreatedEvent ################");
        Account account = Account.builder()
                .id(event.accountId())
                .balance(event.initialBalance())
                .currency(event.currency())
                .status(event.accountStatus())
                .createdAt(eventMessage.getTimestamp())
                .build();
        accountRepository.save(account);
        AccountEvent accountEvent = AccountEvent.builder()
                .type(event.getClass().getSimpleName())
                .accountId(account.getId())
                .balance(account.getBalance())
                .amount(event.initialBalance())
                .status(account.getStatus().toString())
                .build();
        queryUpdateEmitter.emit(e->true, accountEvent);

    }

    @EventHandler
    public void on(AccountStatusUpdatedEvent event, EventMessage eventMessage){
        log.info("################# AccountStatusUpdatedEvent ################");
        Account account = accountRepository.findById(event.accountId()).get();
        account.setStatus(event.toStatus());
        accountRepository.save(account);

        AccountEvent accountEvent = AccountEvent.builder()
                .type(event.getClass().getSimpleName())
                .accountId(account.getId())
                .balance(account.getBalance())
                .amount(0)
                .status(account.getStatus().toString())
                .build();
        queryUpdateEmitter.emit(e->true, accountEvent);

    }
    @EventHandler
    public void on(AccountDebitedEvent event, EventMessage eventMessage){
        log.info("################# AccountDebitedEvent ################");
        Account account = accountRepository.findById(event.accountId()).get();
        Operation operation = Operation.builder()
                .date(eventMessage.getTimestamp())
                .amount(event.amount())
                .type(OperationType.DEBIT)
                .account(account)
                .build();
        Operation savedOperation = operationRepository.save(operation);
        account.setBalance(account.getBalance()-operation.getAmount());
        accountRepository.save(account);
        AccountEvent accountEvent = AccountEvent.builder()
                .type(event.getClass().getSimpleName())
                .accountId(account.getId())
                .balance(account.getBalance())
                .amount(event.amount())
                .status(account.getStatus().toString())
                .build();
        queryUpdateEmitter.emit(e->true, accountEvent);

    }
    @EventHandler
    public void on(AccountCreditedEvent event, EventMessage eventMessage){
        log.info("################# AccountCreditedEvent ################");
        Account account = accountRepository.findById(event.accountId()).get();
        Operation operation = Operation.builder()
                .date(eventMessage.getTimestamp())
                .amount(event.amount())
                .type(OperationType.CREDIT)
                .account(account)
                .build();
        Operation savedOperation = operationRepository.save(operation);
        account.setBalance(account.getBalance()+operation.getAmount());
        accountRepository.save(account);
        AccountEvent accountEvent = AccountEvent.builder()
                .type(event.getClass().getSimpleName())
                .accountId(account.getId())
                .balance(account.getBalance())
                .amount(event.amount())
                .status(account.getStatus().toString())
                .build();
        queryUpdateEmitter.emit(e->true, accountEvent);

    }


}