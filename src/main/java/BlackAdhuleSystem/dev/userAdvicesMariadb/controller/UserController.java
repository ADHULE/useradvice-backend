package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AuthentificationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
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
    // --------------------- MISE À JOUR DU TOKEN ---------------------
    @PostMapping(path = "refresh-token")
    public ResponseEntity<Map<String, String>> refreshTokenRequest(@RequestBody Map<String, String> refreshtokenRequest) {

        try {

            Map<String, String> newTokens = jwtService.refreshToken(refreshtokenRequest);

            return ResponseEntity.ok(newTokens);

        } catch (RuntimeException e) {
            log.warn("Erreur lors du refresh token : {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour demander un changement de mot de passe.
     * L'utilisateur fournit son email et reçoit un code de validation par email.
     */
    @PostMapping(path = "change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> parameter) {
        userService.changePassword(parameter);
        return ResponseEntity.ok("Un code de validation a été envoyé à votre adresse email.");
    }


    /**
     * Endpoint pour définir un nouveau mot de passe.
     * L'utilisateur fournit son email, le code reçu et le nouveau mot de passe.
     */
    @PostMapping(path = "new-password")
    public ResponseEntity<String> newPassword(@RequestBody Map<String, String> parameter) {
        userService.newPassword(parameter);
        return ResponseEntity.ok("Votre mot de passe a été mis à jour avec succès.");
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
    @PostMapping(path = "logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        jwtService.deconnexion(authHeader);
        return ResponseEntity.noContent().build();
    }
}
