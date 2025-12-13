package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implémentation personnalisée de UserDetails pour Spring Security.
 * Permet de brancher notre entité User sur le mécanisme d'authentification.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Les rôles sont déjà stockés avec le préfixe ROLE_ (ex: ROLE_ADMIN, ROLE_USER)
        return user.getRoles().stream()
                .map(Role::getName) // récupère "ROLE_ADMIN" ou "ROLE_USER"
                .map(SimpleGrantedAuthority::new) // convertit en GrantedAuthority
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // mot de passe hashé
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // identifiant = email
    }

    @Override
    public boolean isAccountNonExpired() {
        // Pas de gestion d'expiration → toujours true
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Pas de gestion de verrouillage → toujours true
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Pas de gestion d'expiration des credentials → toujours true
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Activation du compte → c'est ici que Spring Security vérifie
        return user.isActif();
    }
}
