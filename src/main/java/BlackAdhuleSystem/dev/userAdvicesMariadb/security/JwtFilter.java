package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Jwt;
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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Pas de header  on continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(7);
        String email = null;

        try {
            email = jwtService.extractEmail(jwtToken);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erreur lors de l'extraction de l'email: " + e.getMessage(), e);
        }

        // Si email trouvé et pas encore authentifié
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Vérifier validité du token (crypto + BD)
            if (jwtService.isTokenValid(jwtToken)) {
                var userDetails = userServiceImp.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authentification réussie pour : " + email);
            } else {
                logger.warning("Token invalide ou désactivé pour : " + email);
            }
        }

        filterChain.doFilter(request, response);
    }
}
