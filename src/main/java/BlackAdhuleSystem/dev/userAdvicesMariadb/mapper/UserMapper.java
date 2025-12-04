package BlackAdhuleSystem.dev.userAdvicesMariadb.mapper;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstname(user.getFistname());
        dto.setLastname(user.getLastname());
        dto.setGender(user.getGender());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setEmail(user.getEmail());
        dto.setPassword(null); //  On ne renvoie jamais le mot de passe
        dto.setActif(user.isActif());
        dto.setCreatedAt(user.getCreatedAt());

        // Un user n'a pas un set de rôle dans votre DTO → on prend le premier
        user.getRoles().stream().findFirst()
                .ifPresent(role -> dto.setRoleDto(RoleMapper.toDto(role)));

        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setFistname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setActif(dto.isActif());
        user.setCreatedAt(dto.getCreatedAt());

        if (dto.getRoleDto() != null) {
            Role role = RoleMapper.toEntity(dto.getRoleDto());
            user.getRoles().add(role);
        }

        return user;
    }
}
