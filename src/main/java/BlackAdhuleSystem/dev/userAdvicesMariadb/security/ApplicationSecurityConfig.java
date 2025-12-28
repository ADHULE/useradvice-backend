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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    // Services et composants injectés
    private final UserServiceImp userServiceImp;          // Service utilisateur (UserDetailsService)
    private final PasswordEncoderConfig passwordEncoderConfig; // Config du PasswordEncoder
    private final JwtFilter jwtFilter;                    // Filtre JWT personnalisé

    // ==============================
    //  Chaîne de filtres de sécurité
    // ==============================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Désactiver CSRF (utile pour API REST avec JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Activer CORS avec configuration personnalisée
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Définir les règles d’autorisation
                .authorizeHttpRequests(auth -> auth

                        // --------------------- AuthController ---------------------
                        .requestMatchers(POST, "/inscription").permitAll()
                        .requestMatchers(POST, "/activation").permitAll()
                        .requestMatchers("/generate-new-code").permitAll()
                        .requestMatchers(POST, "/login").permitAll()
                        .requestMatchers(POST, "/refresh-token").permitAll()
                        .requestMatchers(POST, "/change-password").permitAll()
                        .requestMatchers(POST, "/forgot-password").permitAll()
                        .requestMatchers(POST, "/new-password").permitAll()
                        .requestMatchers(POST, "/logout").permitAll()

                        // --------------------- OAuth2 Social Login ---------------------
                        .requestMatchers("/auth/google").permitAll()
                        .requestMatchers("/auth/github").permitAll()
                        .requestMatchers("/auth/facebook").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()

                        // --------------------- Preflight CORS ---------------------
                        .requestMatchers(OPTIONS, "/**").permitAll()

                        // --------------------- AdviceController ---------------------
                        .requestMatchers(GET, "/advices/me").permitAll()
                        .requestMatchers(GET, "/advices/admin").permitAll()
                        .requestMatchers(GET, "/advices/{id}").permitAll()
                        .requestMatchers(POST, "/advices").authenticated()
                        .requestMatchers(PUT, "/advices/{id}").authenticated()
                        .requestMatchers(DELETE, "/advices/{id}").authenticated()

                        // --------------------- UserController ---------------------
                        .requestMatchers(GET, "/users/me").authenticated()
                        .requestMatchers(PUT, "/users/me").authenticated()
                        .requestMatchers(DELETE, "/users/me").authenticated()

                        // --------------------- Partie ADMIN ---------------------
                        .requestMatchers(GET, "/users").permitAll()

                        // --------------------- Divers ---------------------
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll() //  Actuator accessible sans auth

                        // Toute autre requête doit être authentifiée
                        .anyRequest().authenticated()
                )

                // API REST => pas de session, tout est stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Désactiver le formulaire HTML et logout par défaut
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // Ajouter le filtre JWT avant l’authentification standard
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    // ==============================
    //  Provider d’authentification
    // ==============================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userServiceImp); // Service utilisateur
        provider.setPasswordEncoder(passwordEncoderConfig.passwordEncoder()); // Encoder des mots de passe
        return provider;
    }

    // ==============================
    //  Gestionnaire d’authentification
    // ==============================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ==============================
    //  Configuration CORS
    // ==============================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autoriser ton frontend React (localhost:3000 ou 3001)
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Autoriser tous les headers (incl. Authorization)
        configuration.setExposedHeaders(List.of("Authorization")); // Exposer le header JWT
        configuration.setAllowCredentials(true); // Autoriser cookies & tokens

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
