package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Jwt;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.RefreshToken;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.JwtRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
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
    // GENERATION DU TOKEN INITIAL
    // ============================================================
    public Map<String, String> generateToken(String email) {

        User user = userServiceImp.loadUserByUsername(email);

        long now = System.currentTimeMillis();
        long expirationTime = now + (60 * 1000); // 1 minute

        RefreshToken refreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .expire(false)
                .creation(Instant.now())
                .expiration(Instant.now().plusSeconds(1800)) // 30 min
                .build();

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
                .refreshToken(refreshToken)
                .build();

        jwtRepository.save(jwt);
        logger.info("Token généré pour : " + email);

        return Map.of(
                TOKEN_KEY, jwtToken,
                "refresh", refreshToken.getValue(),
                "expiresAt", new Date(expirationTime).toString()
        );
    }

    // ============================================================
    // CLE DE SIGNATURE
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
    // SUPPRESSION PREFIXE BEARER
    // ============================================================
    private String stripBearer(String token) {
        if (token == null) return null;
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    // ============================================================
    // EXTRACTION CLAIMS
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
    // EXTRAIRE EMAIL
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
    // VALIDATION TOKEN
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
    // LOGOUT UTILISATEUR
    // ============================================================
    public boolean deconnexion(String token) {
        String email = null;

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
        } else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.warning("Aucun utilisateur authentifié.");
                return false;
            }
            email = auth.getName();
        }

        List<Jwt> activeTokens =
                jwtRepository.findAllByUserEmailAndDesactiveFalseAndExpireFalse(email);

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

        logger.warning("Aucun token actif trouvé pour : " + email);
        SecurityContextHolder.clearContext();
        return false;
    }

    // ============================================================
    // NETTOYAGE AUTOMATIQUE
    // ============================================================
    @Scheduled(cron = "0 */1 * * * *")
    public void removeUseLessToken() {
        log.info("Nettoyage des tokens inutiles " + Instant.now());
        jwtRepository.deleteAllByExpireAndDesactive(true, true);
    }

    // ============================================================
    // REFRESH TOKEN COMPLET (SANS DUPLICATE ENTRY)
    // ============================================================
    public Map<String, String> refreshToken(Map<String, String> request) {

        String refreshValue = request.get("refresh");
        if (refreshValue == null) {
            throw new RuntimeException("Refresh token manquant !");
        }

        // Rechercher le JWT existant avec ce refresh token
        Jwt oldJwt = jwtRepository.findByRefreshTokenValue(refreshValue)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide !"));

        RefreshToken refreshToken = oldJwt.getRefreshToken();
        User user = oldJwt.getUser();

        // Vérifier expiration
        if (refreshToken.isExpire() || refreshToken.getExpiration().isBefore(Instant.now())) {
            refreshToken.setExpire(true);
            oldJwt.setExpire(true);
            oldJwt.setDesactive(true);
            jwtRepository.save(oldJwt);

            throw new RuntimeException("Refresh token expiré !");
        }

        // Désactiver l'ancien JWT
        oldJwt.setExpire(true);
        oldJwt.setDesactive(true);
        jwtRepository.save(oldJwt);

        long now = System.currentTimeMillis();
        long expirationTime = now + (60 * 1000); // 1 minute

        // Générer un nouveau JWT
        String newJwt = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        // Toujours créer un nouveau refresh token pour éviter duplicate entry
        RefreshToken newRefreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .expire(false)
                .creation(Instant.now())
                .expiration(Instant.now().plusSeconds(1800)) // 30 min
                .build();

        Jwt newRecord = Jwt.builder()
                .value(newJwt)
                .desactive(false)
                .expire(false)
                .user(user)
                .refreshToken(newRefreshToken)
                .build();

        jwtRepository.save(newRecord);

        return Map.of(
                "token", newJwt,
                "refresh", newRefreshToken.getValue(),
                "expiresAt", new Date(expirationTime).toString()
        );
    }
}
