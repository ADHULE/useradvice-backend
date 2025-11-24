package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.UserDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Validation;
import BlackAdhuleSystem.dev.userAdvicesMariadb.exceptions.CodeNotFoundException;
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
     * Crée un nouveau code de validation pour un utilisateur (code à 6 chiffres),
     * supprime tout ancien code actif pour éviter les conflits,
     * sauvegarde le nouveau code et envoie une notification à l'utilisateur.
     */
    @Override
    public ValidationDto saveValidation(UserDto userDto) {

        if (userDto == null || userDto.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur ou son identifiant ne peut pas être nul.");
        }

        logger.info("Création du code de validation pour l'utilisateur ID {}", userDto.getId());

        // Vérification d'existence utilisateur
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable avec ID : " + userDto.getId())
                );

        // Vérifier s'il existe déjà un code actif (non expiré)
        Optional<Validation> existing = validationRepository.findActiveValidationByUserId(user.getId());
        if (existing.isPresent()) {
            Validation v = existing.get();

            // Vérifier si expiré → auto-suppression
            if (Instant.now().isAfter(v.getExpireTime())) {
                logger.warn("Ancien code expiré détecté → suppression automatique (ID={})", v.getId());
                validationRepository.delete(v);
            } else {
                logger.warn("Un code de validation actif existe déjà pour l'utilisateur {}", user.getEmail());
                throw new ValidationAlreadyExistsException("Un code actif existe déjà. Veuillez attendre son expiration.");
            }
        }

        // Générer un nouveau code
        String code = generateCode();

        // Création validation
        Validation validation = new Validation();
        validation.setUser(user);
        validation.setCode(code);
        validation.setCreationTime(Instant.now());
        validation.setExpireTime(Instant.now().plus(10, MINUTES));
        validation.setActivationTime(null);

        // Sauvegarde
        Validation savedValidation = validationRepository.save(validation);

        logger.info("Nouveau code de validation créé pour {} : {}", user.getEmail(), code);

        // Conversion → DTO
        ValidationDto validationDto = ValidationMapper.mapToValidationDto(savedValidation);

        // Notification (email, SMS…)
        notificationService.sendNotification(validationDto);
        logger.info("Notification envoyée à {}", user.getEmail());

        return validationDto;
    }

    /**
     * Génère un code numérique sécurisé à 6 chiffres.
     */
    @Override
    public String generateCode() {
        SecureRandom secureRandom = new SecureRandom();
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    /**
     * Recherche une validation par code.
     */
    @Override
    public Validation readByCode(String code) {
        return validationRepository.findByCode(code)
                .orElseThrow(() -> new CodeNotFoundException("Code de validation introuvable ou invalide."));
    }

    /**
     * Supprime une validation par ID.
     */
    @Override
    public void deleteValidation(Long id) {
        if (!validationRepository.existsById(id)) {
            throw new CodeNotFoundException("Validation introuvable avec l'ID : " + id);
        }

        validationRepository.deleteById(id);
        logger.info("Validation ID {} supprimée avec succès", id);
    }
}
