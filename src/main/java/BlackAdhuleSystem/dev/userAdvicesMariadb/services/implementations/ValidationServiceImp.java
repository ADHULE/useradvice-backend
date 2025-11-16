package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Validation;

import BlackAdhuleSystem.dev.userAdvicesMariadb.exceptions.ValidationAlreadyExistsException;
import BlackAdhuleSystem.dev.userAdvicesMariadb.mapper.ValidationMapper;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.UserRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.ValidationRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.NotificationService;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.ValidationService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
@AllArgsConstructor
@Transactional
public class ValidationServiceImp implements ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImp.class);

    private final ValidationRepository validationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Crée une validation pour un utilisateur, envoie une notification, et retourne le DTO.
     */
    @Override
    public ValidationDto saveValidation(UserDto userDto) {
        if (userDto == null || userDto.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur ou son identifiant ne peut pas être nul.");
        }

        logger.info("Tentative de création de validation pour l'utilisateur ID {}", userDto.getId());

        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec l'ID : " + userDto.getId()));

        // Vérifier s'il a déjà une validation
        Optional<Validation> existing = validationRepository.findByUserId(user.getId());
        if (existing.isPresent()) {
            logger.warn("Validation déjà existante pour l'utilisateur ID {}", user.getId());
            throw new ValidationAlreadyExistsException("Cet utilisateur a déjà un code de validation actif.");
        }

        // Générer le code
        String code = generateCode();

        // Créer la validation
        Validation validation = new Validation();
        validation.setUser(user);
        validation.setCode(code);
        validation.setCreationTime(Instant.now());
        validation.setExpireTime(Instant.now().plus(10, MINUTES));
        validation.setActivationTime(null);

        // Sauvegarder
        Validation savedValidation = validationRepository.save(validation);
        logger.info("Validation créée avec succès pour l'utilisateur ID {}", user.getId());

        // Envoyer la notification
        ValidationDto validationDto = ValidationMapper.mapToValidationDto(savedValidation);
        notificationService.sendNotification(validationDto);
        logger.info("Notification envoyée à {}", user.getEmail());

        return validationDto;
    }

    /**
     * Génère un code à 6 chiffres sécurisé.
     */
    @Override
    public String generateCode() {
        SecureRandom secureRandom = new SecureRandom();
        int number = secureRandom.nextInt(900_000) + 100_000;
        return String.format("%06d", number);
    }

    /**
     * Recherche une validation par son code.
     *
     * @param code le code de validation fourni par l'utilisateur
     * @return l'entité Validation correspondante si trouvée
     * @throws ValidationAlreadyExistsException si aucun code n'est trouvé
     */
    @Override
    public Validation readByCode(String code) {
        return validationRepository.findByCode(code)
                .orElseThrow(() -> new ValidationAlreadyExistsException("Code de validation introuvable"));
    }


}
