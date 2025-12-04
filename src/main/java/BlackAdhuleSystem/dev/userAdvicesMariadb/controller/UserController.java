package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AuthentificationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.security.JwtService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Arrays;
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

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping(path = "inscription")
    public ResponseEntity<UserDto> inscription(@RequestBody UserDto userDto) {
        UserDto savedUser = userService.createUser(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    /**
     * Activation du compte avec un code envoyé par email
     */
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

    /**
     * Générer un nouveau code d'activation
     */
    @PostMapping(path = "generate-new-code")
    public ResponseEntity<String> generateNewCode(@RequestBody Map<String, String> parameter) {
        userService.generateNewCode(parameter);
        return ResponseEntity.ok("Un nouveau code a été envoyé à votre email.");
    }

    /**
     * Connexion utilisateur : génère un access token et un refresh token
     * - Access token renvoyé dans le corps JSON
     * - Refresh token placé dans un cookie HttpOnly
     */
    @PostMapping(path = "login")
    public ResponseEntity<Map<String, String>> connexion(@RequestBody AuthentificationDto authentificationDto,
                                                         HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authentificationDto.email(),
                        authentificationDto.password()
                )
        );

        if (authentication.isAuthenticated()) {
            // Générer access et refresh token
            Map<String, String> tokenPayload = jwtService.generateToken(authentificationDto.email());
            String accessToken = tokenPayload.get("token");
            String refreshToken = tokenPayload.get("refresh");

            // Placer le refresh token dans un cookie HttpOnly
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true) // mettre true en prod avec HTTPS
                    .path("/refresh-token") // endpoint de refresh
                    .maxAge(7 * 24 * 60 * 60) // 7 jours
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Retourner access token et expiration
            return ResponseEntity.ok(Map.of(
                    "token", accessToken,
                    "expiresAt", Instant.now().plusSeconds(900).toString() // 15 min
            ));
        } else {
            log.warn("Échec d'authentification pour {}", authentificationDto.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Endpoint de refresh : lit le refresh token depuis le cookie HttpOnly
     */
    @PostMapping(path = "refresh-token")
    public ResponseEntity<Map<String, String>> refreshTokenRequest(HttpServletRequest request) {
        try {
            // Récupérer le cookie refreshToken
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .findFirst()
                    .map(c -> c.getValue())
                    .orElse(null);

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token manquant"));
            }

            // Générer un nouveau access token
            String newAccessToken = jwtService.generateAccessTokenFromRefresh(refreshToken);

            return ResponseEntity.ok(Map.of("token", newAccessToken));
        } catch (RuntimeException e) {
            log.warn("Erreur lors du refresh token : {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Déconnexion : invalide le token
     */
    @PostMapping(path = "logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        jwtService.deconnexion(authHeader);
        return ResponseEntity.noContent().build();
    }

    /**
     * Changement de mot de passe
     */
    @PostMapping(path = "change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> parameter) {
        userService.changePassword(parameter);
        return ResponseEntity.ok("Un code de validation a été envoyé à votre adresse email.");
    }

    /**
     * Définir un nouveau mot de passe
     */
    @PostMapping(path = "new-password")
    public ResponseEntity<String> newPassword(@RequestBody Map<String, String> parameter) {
        userService.newPassword(parameter);
        return ResponseEntity.ok("Votre mot de passe a été mis à jour avec succès.");
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
