package BlackAdhuleSystem.dev.userAdvicesMariadb.repository;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.PermissionEnum;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository <Privilege, Long> {
    Optional<Privilege> findByName(PermissionEnum p);
}
