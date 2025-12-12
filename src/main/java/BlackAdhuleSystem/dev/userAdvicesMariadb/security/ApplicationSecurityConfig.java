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

    private final UserServiceImp userServiceImp;
    private final PasswordEncoderConfig passwordEncoderConfig;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Désactiver CSRF (obligatoire pour API REST avec JWT)
                .csrf(AbstractHttpConfigurer::disable)

                //  Activer CORS et utiliser la configuration définie plus bas
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Gestion des routes publiques et privées
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

                        .anyRequest().authenticated()
                )

                // API REST => Stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Pas de formulaire HTML
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // Ajout du filtre JWT avant UsernamePasswordAuth
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    // --------------------- Provider d’authentification ---------------------

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userServiceImp);
        provider.setPasswordEncoder(passwordEncoderConfig.passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // --------------------- Configuration CORS ---------------------

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        //  Autoriser ton frontend React/Vite
        configuration.setAllowedOrigins(List.of("http://localhost:3001"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Important pour Authorization
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true); // Permet cookies & tokens

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
