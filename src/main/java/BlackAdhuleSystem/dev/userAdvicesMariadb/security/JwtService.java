package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Jwt;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.JwtRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException; // Import pour l'erreur de BD
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String BEARER = "bearer";
    private final UserServiceImp userServiceImp;
    private final JwtRepository jwtRepository;

    @Value("${app.secret-key}")
    private String encryptionKey;

    private static final Logger logger = Logger.getLogger(JwtService.class.getName());

    // ------------------------------
    //     GÉNÉRATION DU TOKEN
    // ------------------------------
    public Map<String, String> generateToken(String username) {
        // Supposons que loadUserByUsername retourne bien l'entité User, qui implémente UserDetails
        User user = this.userServiceImp.loadUserByUsername(username);
        logger.info("generateToken called for " + username);

        Map<String, String> jwtTokenMap = buildJwtToken(user);

        // Construction de l'entité Jwt
        final Jwt jwt = Jwt.builder()
                .value(jwtTokenMap.get(BEARER))
                .desactive(false)
                .expire(false)
                .user(user)
                .build();

        // Tentative de sauvegarde du token dans la base de données
        try {
            this.jwtRepository.save(jwt);
        } catch (DataIntegrityViolationException e) {
            // Cette exception est souvent levée lorsque la taille de la colonne est insuffisante (e.g., VARCHAR(255))
            logger.log(Level.SEVERE,
                    "Erreur de sauvegarde du JWT : La colonne 'value' dans la table 'jwts' est probablement trop petite. " +
                            "Veuillez modifier le schéma de la base de données pour utiliser le type TEXT ou VARCHAR(2048). Cause: " + e.getMessage(), e);

            // On peut choisir de relancer l'exception ou de continuer.
            // Pour l'authentification, il est préférable de relancer pour bloquer la connexion si le token n'est pas sauvegardé.
            throw new RuntimeException("Échec de la sauvegarde du token. Veuillez vérifier la configuration de la base de données.", e);
        }

        return jwtTokenMap;
    }

    private Map<String, String> buildJwtToken(User user) {
        // Durée de validité de 30 minutes
        final long TOKEN_VALIDITY_MS = 30 * 60 * 1000;
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTime = currentTimeMillis + TOKEN_VALIDITY_MS;

        // Les claims peuvent être ajoutées via l'objet Claims si nécessaire, mais Map.of est suffisant ici
        Map<String, Object> claims = Map.of("name", user.getName(), "email", user.getEmail());

        String jwtToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // subject = email
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        // Retourne le token Bearer et l'heure d'expiration pour le client
        return Map.of(BEARER, jwtToken, "expiresAt", new Date(expirationTime).toString());
    }

    private Key getSigningKey() {
        try {
            byte[] decodedKey = Decoders.BASE64.decode(encryptionKey);
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Clé d'encryption invalide : vérifie que app.secret-key est une chaîne Base64 valide", e);
            throw e;
        }
    }

    // ------------------------------
    //     VALIDATION ET EXTRACTION
    // ------------------------------

    // Extraire l'email (subject) depuis le token
    // IMPORTANT : On suppose ici que le token n'a PAS le préfixe "Bearer " (ce qui est géré dans JwtFilter)
    public String extractEmail(String token) {
        if (token == null) return null;
        try {
            Claims claims = getClaims(token);
            return claims != null ? claims.getSubject() : null;
        } catch (JwtException e) {
            // En cas d'exception (expiré, invalide, etc.), on retourne null
            return null;
        }
    }

    /**
     * Valide la signature et la date d'expiration.
     * Si le token est invalide ou expiré, une exception est lancée et capturée.
     */
    public boolean isTokenValid(String token) {
        if (token == null) return false;

        try {
            // Tente de récupérer les claims. Si ça réussit, le token est valide.
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warning("Token expiré : " + e.getMessage());
        } catch (JwtException e) {
            logger.warning("Token invalide : " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erreur lors de la validation du token", e);
        }
        return false;
    }

    /**
     * Récupère les claims du token.
     * IMPORTANT : Laisse les exceptions (comme ExpiredJwtException ou SignatureException) être propagées.
     */
    public Claims getClaims(String token) throws JwtException {
        if (token == null) {
            throw new JwtException("Token is null.");
        }

        // Le préfixe "Bearer " ne doit PAS être ici
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            // Remonter l'exception pour que le filtre ou l'appelant puisse la gérer (par exemple, renvoyer 401)
            logger.log(Level.INFO, "JWT parsing failed: " + e.getMessage());
            throw e;
        }
    }
}