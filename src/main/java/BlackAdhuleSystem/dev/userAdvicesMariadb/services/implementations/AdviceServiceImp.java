package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Advice;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User; // Import de l'entité User
import BlackAdhuleSystem.dev.userAdvicesMariadb.mapper.AdviceMapper;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.AdviceRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.AdviceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdviceServiceImp implements AdviceService {

    private AdviceRepository adviceRepository;

    /**
     * Crée un nouveau conseil (Advice) à partir d'un DTO et l'associe à l'utilisateur authentifié.
     * C'est la méthode principale de création désormais.
     *
     * @param adviceDto DTO contenant les données du conseil à créer.
     * @param user L'entité User représentant l'utilisateur actuellement authentifié.
     * @return AdviceDto représentant le conseil sauvegardé.
     */
    @Override
    public AdviceDto createAdvice(AdviceDto adviceDto, User user) {
        Advice advice = AdviceMapper.mapToAdvice(adviceDto);

        // CORRECTION pour lier l'Advice à l'utilisateur qui l'a créé (pour que user_id ne soit pas NULL)
        advice.setUser(user);

        Advice savedAdvice = adviceRepository.save(advice);
        return AdviceMapper.mapToAdviceDto(savedAdvice);
    }

    /**
     * Récupère tous les conseils enregistrés dans la base de données.
     *
     * @return Liste de AdviceDto représentant tous les conseils.
     */
    @Override
    public List<AdviceDto> getAllAdvices() {
        List<Advice> advices = adviceRepository.findAll();
        return advices.stream()
                .map(AdviceMapper::mapToAdviceDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un conseil spécifique par son identifiant.
     *
     * @param adviceId Identifiant du conseil.
     * @return AdviceDto correspondant au conseil trouvé, ou null si absent.
     */
    @Override
    public AdviceDto getAdviceById(Long adviceId) {
        Optional<Advice> adviceOptional = adviceRepository.findById(adviceId);
        return adviceOptional.map(AdviceMapper::mapToAdviceDto).orElse(null);
    }

    /**
     * Met à jour un conseil existant avec les nouvelles données fournies,
     * en vérifiant que l'utilisateur authentifié est bien le créateur du conseil.
     * C'est la méthode principale de mise à jour désormais.
     *
     * @param adviceId  Identifiant du conseil à mettre à jour.
     * @param adviceDto Nouvelles données du conseil.
     * @param user L'entité User représentant l'utilisateur actuellement authentifié.
     * @return AdviceDto mis à jour, ou null si le conseil n'existe pas ou si l'utilisateur n'est pas autorisé.
     */
    @Override
    public AdviceDto updateAdvice(Long adviceId, AdviceDto adviceDto, User user) {
        Optional<Advice> adviceOptional = adviceRepository.findById(adviceId);

        if (adviceOptional.isPresent()) {
            Advice adviceToUpdate = adviceOptional.get();

            // SÉCURITÉ : Vérifie si l'utilisateur authentifié est bien le propriétaire de l'Advice
            if (adviceToUpdate.getUser() != null && !adviceToUpdate.getUser().getId().equals(user.getId())) {
                // L'utilisateur n'est pas le propriétaire
                // Un contrôleur pourrait ici renvoyer 403 Forbidden
                return null;
            }

            adviceToUpdate.setMessage(adviceDto.getMessage());
            adviceToUpdate.setStatus(adviceDto.getStatus());

            Advice updatedAdvice = adviceRepository.save(adviceToUpdate);
            return AdviceMapper.mapToAdviceDto(updatedAdvice);
        }
        return null;
    }

    /**
     * Supprime un conseil de la base de données par son identifiant.
     *
     * @param adviceId Identifiant du conseil à supprimer.
     */
    @Override
    public void deleteAdvice(Long adviceId) {
        adviceRepository.deleteById(adviceId);
    }
}