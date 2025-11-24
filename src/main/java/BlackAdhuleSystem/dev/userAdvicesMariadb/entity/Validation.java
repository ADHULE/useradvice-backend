package BlackAdhuleSystem.dev.userAdvicesMariadb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "validations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Validation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private Instant creationTime;
    private Instant activationTime;
    private Instant expireTime;
    @ManyToOne(cascade = {CascadeType.MERGE,CascadeType.DETACH})
    private User user;
}
