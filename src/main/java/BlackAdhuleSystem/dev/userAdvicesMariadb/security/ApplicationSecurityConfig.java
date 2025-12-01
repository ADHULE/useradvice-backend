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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // --------------------- UserController ---------------------
                        .requestMatchers(POST, "/inscription").permitAll()
                        .requestMatchers(POST, "/activation").permitAll()
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

                        // --------------------- AdviceController ---------------------
                        // Lecture libre
                        .requestMatchers(GET, "/advices/all").permitAll()
                        .requestMatchers(GET, "/advices/{id}").permitAll()
                        // Création, modification, suppression protégées
                        .requestMatchers(POST, "/advices").authenticated()
                        .requestMatchers(PUT, "/advices/update/**").authenticated()
                        .requestMatchers(DELETE, "/advices/delete/**").authenticated()

                        // --------------------- Divers ---------------------
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin().disable()
                .logout().disable()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

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
}
