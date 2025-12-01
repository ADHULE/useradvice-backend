package BlackAdhuleSystem.dev.userAdvicesMariadb.controller.services;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FacebookAuthController {

    // Endpoint pour déclencher l'authentification Facebook
    @GetMapping("/auth/facebook")
    public String facebookLogin() {
        // Redirige vers le flux OAuth2 de Spring Security
        return "redirect:/oauth2/authorization/facebook";
    }

    // Callback après authentification réussie
    @GetMapping("/login/oauth2/code/facebook")
    public String facebookCallback() {
        // Spring Security gère automatiquement le token et l'utilisateur
        // Ici tu peux rediriger vers une page sécurisée ou ton frontend
        return "redirect:/dashboard";
    }
}
