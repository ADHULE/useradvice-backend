package BlackAdhuleSystem.dev.userAdvicesMariadb.mapper;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Validation;

public class ValidationMapper {

    // 🔹 Mapper une entité Validation → DTO
    public static ValidationDto mapToValidationDto(Validation validation) {
        if (validation == null) return null;

        ValidationDto dto = new ValidationDto();
        dto.setId(validation.getId());
        dto.setCode(validation.getCode());
        dto.setCreationTime(validation.getCreationTime());
        dto.setActivationTime(validation.getActivationTime());
        dto.setExpireTime(validation.getExpireTime());

        // On convertit aussi l'utilisateur en DTO
        UserDto userDto = UserMapper.toDto(validation.getUser());
        dto.setUserDto(userDto);

        return dto;
    }

    //  Mapper un DTO  entité Validation
    public static Validation mapToValidation(ValidationDto dto) {
        if (dto == null) return null;

        Validation validation = new Validation();
        validation.setId(dto.getId());
        validation.setCode(dto.getCode());
        validation.setCreationTime(dto.getCreationTime());
        validation.setActivationTime(dto.getActivationTime());
        validation.setExpireTime(dto.getExpireTime());

        // Conversion du userDto en entité User
        User user = UserMapper.toEntity(dto.getUserDto());
        validation.setUser(user);

        return validation;
    }
}
