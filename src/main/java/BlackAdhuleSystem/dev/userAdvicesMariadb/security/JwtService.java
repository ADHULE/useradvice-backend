package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations.UserServiceImp;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserServiceImp userServiceImp;

    @Value("${app.secret-key}")
    private String encryptionKey;

    private static final Logger logger = Logger.getLogger(JwtService.class.getName());

    // ------------------------------
    //   GÉNÉRATION DU TOKEN
    // ------------------------------
    public Map<String, String> generateToken(String username) {
        // loadUserByUsername peut retourner un User ou UserDetails selon ton implémentation.
        User user = this.userServiceImp.loadUserByUsername(username);
        logger.info("generateToken called for " + username);
        logger.info("userServiceImp = " + userServiceImp);

        return buildJwtToken(user);
    }

    private Map<String, String> buildJwtToken(User user) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTime = currentTimeMillis + 30 * 60 * 1000; // 30 minutes

        Map<String, String> claims = Map.of(
                "name", user.getName(),
                "email", user.getEmail()
        );

        String jwtToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // subject = email
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        return Map.of("bearer", jwtToken);
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
    //   VALIDATION ET EXTRACTION
    // ------------------------------

    // Extraire l'email (subject) depuis le token
    public String extractEmail(String token) {
        if (token == null) return null;
        // si le token contient "Bearer " par erreur, on le nettoie
        if (token.startsWith("Bearer ")) token = token.substring(7);
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    // Vérifier si un token est valide et non expiré
    public boolean isTokenValid(String token) {
        if (token == null) return false;
        try {
            // nettoie le prefix s'il existe
            if (token.startsWith("Bearer ")) token = token.substring(7);
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

    private Claims getClaims(String token) {
        if (token == null) return null;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            // remonter l'exception pour que l'appelant sache que le token est invalide
            logger.fine("parseClaimsJws échoué: " + e.getMessage());
            throw e;
        }                                                                 
    }
}
