package BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Validation;

public interface ValidationService {
    ValidationDto saveValidation(UserDto userDto);
    String generateCode();
    Validation readByCode(String code);

    void deleteValidation(Long id);
}
