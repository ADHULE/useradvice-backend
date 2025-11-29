package BlackAdhuleSystem.dev.userAdvicesMariadb.dto;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.PermissionEnum;
import lombok.Data;

@Data
public class PrivilegeDto {
    private PermissionEnum name;

}
