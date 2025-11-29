package BlackAdhuleSystem.dev.userAdvicesMariadb.repository;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository <Role,Long> {
    Optional<Role> findByName(String roleName);



}
