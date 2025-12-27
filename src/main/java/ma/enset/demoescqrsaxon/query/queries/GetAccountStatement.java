package ma.enset.demoescqrsaxon.query.queries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class GetAccountStatement {
    private String accountId;
}