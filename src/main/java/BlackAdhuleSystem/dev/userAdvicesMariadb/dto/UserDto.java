package BlackAdhuleSystem.dev.userAdvicesMariadb.dto;

import lombok.*;

@Builder

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private boolean actif=false;
    private RoleDto roleDto;

}
