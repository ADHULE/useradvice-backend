package BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User; // Import de l'entité User

import java.util.List;

public interface AdviceService {

    /**
     * Crée un nouveau conseil et l'associe à l'utilisateur donné.
     * @param adviceDto Les données du conseil.
     * @param user L'utilisateur authentifié (le créateur du conseil).
     * @return Le DTO du conseil créé.
     */
    AdviceDto createAdvice(AdviceDto adviceDto, User user); // <-- Signature corrigée

    List<AdviceDto> getAllAdvices();


    List<AdviceDto> getAllAdvicesByUser(User user);

    AdviceDto getAdviceById(Long adviceId);

    /**
     * Met à jour un conseil existant, en vérifiant l'appartenance à l'utilisateur.
     * @param adviceId L'ID du conseil à modifier.
     * @param adviceDto Les nouvelles données.
     * @param user L'utilisateur authentifié (le propriétaire).
     * @return Le DTO du conseil mis à jour, ou null si l'utilisateur n'est pas autorisé.
     */
    AdviceDto updateAdvice(Long adviceId, AdviceDto adviceDto, User user); // <-- Signature corrigée

    List<AdviceDto> getAdvicesByUser(User user);

    boolean deleteAdvice(Long adviceId, User user);
}