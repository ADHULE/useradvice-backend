package BlackAdhuleSystem.dev.userAdvicesMariadb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling   //permet d'ajouter des configurations
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class UserAdvicesMariadbApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAdvicesMariadbApplication.class, args);
    }


}