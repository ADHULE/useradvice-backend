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

/**
 * Controller pour gérer les avis (Advice)
 * - Création, lecture, mise à jour, suppression
 * - Respect des droits :
 *      - utilisateur = peut gérer ses propres avis
 *      - admin = peut gérer tous les avis
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/advices")
public class AdviceController {

    private final AdviceService adviceService;

    // ────────────────────────────────────────────────
    // CREATE (un utilisateur connecté peut créer un avis)
    // ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<AdviceDto> createAdviceApi(
            @RequestBody AdviceDto adviceDto,
            @AuthenticationPrincipal User user // Injecte l'utilisateur connecté
    ) {
        AdviceDto savedAdvice = adviceService.createAdvice(adviceDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAdvice);
    }

    // ────────────────────────────────────────────────
    // LIST ALL (accessible uniquement par l’admin)
    // ────────────────────────────────────────────────
    @GetMapping("/admin")
    public ResponseEntity<List<AdviceDto>> getAllAdvicesApi() {
        List<AdviceDto> advices = adviceService.getAllAdvices();
        return ResponseEntity.ok(advices);
    }

    // ────────────────────────────────────────────────
    // LIST MY OWN ADVICES (utilisateur peut voir ses avis)
    // ────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<List<AdviceDto>> getMyAdvicesApi(
            @AuthenticationPrincipal User user
    ) {
        List<AdviceDto> advices = adviceService.getAdvicesByUser(user);
        return ResponseEntity.ok(advices);
    }

    // ────────────────────────────────────────────────
    // GET BY ID (un utilisateur peut voir un avis par son id)
    // ────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<AdviceDto> getAdviceByIdApi(@PathVariable("id") Long adviceId) {
        AdviceDto advice = adviceService.getAdviceById(adviceId);
        return (advice != null)
                ? ResponseEntity.ok(advice)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ────────────────────────────────────────────────
    // UPDATE (un utilisateur peut modifier uniquement son propre avis)
    // ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<AdviceDto> updateAdviceApi(
            @PathVariable("id") Long adviceId,
            @RequestBody AdviceDto adviceDto,
            @AuthenticationPrincipal User user
    ) {
        AdviceDto updatedAdvice = adviceService.updateAdvice(adviceId, adviceDto, user);

        if (updatedAdvice != null) {
            return ResponseEntity.ok(updatedAdvice);
        }

        // Si l’avis existe mais n’appartient pas à l’utilisateur => FORBIDDEN
        boolean exists = adviceService.getAdviceById(adviceId) != null;
        return exists
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ────────────────────────────────────────────────
    // DELETE (utilisateur peut supprimer ses avis, admin peut supprimer tous les avis)
    // ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdviceApi(
            @PathVariable("id") Long adviceId,
            @AuthenticationPrincipal User user
    ) {
        // La méthode deleteAdvice doit retourner true si suppression autorisée et réussie
        boolean success = adviceService.deleteAdvice(adviceId, user);

        if (success) {
            return ResponseEntity.noContent().build();
        }

        // Si l’avis existe mais suppression interdite => FORBIDDEN
        boolean exists = adviceService.getAdviceById(adviceId) != null;
        return exists
                ? ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
