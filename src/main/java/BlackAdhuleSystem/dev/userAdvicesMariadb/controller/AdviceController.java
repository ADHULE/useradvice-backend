package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.AdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/advices")
public class AdviceController {

    private final AdviceService adviceService;

    /**
     * Crée un nouveau conseil et l'associe à l'utilisateur authentifié.
     *
     * @param adviceDto données du conseil à créer.
     * @param user utilisateur authentifié (injecté par Spring Security).
     * @return le conseil créé avec statut HTTP 201.
     */
    @PostMapping
    public ResponseEntity<AdviceDto> createAdviceApi(
            @RequestBody AdviceDto adviceDto,
            @AuthenticationPrincipal User user) {

        AdviceDto savedAdvice = adviceService.createAdvice(adviceDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAdvice);
    }

    /**
     * Récupère tous les conseils.
     *
     * @return liste des conseils avec statut HTTP 200.
     */

    @GetMapping
    public ResponseEntity<List<AdviceDto>> getAllAdvicesApi() {
        List<AdviceDto> advices = adviceService.getAllAdvices();
        return ResponseEntity.ok(advices);
    }

    /**
     * Récupère un conseil par son identifiant.
     *
     * @param adviceId identifiant du conseil.
     * @return le conseil trouvé ou statut 404 si absent.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdviceDto> getAdviceByIdApi(@PathVariable("id") Long adviceId) {
        AdviceDto advice = adviceService.getAdviceById(adviceId);
        return (advice != null)
                ? ResponseEntity.ok(advice)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Met à jour un conseil existant, en vérifiant l'autorisation de l'utilisateur.
     *
     * @param adviceId identifiant du conseil à modifier.
     * @param adviceDto nouvelles données du conseil.
     * @param user utilisateur authentifié.
     * @return le conseil mis à jour, statut 404 si absent, ou statut 403 si non autorisé.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdviceDto> updateAdviceApi(
            @PathVariable("id") Long adviceId,
            @RequestBody AdviceDto adviceDto,
            @AuthenticationPrincipal User user) {

        AdviceDto updatedAdvice = adviceService.updateAdvice(adviceId, adviceDto, user);

        if (updatedAdvice != null) {
            return ResponseEntity.ok(updatedAdvice);
        } else {
            AdviceDto existingAdvice = adviceService.getAdviceById(adviceId);
            return (existingAdvice == null)
                    ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
                    : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Supprime un conseil par son identifiant.
     * ⚠️ Vérification d'autorisation à ajouter si seuls les propriétaires peuvent supprimer.
     *
     * @param adviceId identifiant du conseil.
     * @return statut HTTP 204 si suppression réussie.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdviceApi(@PathVariable("id") Long adviceId,
                                                @AuthenticationPrincipal User user) {
        // TODO: Vérifier que user est bien propriétaire avant suppression
        adviceService.deleteAdvice(adviceId);
        return ResponseEntity.noContent().build();
    }
}
