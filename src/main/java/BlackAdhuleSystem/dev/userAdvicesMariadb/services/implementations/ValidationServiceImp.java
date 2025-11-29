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

    @Override
    public ValidationDto saveValidation(UserDto userDto) {

        if (userDto == null || userDto.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur ou son identifiant ne peut pas être nul.");
        }

        logger.info("Création d’un code de validation pour l’utilisateur ID {}", userDto.getId());

        // Vérifier utilisateur
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable avec ID : " + userDto.getId())
                );

        // Vérifier si un code actif existe déjà
        Optional<Validation> existing = validationRepository.findActiveValidationByUserId(user.getId());

        if (existing.isPresent()) {
            Validation v = existing.get();

            // Code expiré → suppression
            if (Instant.now().isAfter(v.getExpireTime())) {
                logger.warn("Code expiré détecté → suppression automatique (ID={})", v.getId());
                validationRepository.delete(v);
            } else {
                logger.warn("Un code actif existe déjà pour {}", user.getEmail());
                throw new ValidationAlreadyExistsException("Un code actif existe déjà. Veuillez attendre son expiration.");
            }
        }

        // Générer un nouveau code sécurisé
        String code = generateCode();

        // Nouvelle validation
        Validation validation = new Validation();
        validation.setUser(user);
        validation.setCode(code);
        validation.setCreationTime(Instant.now());
        validation.setExpireTime(Instant.now().plus(10, MINUTES));
        validation.setActivationTime(null);

        Validation saved = validationRepository.save(validation);

        logger.info("Code généré pour {} : {}", user.getEmail(), code);

        // Notifier l'utilisateur
        ValidationDto dto = ValidationMapper.mapToValidationDto(saved);
        notificationService.sendNotification(dto);

        return dto;
    }

    @Override
    public String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    @Override
    public Validation readByCode(String code) {
        return validationRepository.findByCode(code)
                .orElseThrow(() -> new CodeNotFoundException("Code de validation introuvable ou invalide."));
    }

    @Override
    public void deleteValidation(Long id) {
        if (!validationRepository.existsById(id)) {
            throw new CodeNotFoundException("Validation introuvable avec l’ID : " + id);
        }

        validationRepository.deleteById(id);
        logger.info("Validation ID {} supprimée avec succès", id);
    }
}
