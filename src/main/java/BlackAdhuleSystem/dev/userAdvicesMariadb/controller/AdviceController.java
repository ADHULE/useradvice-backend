package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.AdviceDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User; // Import de l'entité User pour l'authentification
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.AdviceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import crucial
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/advices")
public class AdviceController {

    private AdviceService adviceService;

    /**
     * Crée un nouveau conseil et l'associe à l'utilisateur authentifié (via JWT).
     *
     * @param adviceDto données du conseil à créer.
     * @param user L'utilisateur authentifié (injecté par Spring Security).
     * @return le conseil créé avec le statut HTTP 201.
     */
    @PostMapping
    public ResponseEntity<AdviceDto> serviceCreateApi(
            @RequestBody AdviceDto adviceDto,
            @AuthenticationPrincipal User user) { // <-- L'utilisateur est injecté ici

        // CORRECTION : Transmission de l'utilisateur au service
        AdviceDto savedAdvice = adviceService.createAdvice(adviceDto, user);
        return new ResponseEntity<>(savedAdvice, HttpStatus.CREATED);
    }

    /**
     * Récupère tous les conseils disponibles.
     *
     * @return liste des conseils avec le statut HTTP 200.
     */
    @GetMapping("all")
    public ResponseEntity<List<AdviceDto>> getAllAdvicesApi() {
        List<AdviceDto> advices = adviceService.getAllAdvices();
        return new ResponseEntity<>(advices, HttpStatus.OK);
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
        if (advice != null) {
            return new ResponseEntity<>(advice, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Met à jour un conseil existant, en vérifiant l'autorisation de l'utilisateur.
     *
     * @param adviceId  identifiant du conseil à modifier.
     * @param adviceDto nouvelles données du conseil.
     * @param user L'utilisateur authentifié (injecté par Spring Security).
     * @return le conseil mis à jour, statut 404 si absent, ou statut 403 si non autorisé.
     */
    @PutMapping("update/{id}")
    public ResponseEntity<AdviceDto> updateAdviceApi(
            @PathVariable("id") Long adviceId,
            @RequestBody AdviceDto adviceDto,
            @AuthenticationPrincipal User user) { // <-- L'utilisateur est injecté ici

        // CORRECTION : Transmission de l'utilisateur au service pour la vérification des droits
        AdviceDto updatedAdvice = adviceService.updateAdvice(adviceId, adviceDto, user);

        if (updatedAdvice != null) {
            // Mise à jour réussie
            return new ResponseEntity<>(updatedAdvice, HttpStatus.OK);
        } else {
            // Le service retourne null soit parce que le conseil n'existe pas,
            // soit parce que l'utilisateur n'est pas autorisé.

            // Pour distinguer les deux cas, on peut essayer de récupérer le conseil
            AdviceDto existingAdvice = adviceService.getAdviceById(adviceId);

            if (existingAdvice == null) {
                // Le conseil n'existe pas
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                // Le conseil existe, mais l'utilisateur n'est pas le propriétaire (le service a retourné null)
                return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
            }
        }
    }

    /**
     * Supprime un conseil par son identifiant.
     *
     * @param adviceId identifiant du conseil à supprimer.
     * @return statut HTTP 204 si suppression réussie.
     */
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteAdviceApi(@PathVariable("id") Long adviceId) {
        // NOTE: Une vérification d'autorisation (similaire à update) serait nécessaire ici
        // si seuls les propriétaires peuvent supprimer leurs conseils.
        adviceService.deleteAdvice(adviceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}