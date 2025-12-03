package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Advice;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.mapper.AdviceMapper;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.AdviceRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.AdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service AdviceService.
 * Gère la logique métier autour des conseils (Advice).
 */
@Service
@RequiredArgsConstructor
public class AdviceServiceImp implements AdviceService {

    private final AdviceRepository adviceRepository;

    /**
     * Crée un nouveau conseil et l'associe à l'utilisateur authentifié.
     *
     * @param adviceDto DTO contenant les données du conseil.
     * @param user      Utilisateur authentifié.
     * @return AdviceDto représentant le conseil sauvegardé.
     */
    @Override
    public AdviceDto createAdvice(AdviceDto adviceDto, User user) {
        Advice advice = AdviceMapper.mapToAdvice(adviceDto);
        advice.setUser(user); // Associer l'utilisateur créateur
        Advice savedAdvice = adviceRepository.save(advice);
        return AdviceMapper.mapToAdviceDto(savedAdvice);
    }

    /**
     * Récupère tous les conseils.
     *
     * @return Liste de AdviceDto.
     */
    @Override
    public List<AdviceDto> getAllAdvices() {
        return adviceRepository.findAll()
                .stream()
                .map(AdviceMapper::mapToAdviceDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un conseil par son identifiant.
     *
     * @param adviceId Identifiant du conseil.
     * @return AdviceDto ou null si absent.
     */
    @Override
    public AdviceDto getAdviceById(Long adviceId) {
        return adviceRepository.findById(adviceId)
                .map(AdviceMapper::mapToAdviceDto)
                .orElse(null);
    }

    /**
     * Met à jour un conseil existant.
     * Vérifie que l'utilisateur authentifié est bien le créateur.
     *
     * @param adviceId  Identifiant du conseil.
     * @param adviceDto Nouvelles données.
     * @param user      Utilisateur authentifié.
     * @return AdviceDto mis à jour ou null si non autorisé.
     */
    @Override
    public AdviceDto updateAdvice(Long adviceId, AdviceDto adviceDto, User user) {
        Optional<Advice> adviceOptional = adviceRepository.findById(adviceId);

        if (adviceOptional.isEmpty()) {
            return null; // ⚠️ À remplacer par une exception personnalisée
        }

        Advice adviceToUpdate = adviceOptional.get();

        if (adviceToUpdate.getUser() != null && !adviceToUpdate.getUser().getId().equals(user.getId())) {
            return null; // ⚠️ À remplacer par une exception Unauthorized
        }

        adviceToUpdate.setMessage(adviceDto.getMessage());
        adviceToUpdate.setStatus(adviceDto.getStatus());

        Advice updatedAdvice = adviceRepository.save(adviceToUpdate);
        return AdviceMapper.mapToAdviceDto(updatedAdvice);
    }

    /**
     * Supprime un conseil par son identifiant.
     *
     * @param adviceId Identifiant du conseil.
     */
    @Override
    public void deleteAdvice(Long adviceId) {
        adviceRepository.deleteById(adviceId);
    }
}
