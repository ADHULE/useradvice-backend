package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AuthentificationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.security.JwtService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // --------------------- INSCRIPTION ---------------------
    @PostMapping(path = "inscription")
    public ResponseEntity<UserDto> inscription(@RequestBody UserDto userDto) {
        UserDto savedUser = userService.createUser(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // --------------------- ACTIVATION ---------------------
    @PostMapping(path = "activation")
    public ResponseEntity<UserDto> activation(@RequestBody Map<String, String> activation) {
        try {
            UserDto activatedUser = userService.activation(activation);
            return ResponseEntity.ok(activatedUser);
        } catch (RuntimeException e) {
            log.warn("Erreur d'activation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // --------------------- LOGIN ---------------------
    @PostMapping(path = "login")
    public ResponseEntity<Map<String, String>> connexion(@RequestBody AuthentificationDto authentificationDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authentificationDto.email(),
                        authentificationDto.password()
                )
        );

        if (authentication.isAuthenticated()) {
            Map<String, String> tokenPayload = jwtService.generateToken(authentificationDto.email());
            return ResponseEntity.ok(tokenPayload);
        } else {
            log.warn("Échec d'authentification pour {}", authentificationDto.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // --------------------- LOGOUT ---------------------
    @PostMapping(path="logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        jwtService.deconnexion(authHeader);
        return ResponseEntity.noContent().build();
    }
}
