package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserServiceImp userServiceImp;

    private static final Logger logger = Logger.getLogger(JwtFilter.class.getName());

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Récupérer l'en-tête Authorization
        final String authHeader = request.getHeader("Authorization");
        String jwtToken = null;
        String email = null;

        try {
            // Vérifier si le header contient "Bearer <token>"
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7); // enlever "Bearer "
                email = jwtService.extractEmail(jwtToken);                           
            }

            // Si email trouvé et pas encore authentifié
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userServiceImp.loadUserByUsername(email);

                // Vérifier la validité du token
                if (jwtService.isTokenValid(jwtToken)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Mettre l'utilisateur dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Ne pas casser la chaîne de filtres : logger et continuer (retournera 401 si endpoint protégé)
            logger.log(Level.WARNING, "Erreur lors du traitement du JWT: " + e.getMessage(), e);
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
