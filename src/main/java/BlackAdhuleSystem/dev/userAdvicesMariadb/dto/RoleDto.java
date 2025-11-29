package BlackAdhuleSystem.dev.userAdvicesMariadb.dto;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.PermissionEnum;
import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Data
public class RoleDto {
    private String name;
    private Set<PermissionEnum> permissions;
}
