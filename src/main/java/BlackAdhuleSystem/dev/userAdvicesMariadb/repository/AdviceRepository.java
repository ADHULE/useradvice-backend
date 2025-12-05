package BlackAdhuleSystem.dev.userAdvicesMariadb.repository;

import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Advice;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdviceRepository extends JpaRepository <Advice,Long> {

    /**
     * Trouve tous les Advice associés à un utilisateur spécifique.
     * Cette méthode est automatiquement implémentée par Spring Data JPA
     * grâce à la convention de nommage 'findBy' + Nom du champ dans l'entité Advice (qui est 'user').
     *
     * @param user L'entité User utilisée pour la recherche.
     * @return Une liste d'entités Advice.
     */
    List<Advice> findByUser(User user);

    // OU, si vous préférez chercher directement par l'ID de l'utilisateur :

    /**
     * Trouve tous les Advice associés à un ID utilisateur spécifique.
     * Spring Data JPA reconnaît le 'UserId' car 'user' est une relation
     * dans l'entité Advice et 'Id' est la clé primaire de l'entité User.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste d'entités Advice.
     */
    List<Advice> findByUserId(Long userId);

}