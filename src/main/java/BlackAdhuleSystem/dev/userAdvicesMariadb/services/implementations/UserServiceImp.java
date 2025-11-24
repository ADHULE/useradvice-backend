package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.RoleType;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Validation;

import BlackAdhuleSystem.dev.userAdvicesMariadb.exceptions.CodeNotFoundException;
import BlackAdhuleSystem.dev.userAdvicesMariadb.mapper.UserMapper;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.RoleRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.UserRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.ValidationService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class UserServiceImp implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImp.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationService validationService;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto == null) throw new IllegalArgumentException("userDto ne peut pas être nul");

        // 1. Vérifier si l'email existe déjà
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Votre adresse email est déjà utilisée !");
        }

        // 2. Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);

        // 3. Assigner le rôle USER par défaut
        Role role = roleRepository.findByRoleType(RoleType.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleType(RoleType.USER);
                    return roleRepository.save(newRole);
                });

        // 4. Créer et sauvegarder l'utilisateur
        User user = UserMapper.mapToUser(userDto);
        user.setRole(role);
        user.setActif(false); // l'utilisateur n’est pas encore activé
        User savedUser = userRepository.save(user);

        logger.info("Nouvel utilisateur créé : {}", savedUser.getEmail());

        // 5. Convertir en DTO pour la validation
        UserDto savedUserDto = UserMapper.mapToUserDto(savedUser);

        try {
            // 6. Créer et envoyer le code d’activation
            validationService.saveValidation(savedUserDto);
            logger.info("Code d'activation envoyé à {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l’envoi du code de validation à {} : {}", savedUser.getEmail(), e.getMessage());
        }

        return savedUserDto;
    }

    @Override
    public UserDto activation(Map<String, String> activation) {
        String code = activation == null ? null : activation.get("code");
        if (code == null) throw new RuntimeException("Code d'activation requis");

        Validation validation;
        try {
            validation = validationService.readByCode(code);
        } catch (CodeNotFoundException e) {
            throw new RuntimeException("Code invalide");
        }

        if (Instant.now().isAfter(validation.getExpireTime())) {
            throw new RuntimeException("Votre code a expiré");
        }

        Long userId = validation.getUser() == null ? null : validation.getUser().getId();
        if (userId == null) {
            throw new RuntimeException("Validation associée à un utilisateur invalide");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setActif(true);
        userRepository.save(user);

        logger.info("Utilisateur {} activé avec succès", user.getEmail());

        // Optionnel: marquer la validation comme activée (activation time)
        try {
            validation.setActivationTime(Instant.now());
            // si validationService propose une méthode de save directe pour entity, l'utiliser sinon laisser comme est
            // ici on suppose qu'il y a un save derrière la repository si nécessaire
        } catch (Exception ignored) {
        }

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setActif(dto.isActif());

        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * @param parameter
     */
    @Override
    public void changePassword(Map<String, String> parameter) {
        String email = parameter.get("email");
        if (email == null) throw new RuntimeException("Email requis");

        User user = this.loadUserByUsername(email);
        UserDto userDto = UserMapper.mapToUserDto(user);

        // Générer et envoyer le code
        validationService.saveValidation(userDto);

        logger.info("Code de modification du mot de passe envoyé à {}", email);
    }


    /**
     * @param parameter
     */
    @Override
    public void newPassword(Map<String, String> parameter) {

        String email = parameter.get("email");
        String code = parameter.get("code");
        String newPassword = parameter.get("password");

        if (email == null || code == null || newPassword == null) {
            throw new RuntimeException("Email, code et nouveau mot de passe sont requis");
        }

        User user = this.loadUserByUsername(email);

        // Charger la validation
        Validation validation = validationService.readByCode(code);

        // Vérifier que le code correspond bien à cet utilisateur
        if (!validation.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Ce code ne correspond pas à cet utilisateur.");
        }

        // Vérifier expiration
        if (Instant.now().isAfter(validation.getExpireTime())) {
            throw new RuntimeException("Votre code a expiré.");
        }

        // Mettre à jour le mot de passe
        String cryptPassword = passwordEncoder.encode(newPassword);
        user.setPassword(cryptPassword);
        userRepository.save(user);

        logger.info("Mot de passe modifié avec succès pour {}", email);

        // Supprimer ou désactiver le code après utilisation
        validationService.deleteValidation(validation.getId());
    }


    // ---------------------------
    // Implémentation pour Spring Security
    // ---------------------------
    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));


    }
}
