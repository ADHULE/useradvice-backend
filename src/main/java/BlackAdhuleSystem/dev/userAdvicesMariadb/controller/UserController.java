package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AuthentificationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.security.JwtService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // --------------------- AUTHENTIFICATION ---------------------

    @PostMapping(path = "inscription")
    public ResponseEntity<UserDto> inscription(@RequestBody UserDto userDto) {
        UserDto savedUser = userService.createUser(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

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

    @PostMapping(path = "change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> parameter) {
        userService.changePassword(parameter);
        return ResponseEntity.ok("Un code de validation a été envoyé à votre adresse email.");
    }

    @PostMapping(path = "new-password")
    public ResponseEntity<String> newPassword(@RequestBody Map<String, String> parameter) {
        userService.newPassword(parameter);
        return ResponseEntity.ok("Votre mot de passe a été mis à jour avec succès.");
    }

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

    @PostMapping(path = "logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        jwtService.deconnexion(authHeader);
        return ResponseEntity.noContent().build();
    }

    // --------------------- UTILISATEUR CONNECTÉ ---------------------

    @GetMapping(path = "users/me")
    public ResponseEntity<UserDto> getMyDetails(@AuthenticationPrincipal UserDto user) {
        UserDto userDto = userService.getUserById(user.getId());
        return (userDto != null) ? ResponseEntity.ok(userDto) : ResponseEntity.notFound().build();
    }

    @PutMapping(path = "users/me")
    public ResponseEntity<UserDto> updateMyDetails(@AuthenticationPrincipal UserDto user,
                                                   @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(user.getId(), userDto);
        return (updatedUser != null) ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @DeleteMapping(path = "users/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDto user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.noContent().build();
    }

    // --------------------- PARTIE ADMIN ---------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
