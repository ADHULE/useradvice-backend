package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.*;
import BlackAdhuleSystem.dev.userAdvicesMariadb.exceptions.CodeNotFoundException;
import BlackAdhuleSystem.dev.userAdvicesMariadb.mapper.UserMapper;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.RoleRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.UserRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.UserService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.ValidationService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.*;
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

        // 3. Assigner le rôle USER par défaut (recherche par nom de rôle "ROLE_USER")
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    newRole.setPrivileges(new HashSet<>()); // aucun privilege par défaut ici
                    return roleRepository.save(newRole);
                });

        // 4. Créer et sauvegarder l'utilisateur
        User user = UserMapper.toEntity(userDto);
        user.setRoles(Set.of(role));
        user.setActif(false); // l'utilisateur n’est pas encore activé
        User savedUser = userRepository.save(user);

        logger.info("Nouvel utilisateur créé : {}", savedUser.getEmail());

        // 5. Convertir en DTO pour la validation
        UserDto savedUserDto = UserMapper.toDto(savedUser);

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
            // Si validationService propose un save, appelez-le ici. Sinon ignorer.
        } catch (Exception ignored) {
        }

        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setFistname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setActif(dto.isActif());

        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Gère la demande d'un nouveau code pour la réinitialisation du mot de passe.
     * @param parameter Map contenant l'email de l'utilisateur.
     */
    @Override
    public void changePassword(Map<String, String> parameter) {
        String email = parameter == null ? null : parameter.get("email");
        if (email == null) throw new RuntimeException("Email requis");

        UserDetails userDetails = this.loadUserByUsername(email);
        User user = (User) userDetails; // safe cast if your User implements UserDetails
        UserDto userDto = UserMapper.toDto(user);

        // Générer et envoyer le code
        validationService.saveValidation(userDto);

        logger.info("Code de modification du mot de passe envoyé à {}", email);
    }


    /**
     * Gère la mise à jour du mot de passe après validation du code.
     * @param parameter Map contenant l'email, le code de validation et le nouveau mot de passe.
     */
    @Override
    public void newPassword(Map<String, String> parameter) {

        String email = parameter == null ? null : parameter.get("email");
        String code = parameter == null ? null : parameter.get("code");
        String newPassword = parameter == null ? null : parameter.get("password");

        if (email == null || code == null || newPassword == null) {
            throw new RuntimeException("Email, code et nouveau mot de passe sont requis");
        }

        UserDetails userDetails = this.loadUserByUsername(email);
        User user = (User) userDetails;

        // Charger la validation
        Validation validation = validationService.readByCode(code);

        // Vérifier que le code correspond bien à cet utilisateur
        if (validation.getUser() == null || !validation.getUser().getEmail().equals(email)) {
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

    /**
     * @return Liste de tous les utilisateurs (pour l'administration).
     */
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll() // récupère tous les utilisateurs en base
                .stream()
                .map(UserMapper::toDto) // convertit chaque User en UserDto
                .collect(Collectors.toList()); // retourne une liste de DTO
    }

    /**
     * Gère la demande d'un nouveau code d'activation pour un compte INACTIF.
     *
     * @param parameter Map contenant l'email de l'utilisateur.
     */
    @Override
    public void generateNewCode(Map<String, String> parameter) {
        String email = parameter == null ? null : parameter.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email est requis pour générer un nouveau code.");
        }

        // 1. Rechercher l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun compte trouvé pour l'email: " + email));

        // 2. Vérifier si le compte est déjà actif
        if (user.isActif()) {
            throw new RuntimeException("Le compte est déjà actif. Vous n'avez pas besoin d'un nouveau code d'activation.");
        }

        // 3. Mapper en DTO pour le service de validation
        UserDto userDto = UserMapper.toDto(user);

        try {
            // 4. Créer et envoyer le nouveau code d’activation.
            // La logique de 'saveValidation' devrait invalider l'ancien code s'il existe.
            validationService.saveValidation(userDto);
            logger.info("Nouveau code d'activation généré et envoyé à {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l’envoi du nouveau code de validation à {} : {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Échec de l'envoi du nouveau code de validation. Veuillez réessayer.");
        }
    }

    /**
     * Recherche un utilisateur par son email et retourne l'entité complète.
     * @param email L'adresse email de l'utilisateur.
     * @return L'entité User.
     * @throws UsernameNotFoundException si aucun utilisateur n'est trouvé.
     */
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur trouvé avec l'email : " + email));
    }
    // ---------------------------
    // Implémentation pour Spring Security
    // ---------------------------
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
    }
}