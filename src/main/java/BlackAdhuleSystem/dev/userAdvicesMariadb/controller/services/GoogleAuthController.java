package BlackAdhuleSystem.dev.userAdvicesMariadb.controller.services;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogleAuthController {

    // Endpoint pour déclencher l'authentification Google
    @GetMapping("/auth/google")
    public String googleLogin() {
        // Redirige vers le flux OAuth2 de Spring Security
        return "redirect:/oauth2/authorization/google";
    }

    // Callback après authentification réussie
    @GetMapping("/login/oauth2/code/google")
    public String googleCallback() {
        // Ici Spring Security gère automatiquement le token et l'utilisateur
        // Tu peux rediriger vers une page sécurisée ou ton frontend
        return "redirect:/dashboard";
    }
}
