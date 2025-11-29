package BlackAdhuleSystem.dev.userAdvicesMariadb.mapper;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.PrivilegeDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Privilege;

public class PrivilegeMapper {

    public static PrivilegeDto toDto(Privilege privilege) {
        if (privilege == null) return null;

        PrivilegeDto dto = new PrivilegeDto();
        dto.setName(privilege.getName());
        return dto;
    }

    public static Privilege toEntity(PrivilegeDto dto) {
        if (dto == null) return null;

        Privilege privilege = new Privilege();
        privilege.setName(dto.getName());
        return privilege;
    }
}
