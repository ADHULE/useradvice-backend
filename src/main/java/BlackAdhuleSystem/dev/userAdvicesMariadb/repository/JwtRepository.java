package BlackAdhuleSystem.dev.userAdvicesMariadb.repository;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Jwt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JwtRepository extends JpaRepository<Jwt, Long> {

    Optional<Jwt> findByValueAndDesactiveAndExpire(String value, boolean desactive, boolean expire);

    @Query("SELECT j FROM Jwt j WHERE j.user.email = :email AND j.desactive = :desactive AND j.expire = :expire")
    Optional<Jwt> findUserValidToken(
            @Param("email") String email,
            @Param("desactive") boolean desactive,
            @Param("expire") boolean expire
    );

    Optional<Jwt> findByValue(String value);

    List<Jwt> findAllByUserEmailAndDesactiveFalseAndExpireFalse(String email);
}
