package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.UserRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping(path = "inscription")
    public ResponseEntity<UserDto> inscruption(@RequestBody UserDto userDto) {
        UserDto savedUser = this.userService.createUser(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

    }
    @PostMapping(path = "activation")
    public ResponseEntity<UserDto> activation(@RequestBody Map<String, String> activation) {
        try {
            UserDto activatedUser = userService.activation(activation);
            return ResponseEntity.ok(activatedUser);
        } catch (RuntimeException e) {
            // Gestion des erreurs métier (code invalide ou expiré)
            return ResponseEntity.badRequest().body(null);
        }
    }

}
