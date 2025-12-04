package BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Validation;

import java.util.List;
import java.util.Map;
public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto activation(Map<String, String> activation);

    List<UserDto> getUsers();
    UserDto getUserById(Long userId);
    UserDto updateUser(Long userId, UserDto userDto);
    void deleteUser(Long userId);


    // Modifier mot de passe de l'utilisateur connecté
    void changePassword(Map<String,String> parameter) ;
    void newPassword(Map<String,String> parameter);


    List<UserDto> getAllUsers();

    void generateNewCode(Map<String, String> parameter);
}
