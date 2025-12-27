package ma.enset.demoescqrsaxon.query.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Operation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant date;
    private double amount;
    @Enumerated(EnumType.STRING)
    private OperationType type;
    @ManyToOne
    private Account account;
}
