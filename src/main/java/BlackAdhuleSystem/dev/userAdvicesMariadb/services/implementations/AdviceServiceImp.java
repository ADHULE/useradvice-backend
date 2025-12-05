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
     */
    @Override
    public AdviceDto createAdvice(AdviceDto adviceDto, User user) {
        Advice advice = AdviceMapper.mapToAdvice(adviceDto);
        advice.setUser(user); // Associer l'utilisateur créateur
        Advice savedAdvice = adviceRepository.save(advice);
        return AdviceMapper.mapToAdviceDto(savedAdvice);
    }

    /**
     * Récupère tous les conseils (ADMIN uniquement).
     */
    @Override
    public List<AdviceDto> getAllAdvices() {
        return adviceRepository.findAll()
                .stream()
                .map(AdviceMapper::mapToAdviceDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les conseils d'un utilisateur spécifique.
     */
    @Override
    public List<AdviceDto> getAllAdvicesByUser(User user) {
        if (user == null) return List.of();

        // Si l'utilisateur est admin, retourne tous les avis
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ADMIN"));

        List<Advice> advices;
        if (isAdmin) {
            advices = adviceRepository.findAll();
        } else {
            advices = adviceRepository.findByUserId(user.getId());
        }

        return advices.stream()
                .map(AdviceMapper::mapToAdviceDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un conseil par son identifiant.
     */
    @Override
    public AdviceDto getAdviceById(Long adviceId) {
        return adviceRepository.findById(adviceId)
                .map(AdviceMapper::mapToAdviceDto)
                .orElse(null);
    }

    /**
     * Met à jour un conseil existant.
     * Vérifie que l'utilisateur authentifié est bien le créateur ou admin.
     */
    @Override
    public AdviceDto updateAdvice(Long adviceId, AdviceDto adviceDto, User user) {
        Optional<Advice> adviceOptional = adviceRepository.findById(adviceId);

        if (adviceOptional.isEmpty()) {
            return null; // Non trouvé
        }

        Advice adviceToUpdate = adviceOptional.get();

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ADMIN"));

        if (!isAdmin && (adviceToUpdate.getUser() == null ||
                !adviceToUpdate.getUser().getId().equals(user.getId()))) {
            return null; // Non autorisé
        }

        adviceToUpdate.setMessage(adviceDto.getMessage());
        adviceToUpdate.setStatus(adviceDto.getStatus());

        Advice updatedAdvice = adviceRepository.save(adviceToUpdate);
        return AdviceMapper.mapToAdviceDto(updatedAdvice);
    }

    /**
     * Récupère tous les avis d'un utilisateur authentifié.
     * @param user utilisateur connecté
     * @return liste des avis
     */
    @Override
    public List<AdviceDto> getAdvicesByUser(User user) {
        if (user == null) return List.of();

        return adviceRepository.findByUserId(user.getId())
                .stream()
                .map(AdviceMapper::mapToAdviceDto)
                .collect(Collectors.toList());
    }

    /**
     * Supprime un avis.
     * L'utilisateur peut supprimer seulement ses propres avis.
     * L'admin peut supprimer n'importe quel avis.
     *
     * @param adviceId identifiant de l'avis
     * @param user utilisateur authentifié
     * @return true si suppression réussie, false sinon
     */
    @Override
    public boolean deleteAdvice(Long adviceId, User user) {
        Optional<Advice> adviceOptional = adviceRepository.findById(adviceId);

        if (adviceOptional.isEmpty()) {
            return false; // Avis non trouvé
        }

        Advice advice = adviceOptional.get();

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ADMIN"));

        // Vérifie si user est propriétaire ou admin
        if (!isAdmin && (advice.getUser() == null ||
                !advice.getUser().getId().equals(user.getId()))) {
            return false; // Non autorisé
        }

        adviceRepository.delete(advice);
        return true; // Suppression réussie
    }
}
