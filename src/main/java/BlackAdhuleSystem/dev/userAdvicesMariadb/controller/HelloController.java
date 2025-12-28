package BlackAdhuleSystem.dev.userAdvicesMariadb.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Contrôleur REST minimal pour tester le backend
@RestController
public class HelloController {

    // Endpoint GET accessible à l'URL http://localhost:9191/api/actuator
    @GetMapping("/actuator")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("✅ Backend Spring Boot fonctionne dans Docker !");
    }
}
