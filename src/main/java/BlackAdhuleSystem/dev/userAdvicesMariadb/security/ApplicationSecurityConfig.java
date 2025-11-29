package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    private final UserServiceImp userServiceImp;              // ton service qui implémente UserDetailsService
    private final PasswordEncoderConfig passwordEncoderConfig; // config pour encoder les mots de passe
    private final JwtFilter jwtFilter;                        // filtre JWT personnalisé

    /**
     * Configuration principale de la sécurité HTTP.
     * - Désactive CSRF (inutile pour une API REST stateless).
     * - Définit les endpoints publics (/inscription, /activation, /auth/login, /auth/logout).
     * - Toutes les autres requêtes nécessitent une authentification.
     * - Session stateless (pas de session HTTP, uniquement JWT).
     * - Ajoute le filtre JWT avant le filtre UsernamePasswordAuthenticationFilter.
     * - Désactive formLogin et logout par défaut pour éviter les conflits.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(POST, "/inscription").permitAll()
                        .requestMatchers(POST, "/activation").permitAll()
                        .requestMatchers(POST, "/login").permitAll()
                        .requestMatchers(POST,"refresh-token").permitAll()
                        .requestMatchers(POST, "/change-password").permitAll()
                        .requestMatchers(POST, "/forgot-password").permitAll()
                        .requestMatchers(POST, "/new-password").permitAll()
                        .requestMatchers(POST, "/logout").permitAll()
                        .requestMatchers("/error").permitAll() // autoriser /error pour éviter 403 sur erreurs
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin().disable()   //  désactive le /login par défaut
                .logout().disable()      //  désactive le /logout par défaut
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Fournit un AuthenticationProvider basé sur DAO.
     * - Utilise UserServiceImp comme UserDetailsService.
     * - Utilise BCryptPasswordEncoder pour encoder les mots de passe.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userServiceImp); // correction: utiliser directement ton service
        provider.setPasswordEncoder(passwordEncoderConfig.passwordEncoder());
        return provider;
    }

    /**
     * Fournit l'AuthenticationManager à partir de la configuration Spring.
     * - Utilisé pour authentifier les utilisateurs lors du login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
