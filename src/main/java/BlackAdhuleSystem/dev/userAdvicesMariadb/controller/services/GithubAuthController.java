package BlackAdhuleSystem.dev.userAdvicesMariadb.controller.services;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GithubAuthController {

    // Endpoint pour déclencher l'authentification GitHub
    @GetMapping("/auth/github")
    public String githubLogin() {
        // Redirige vers le flux OAuth2 de Spring Security
        return "redirect:/oauth2/authorization/github";
    }

    // Callback après authentification réussie
    @GetMapping("/login/oauth2/code/github")
    public String githubCallback() {
        // Spring Security gère l'utilisateur et le token
        return "redirect:/dashboard";
    }
}
