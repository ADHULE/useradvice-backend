package BlackAdhuleSystem.dev.userAdvicesMariadb.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jwts")
public class Jwt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String value;
    private boolean desactive;
    private boolean expire;
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE})
    private User user;
}

