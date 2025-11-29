package BlackAdhuleSystem.dev.userAdvicesMariadb.mapper;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.RoleDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Privilege;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

public class RoleMapper {

    public static RoleDto toDto(Role role) {
        if (role == null) return null;

        return RoleDto.builder()
                .name(role.getName())
                .permissions(
                        role.getPrivileges()
                                .stream()
                                .map(Privilege::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    public static Role toEntity(RoleDto dto) {
        if (dto == null) return null;

        Role role = new Role();
        role.setName(dto.getName());

        // On convertit PermissionEnum -> Privilege
        Set<Privilege> privileges = dto.getPermissions()
                .stream()
                .map(p -> {
                    Privilege privilege = new Privilege();
                    privilege.setName(p);
                    return privilege;
                })
                .collect(Collectors.toSet());

        role.setPrivileges(privileges);
        return role;
    }
}
