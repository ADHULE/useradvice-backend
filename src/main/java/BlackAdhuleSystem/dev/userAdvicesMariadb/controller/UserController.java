package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AuthentificationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.UserRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.security.JwtService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    @PostMapping(path = "login")
    public Map<String, String> connexion(@RequestBody AuthentificationDto authentificationDto) {
        final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authentificationDto.username(),
                authentificationDto.password())
        );
//        si l'utilisateur est authentifié alors récuperer son nom dans la base de données
        if (authenticate.isAuthenticated()) {
          return this.jwtService.generateToken(authentificationDto.username());
        }
        return null;
    }

}
