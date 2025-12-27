package ma.enset.demoescqrsaxon.query.repository;

import ma.enset.demoescqrsaxon.query.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}