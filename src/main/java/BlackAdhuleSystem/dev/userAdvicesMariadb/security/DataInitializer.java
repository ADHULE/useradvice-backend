package BlackAdhuleSystem.dev.userAdvicesMariadb.security;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.PermissionEnum;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Privilege;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.PrivilegeRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.RoleRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PrivilegeRepository privilegeRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoderConfig encoder;

    @Override
    public void run(String... args) {

        // 1. Synchroniser toutes les permissions dans la base
        for (PermissionEnum p : PermissionEnum.values()) {
            privilegeRepo.findByName(p)
                    .orElseGet(() -> privilegeRepo.save(new Privilege(null, p)));
        }

        // 2. Rôle USER (accès de base)
        Role userRole = createOrUpdateRole(
                "ROLE_USER",
                Set.of(
                        PermissionEnum.USER_READ,
                        PermissionEnum.ADVICE_READ,
                        PermissionEnum.ADVICE_CREATE
                )
        );

        // 3. Rôle ADMIN (toutes les permissions)
        Role adminRole = createOrUpdateRole(
                "ROLE_ADMIN",
                Set.of(PermissionEnum.values()) // toutes les permissions automatiques
        );

        // 4. Créer un compte admin si absent
        if (!userRepo.existsByEmail("admin@gmail.com")) {
            User admin = new User();
            admin.setFistname("Super");
            admin.setLastname("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(encoder.passwordEncoder().encode("admin1234"));
            admin.setActif(true);
            admin.setRoles(Set.of(adminRole));
            userRepo.save(admin);
        }
    }

    private Role createOrUpdateRole(String roleName, Set<PermissionEnum> permissions) {

        Set<Privilege> privilegeEntities = permissions.stream()
                .map(p -> privilegeRepo.findByName(p).orElseThrow())
                .collect(Collectors.toSet());

        Optional<Role> existing = roleRepo.findByName(roleName);

        if (existing.isPresent()) {
            Role role = existing.get();
            role.setPrivileges(privilegeEntities);
            return roleRepo.save(role);
        } else {
            Role role = new Role(null, roleName, privilegeEntities);
            return roleRepo.save(role);
        }
    }
}
