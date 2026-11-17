package tn.eps.scolarite.cours.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.eps.scolarite.cours.entities.Cours;

import java.util.List;

public interface CoursRepository extends JpaRepository<Cours, Long> {

    List<Cours> findByModuleIdOrderByCode(Long moduleId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            select c from Cours c
            where lower(c.titre) like lower(concat('%', :titre, '%'))
              and lower(c.enseignant) like lower(concat('%', :enseignant, '%'))
            order by c.code
            """)
    List<Cours> rechercher(@Param("titre") String titre, @Param("enseignant") String enseignant);
}
