package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.ValidationDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImp implements NotificationService {

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Envoie un email de notification contenant le code d'activation.
     *
     * @param validationDto les données de validation à notifier
     * @return le même DTO, après envoi
     */
    @Override
    public ValidationDto sendNotification(ValidationDto validationDto) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("black_adhule_system@gmail.com");
        mailMessage.setTo(validationDto.getUserDto().getEmail());
        mailMessage.setSubject("Votre code d'activation");

        String text = String.format(
                "Bonjour %s,\n\nVotre code d'activation est : %s.\nCe code expirera dans 10 minutes.\n\nCordialement,\nBlack Adhule System",
                validationDto.getUserDto().getFirstname(),
                validationDto.getCode()
        );
        mailMessage.setText(text);

        javaMailSender.send(mailMessage);

        return validationDto;
    }
}
