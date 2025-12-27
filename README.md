# 🏦 Demo Event Sourcing & CQRS avec Axon Framework

## 📋 Table des matières
- [Vue d'ensemble](#vue-densemble)
- [Technologies utilisées](#technologies-utilisées)
- [Architecture du projet](#architecture-du-projet)
- [Concepts clés](#concepts-clés)
- [Structure du projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Installation et exécution](#installation-et-exécution)
- [API Endpoints](#api-endpoints)
- [Exemples d'utilisation](#exemples-dutilisation)
- [Diagrammes](#diagrammes)

---

## 🎯 Vue d'ensemble

Ce projet est une implémentation complète d'une application bancaire utilisant les patterns **Event Sourcing** et **CQRS** (Command Query Responsibility Segregation) avec **Axon Framework**. Il permet de gérer des comptes bancaires avec des opérations de crédit, débit et changement de statut.

### Fonctionnalités principales
- ✅ Création de comptes bancaires
- 💰 Opérations de crédit et débit
- 🔄 Gestion des statuts de compte (CREATED, ACTIVATED, SUSPENDED, BLOCKED)
- 📊 Consultation de l'historique des opérations
- 🔍 Event Sourcing complet avec rejeu d'événements
- 📡 Streaming temps réel des événements

---

## 🛠 Technologies utilisées

### Framework et bibliothèques principales

#### **Spring Boot 3.2.5**
Framework Java pour créer des applications autonomes et prêtes pour la production. Il simplifie la configuration et le déploiement.

#### **Axon Framework 4.10.3**
Framework spécialisé pour implémenter CQRS et Event Sourcing. Il fournit:
- Command Bus pour router les commandes
- Event Bus pour publier et écouter les événements
- Event Store pour persister l'historique des événements
- Query Gateway pour gérer les requêtes

#### **PostgreSQL**
Base de données relationnelle pour la partie Query (lecture). Stocke l'état actuel des comptes et opérations.

#### **Spring Data JPA**
Abstraction pour interagir avec la base de données de manière orientée objet.

#### **Lombok**
Réduit le code boilerplate avec des annotations (@Getter, @Setter, @Builder, etc.).

#### **Project Reactor**
Bibliothèque pour la programmation réactive, utilisée pour le streaming temps réel des événements.

#### **SpringDoc OpenAPI**
Génération automatique de documentation API (Swagger UI).

---

## 🏗 Architecture du projet

### Pattern CQRS (Command Query Responsibility Segregation)

Le pattern CQRS sépare les opérations de **lecture** (Query) et d'**écriture** (Command) :

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT APPLICATION                    │
└────────────┬────────────────────────────┬────────────────┘
             │                            │
    ┌────────▼────────┐         ┌────────▼────────┐
    │   COMMAND SIDE  │         │   QUERY SIDE    │
    │   (Write Model) │         │   (Read Model)  │
    └────────┬────────┘         └────────▲────────┘
             │                            │
             │                            │
    ┌────────▼────────┐                  │
    │  Event Store    │──────Events──────┘
    │  (Axon Server)  │
    └─────────────────┘
```

### Event Sourcing

Au lieu de stocker uniquement l'état actuel, Event Sourcing stocke **tous les événements** qui ont modifié l'état:

```
État actuel = f(Event1, Event2, Event3, ..., EventN)
```

**Avantages**:
- Historique complet et auditable
- Possibilité de reconstruire l'état à n'importe quel moment
- Debugging facilité
- Analytics avancés

---

## 📁 Structure du projet

```
ma.enset.demoescqrsaxon/
│
├── command/                          # COMMAND SIDE (Écriture)
│   ├── aggregates/
│   │   └── AccountAggregate.java    # Aggregate Root - Logique métier
│   ├── commands/
│   │   ├── CreateAccountCommand.java
│   │   ├── CreditAccountCommand.java
│   │   ├── DebitAccountCommand.java
│   │   └── UpdateAccountStatusCommand.java
│   └── controllers/
│       └── AccountCommandController.java
│
├── query/                            # QUERY SIDE (Lecture)
│   ├── entities/
│   │   ├── Account.java             # Entité JPA
│   │   ├── Operation.java
│   │   └── OperationType.java
│   ├── repository/
│   │   ├── AccountRepository.java
│   │   └── OperationRepository.java
│   ├── handlers/
│   │   ├── AccountEventHandler.java # Écoute les événements
│   │   └── AccountQueryHandler.java # Traite les requêtes
│   ├── queries/
│   │   ├── GetAllAccounts.java
│   │   ├── GetAccountStatement.java
│   │   └── WatchEventQuery.java
│   ├── dtos/
│   │   └── AccountEvent.java
│   └── controllers/
│       └── AccountQueryController.java
│
└── commons/                          # Partagé entre Command et Query
    ├── events/
    │   ├── AccountCreatedEvent.java
    │   ├── AccountCreditedEvent.java
    │   ├── AccountDebitedEvent.java
    │   └── AccountStatusUpdatedEvent.java
    ├── dtos/
    │   ├── CreateAccountDTO.java
    │   ├── CreditAccountDTO.java
    │   ├── DebitAccountDTO.java
    │   └── UpdateAccountStatusDTO.java
    └── enums/
        └── AccountStatus.java
```

---

## 🔑 Concepts clés

### 1. Aggregate (AccountAggregate)

L'Aggregate est le cœur de la logique métier dans le pattern CQRS. Il:
- Reçoit des commandes
- Valide les règles métier
- Émet des événements
- Ne contient PAS de logique de persistance

```java
@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private double currentBalance;
    private String currency;
    private AccountStatus status;

    @CommandHandler
    public AccountAggregate(CreateAccountCommand command) {
        if (command.getInitialBalance() < 0) 
            throw new IllegalArgumentException("Balance négative");
        
        // Émettre un événement
        AggregateLifecycle.apply(new AccountCreatedEvent(
            command.getId(),
            command.getInitialBalance(),
            command.getCurrency(),
            AccountStatus.CREATED
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent event) {
        // Mettre à jour l'état interne
        this.accountId = event.accountId();
        this.currentBalance = event.initialBalance();
        this.currency = event.currency();
        this.status = event.accountStatus();
    }
}
```

### 2. Commands

Les commandes représentent l'**intention** de modifier l'état. Elles sont immutables.

```java
@Getter
@AllArgsConstructor
public class DebitAccountCommand {
    @TargetAggregateIdentifier
    private String id;
    private double amount;
}
```

### 3. Events

Les événements représentent des **faits** qui se sont produits. Ils sont immutables et utilisent des records Java.

```java
public record AccountDebitedEvent(String accountId, double amount) {}
```

### 4. Event Handlers

Les Event Handlers écoutent les événements et mettent à jour le modèle de lecture.

```java
@Service
public class AccountEventHandler {
    private AccountRepository accountRepository;
    
    @EventHandler
    public void on(AccountDebitedEvent event) {
        Account account = accountRepository.findById(event.accountId()).get();
        
        // Créer une opération
        Operation operation = Operation.builder()
            .amount(event.amount())
            .type(OperationType.DEBIT)
            .account(account)
            .build();
        operationRepository.save(operation);
        
        // Mettre à jour le solde
        account.setBalance(account.getBalance() - event.amount());
        accountRepository.save(account);
    }
}
```

### 5. Query Handlers

Les Query Handlers traitent les requêtes de lecture.

```java
@Service
public class AccountQueryHandler {
    @QueryHandler
    public List<Account> on(GetAllAccounts query) {
        return accountRepository.findAll();
    }
    
    @QueryHandler
    public AccountStatement on(GetAccountStatement query) {
        Account account = accountRepository.findById(query.getAccountId()).get();
        List<Operation> operations = operationRepository
            .findByAccountId(query.getAccountId());
        return new AccountStatement(account, operations);
    }
}
```

---

## ⚙️ Prérequis

- **Java 21** ou supérieur
- **Maven 3.8+**
- **PostgreSQL 12+**
- **Docker** (optionnel, pour Axon Server)

---

## 🚀 Installation et exécution

### Étape 1: Cloner le projet

```bash
git clone <repository-url>
cd demo-es-cqrs-axon
```

### Étape 2: Configurer PostgreSQL

Créer la base de données:

```sql
CREATE DATABASE accounts_db;
CREATE USER admin WITH PASSWORD '1234';
GRANT ALL PRIVILEGES ON DATABASE accounts_db TO admin;
```

### Étape 3: Configurer Axon Server (optionnel)

Si vous utilisez Axon Server, lancez-le avec Docker:

```bash
docker run -d --name axonserver \
  -p 8024:8024 \
  -p 8124:8124 \
  axoniq/axonserver
```

Accédez à l'interface: http://localhost:8024

### Étape 4: Configurer application.properties

Le fichier `src/main/resources/application.properties`:

```properties
spring.application.name=demo-es-cqrs-axon
server.port=8787

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/accounts_db
spring.datasource.username=admin
spring.datasource.password=1234
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

# Axon Serialization
axon.serializer.events=jackson
axon.serializer.messages=xstream
axon.serializer.general=jackson
```

### Étape 5: Compiler et lancer l'application

```bash
# Compiler
mvn clean install

# Lancer l'application
mvn spring-boot:run
```

L'application sera accessible sur: **http://localhost:8787**

### Étape 6: Accéder à Swagger UI

Documentation API interactive: **http://localhost:8787/swagger-ui.html**

---

## 📡 API Endpoints

### Command Side (Écriture)

#### 1. Créer un compte

```bash
POST http://localhost:8787/commands/accounts/create
Content-Type: application/json

{
  "initialBalance": 5000.0,
  "currency": "MAD"
}
```

**Réponse**: ID du compte créé (UUID)

#### 2. Débiter un compte

```bash
POST http://localhost:8787/commands/accounts/debit
Content-Type: application/json

{
  "accountId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 500.0
}
```

#### 3. Créditer un compte

```bash
POST http://localhost:8787/commands/accounts/credit
Content-Type: application/json

{
  "accountId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 1000.0
}
```

#### 4. Mettre à jour le statut

```bash
PUT http://localhost:8787/commands/accounts/updateStatus
Content-Type: application/json

{
  "accountId": "123e4567-e89b-12d3-a456-426614174000",
  "accountStatus": "ACTIVATED"
}
```

Statuts possibles: `CREATED`, `ACTIVATED`, `SUSPENDED`, `BLOCKED`

#### 5. Consulter l'Event Store

```bash
GET http://localhost:8787/commands/accounts/events/{accountId}
```

### Query Side (Lecture)

#### 1. Lister tous les comptes

```bash
GET http://localhost:8787/query/accounts/all
```

#### 2. Obtenir le relevé d'un compte

```bash
GET http://localhost:8787/query/accounts/statement/{accountId}
```

**Réponse**:
```json
{
  "account": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "balance": 5500.0,
    "currency": "MAD",
    "status": "ACTIVATED",
    "createdAt": "2024-12-27T10:30:00Z"
  },
  "operations": [
    {
      "id": 1,
      "date": "2024-12-27T10:35:00Z",
      "amount": 1000.0,
      "type": "CREDIT"
    },
    {
      "id": 2,
      "date": "2024-12-27T10:40:00Z",
      "amount": 500.0,
      "type": "DEBIT"
    }
  ]
}
```

#### 3. Streaming temps réel des événements

```bash
GET http://localhost:8787/query/accounts/watch/{accountId}
Accept: text/event-stream
```

Cette endpoint utilise Server-Sent Events (SSE) pour envoyer les événements en temps réel.

---

## 💡 Exemples d'utilisation

### Scénario complet avec curl

```bash
# 1. Créer un compte
ACCOUNT_ID=$(curl -X POST http://localhost:8787/commands/accounts/create \
  -H "Content-Type: application/json" \
  -d '{"initialBalance": 10000.0, "currency": "MAD"}' \
  | tr -d '"')

echo "Compte créé: $ACCOUNT_ID"

# 2. Activer le compte
curl -X PUT http://localhost:8787/commands/accounts/updateStatus \
  -H "Content-Type: application/json" \
  -d "{\"accountId\": \"$ACCOUNT_ID\", \"accountStatus\": \"ACTIVATED\"}"

# 3. Créditer le compte
curl -X POST http://localhost:8787/commands/accounts/credit \
  -H "Content-Type: application/json" \
  -d "{\"accountId\": \"$ACCOUNT_ID\", \"amount\": 5000.0}"

# 4. Débiter le compte
curl -X POST http://localhost:8787/commands/accounts/debit \
  -H "Content-Type: application/json" \
  -d "{\"accountId\": \"$ACCOUNT_ID\", \"amount\": 2000.0}"

# 5. Consulter le relevé
curl http://localhost:8787/query/accounts/statement/$ACCOUNT_ID

# 6. Voir l'historique des événements
curl http://localhost:8787/commands/accounts/events/$ACCOUNT_ID
```

---

## 🎨 Diagrammes

### Flux de création de compte

```
Client          Controller        CommandGateway      Aggregate         EventStore       EventHandler       Database
  |                 |                    |                |                 |                 |                |
  |--POST /create-->|                    |                |                 |                 |                |
  |                 |--CreateAccountCmd->|                |                 |                 |                |
  |                 |                    |--dispatch----->|                 |                 |                |
  |                 |                    |                |--validate------>|                 |                |
  |                 |                    |                |                 |                 |                |
  |                 |                    |                |--apply Event--->|                 |                |
  |                 |                    |                |                 |--store--------->|                |
  |                 |                    |                |                 |                 |                |
  |                 |                    |                |                 |--publish------->|                |
  |                 |                    |                |                 |                 |--save--------->|
  |                 |                    |                |                 |                 |                |
  |<--accountId-----|<--CompletableFuture|<---return------|                 |                 |                |
```

### Flux de requête

```
Client          Controller        QueryGateway       QueryHandler      Repository      Database
  |                 |                    |                |                 |                |
  |--GET /all------>|                    |                |                 |                |
  |                 |--GetAllAccountsQry>|                |                 |                |
  |                 |                    |--dispatch----->|                 |                |
  |                 |                    |                |--findAll()----->|                |
  |                 |                    |                |                 |--SELECT------->|
  |                 |                    |                |                 |<--results------|
  |                 |                    |                |<--List<Account>-|                |
  |<--List<Account>-|<--CompletableFuture|<---return------|                 |                |
```

---

## 🔧 Configuration avancée

### Personnaliser la stratégie de sérialisation

Dans `application.properties`:

```properties
# Jackson pour les événements (JSON)
axon.serializer.events=jackson

# XStream pour les messages (XML)
axon.serializer.messages=xstream

# Jackson pour la sérialisation générale
axon.serializer.general=jackson
```

### Configurer le pool de connexions PostgreSQL

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

---

## 🐛 Dépannage

### Problème: "Account not activated"

**Solution**: Assurez-vous d'activer le compte avant de faire des opérations:

```bash
curl -X PUT http://localhost:8787/commands/accounts/updateStatus \
  -H "Content-Type: application/json" \
  -d '{"accountId": "YOUR_ID", "accountStatus": "ACTIVATED"}'
```

### Problème: "Balance not sufficient"

**Solution**: Vérifiez le solde du compte avant de débiter:

```bash
curl http://localhost:8787/query/accounts/statement/YOUR_ID
```

### Problème: Axon Server non accessible

**Solution**: Vérifiez que le serveur est lancé sur le port 8124:

```bash
docker ps | grep axonserver
```

---

## 📚 Ressources supplémentaires

- [Documentation Axon Framework](https://docs.axoniq.io/)
- [CQRS Pattern - Martin Fowler](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

---

## 👥 Contribution

Les contributions sont les bienvenues! N'hésitez pas à ouvrir une issue ou une pull request.

---

## 📄 Licence

Ce projet est sous licence MIT.

---

**Développé avec ❤️ pour l'apprentissage de CQRS et Event Sourcing**
