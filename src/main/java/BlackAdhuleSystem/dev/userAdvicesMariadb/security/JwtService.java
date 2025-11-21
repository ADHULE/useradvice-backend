package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Jwt;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.JwtRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtService {

    public static final String TOKEN_KEY = "token";

    private final UserServiceImp userServiceImp;
    private final JwtRepository jwtRepository;

    @Value("${app.secret-key}")
    private String encryptionKey;

    private static final Logger logger = Logger.getLogger(JwtService.class.getName());

    // ============================================================
    //      GENERATION DU TOKEN
    // ============================================================
    public Map<String, String> generateToken(String email) {

        User user = userServiceImp.loadUserByUsername(email);

        long now = System.currentTimeMillis();
        long expirationTime = now + (60 * 60 * 1000); // 1 heure

        String jwtToken = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        Jwt jwt = Jwt.builder()
                .value(jwtToken)
                .desactive(false)
                .expire(false)
                .user(user)
                .build();

        jwtRepository.save(jwt);
        logger.info("Token généré pour : " + email);

        return Map.of(
                TOKEN_KEY, jwtToken,
                "expiresAt", new Date(expirationTime).toString()
        );
    }

    // ============================================================
    //      CLE DE SIGNATURE
    // ============================================================
    private Key getSigningKey() {
        try {
            byte[] decoded = Decoders.BASE64.decode(encryptionKey);
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Clé secrète invalide (Base64) : " + e.getMessage(), e);
            throw new RuntimeException("La clé secrète est invalide !");
        }
    }

    // ============================================================
    //      SUPPRESSION DU PREFIXE BEARER
    // ============================================================
    private String stripBearer(String token) {
        if (token == null) return null;
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    // ============================================================
    //      EXTRACTION DES CLAIMS
    // ============================================================
    public Claims getClaims(String token) throws JwtException {
        token = stripBearer(token);

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(300)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ============================================================
    //      EXTRAIRE EMAIL
    // ============================================================
    public String extractEmail(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            logger.warning("Token expiré : " + e.getMessage());
            return null;
        } catch (JwtException e) {
            logger.warning("Token invalide : " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    //      VALIDATION TOKEN
    // ============================================================
    public boolean isTokenValid(String token) {
        String stripped = stripBearer(token);
        if (stripped == null) return false;

        try {
            getClaims(stripped);

            return jwtRepository
                    .findByValueAndDesactiveAndExpire(stripped, false, false)
                    .isPresent();

        } catch (JwtException e) {
            logger.warning("Token non valide : " + e.getMessage());
            return false;
        }
    }

    // ============================================================
//      LOGOUT UTILISATEUR (UNIFIÉ)
// ============================================================
    public boolean deconnexion(String token) {
        String email = null;

        // 1. Si un token est fourni, on l’utilise pour retrouver l’utilisateur
        if (token != null) {
            String stripped = stripBearer(token);
            Optional<Jwt> jwtOpt = jwtRepository.findByValueAndDesactiveAndExpire(stripped, false, false);

            if (jwtOpt.isPresent()) {
                email = jwtOpt.get().getUser().getEmail();
            } else {
                logger.warning("Token fourni invalide ou déjà désactivé.");
                SecurityContextHolder.clearContext();
                return false;
            }
        }
        // 2. Sinon, on récupère l’utilisateur depuis le SecurityContext
        else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.warning("Aucun utilisateur authentifié.");
                return false;
            }
            email = auth.getName();
        }

        // 3. Désactiver TOUS les tokens actifs de cet utilisateur
        List<Jwt> activeTokens = jwtRepository.findAllByUserEmailAndDesactiveFalseAndExpireFalse(email);
        if (!activeTokens.isEmpty()) {
            for (Jwt t : activeTokens) {
                t.setDesactive(true);
                t.setExpire(true);
                jwtRepository.save(t);
            }
            SecurityContextHolder.clearContext();
            logger.info("Déconnexion réussie, tous les tokens désactivés pour : " + email);
            return true;
        }

        logger.warning("Aucun token actif trouvé pour l'utilisateur : " + email);
        SecurityContextHolder.clearContext();
        return false;
    }

}
