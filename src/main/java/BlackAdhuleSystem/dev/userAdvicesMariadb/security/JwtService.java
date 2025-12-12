package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Jwt;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.RefreshToken;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.JwtRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JwtService {

    public static final String TOKEN_KEY = "token";

    private final UserServiceImp userServiceImp;
    private final JwtRepository jwtRepository;

    @Value("${app.secret-key:}") // clé injectée, vide par défaut
    private String encryptionKey;

    // ============================================================
    // INITIALISATION DE LA CLE SECRETE
    // ============================================================
    @PostConstruct
    private void initSecretKey() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            encryptionKey = generateSecretKey();
            log.warn("Aucune clé secrète définie. Une clé aléatoire vient d’être générée : {}", encryptionKey);
        } else {
            log.info("Clé secrète chargée depuis la configuration.");
        }
    }

    // ============================================================
    // GENERATION AUTOMATIQUE DE LA CLE SECRETE
    // ============================================================
    private String generateSecretKey() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        return Encoders.BASE64.encode(key.getEncoded());
    }

    // ============================================================
    // GENERATION DU TOKEN INITIAL
    // ============================================================
    public Map<String, String> generateToken(String email) {
        UserDetails userDetails = userServiceImp.loadUserByUsername(email);
        User user;
        try {
            user = (User) userDetails;
        } catch (ClassCastException ex) {
            log.error("Impossible de caster UserDetails en User pour l'email {}: {}", email, ex.getMessage());
            throw new RuntimeException("Erreur interne lors de la génération du token");
        }

        long now = System.currentTimeMillis();
        long expirationTime = now + (15 * 60 * 1000); // ✅ 15 minutes

        RefreshToken refreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .expire(false)
                .creation(Instant.now())
                .expiration(Instant.now().plusSeconds(7 * 24 * 3600)) // ✅ 7 jours
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
        log.info("Token généré pour : {}", email);

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
            log.error("Clé secrète invalide (Base64) : {}", e.getMessage(), e);
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
            log.warn("Token expiré : {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.warn("Token invalide : {}", e.getMessage());
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
            log.warn("Token non valide : {}", e.getMessage());
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
                log.warn("Token fourni invalide ou déjà désactivé.");
                SecurityContextHolder.clearContext();
                return false;
            }
        } else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.warn("Aucun utilisateur authentifié.");
                return false;
            }
            email = auth.getName();
        }

        List<Jwt> activeTokens = jwtRepository.findAllByUserEmailAndDesactiveFalseAndExpireFalse(email);

        if (!activeTokens.isEmpty()) {
            for (Jwt t : activeTokens) {
                t.setDesactive(true);
                t.setExpire(true);
                jwtRepository.save(t);
            }
            SecurityContextHolder.clearContext();
            log.info("Déconnexion réussie, tous les tokens désactivés pour : {}", email);
            return true;
        }

        log.warn("Aucun token actif trouvé pour : {}", email);
        SecurityContextHolder.clearContext();
        return false;
    }

    // ============================================================
    // NETTOYAGE AUTOMATIQUE
    // ============================================================
    @Scheduled(cron = "0 */1 * * * *")
    public void removeUseLessToken() {
        log.info("Nettoyage des tokens inutiles {}", Instant.now());
        jwtRepository.deleteAllByExpireAndDesactive(true, true);
    }

    // ============================================================
    // REFRESH TOKEN COMPLET
    // ============================================================
    public Map<String, String> refreshToken(Map<String, String> request) {
        String refreshValue = request == null ? null : request.get("refresh");
        if (refreshValue == null) {
            throw new RuntimeException("Refresh token manquant !");
        }

        Jwt oldJwt = jwtRepository.findByRefreshTokenValue(refreshValue)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide !"));

        RefreshToken refreshToken = oldJwt.getRefreshToken();
        User user = oldJwt.getUser();

        if (refreshToken.isExpire() || refreshToken.getExpiration().isBefore(Instant.now())) {
            refreshToken.setExpire(true);
            oldJwt.setExpire(true);
            oldJwt.setDesactive(true);
            jwtRepository.save(oldJwt);
            throw new RuntimeException("Refresh token expiré !");
        }

        oldJwt.setExpire(true);
        oldJwt.setDesactive(true);
        jwtRepository.save(oldJwt);

        long now = System.currentTimeMillis();
        long expirationTime = now + (15 * 60 * 1000); // ✅ 15 minutes

        String newJwt = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .expire(false)
                .creation(Instant.now())
                .expiration(Instant.now().plusSeconds(7 * 24 * 3600)) // ✅ 7 jours
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
                TOKEN_KEY, newJwt,
                "refresh", newRefreshToken.getValue(),
                "expiresAt", new Date(expirationTime).toString()
        );
    }

    // ============================================================
    // GENERER UN NOUVEAU ACCESS TOKEN A PARTIR D'UN REFRESH TOKEN
    // ============================================================
    public String generateAccessTokenFromRefresh(String refreshValue) {
        if (refreshValue == null) {
            throw new RuntimeException("Refresh token manquant !");
        }

        Jwt jwt = jwtRepository.findByRefreshTokenValue(refreshValue)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide !"));

        RefreshToken refreshToken = jwt.getRefreshToken();
        User user = jwt.getUser();

        if (refreshToken.isExpire() || refreshToken.getExpiration().isBefore(Instant.now())) {
            refreshToken.setExpire(true);
            jwt.setExpire(true);
            jwt.setDesactive(true);
            jwtRepository.save(jwt);

            throw new RuntimeException("Refresh token expiré !");
        }

        long now = System.currentTimeMillis();
        long expirationTime = now + (15 * 60 * 1000); // ✅ 15 minutes

        String newAccessToken = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        log.info("Nouveau access token généré pour {}", user.getEmail());

        return newAccessToken;
    }

    // ============================================================
    // EXTRAIRE LA DATE D'EXPIRATION D'UN ACCESS TOKEN
    // ============================================================
    public Instant getExpiration(String accessToken) {
        try {
            Claims claims = getClaims(accessToken);
            Date expiration = claims.getExpiration();
            return expiration.toInstant();
        } catch (ExpiredJwtException e) {
            log.warn("Token déjà expiré : {}", e.getMessage());
            return e.getClaims().getExpiration().toInstant();
        } catch (JwtException e) {
            log.error("Impossible d'extraire l'expiration du token : {}", e.getMessage());
            throw new RuntimeException("Token invalide !");
        }
    }
}
