package tn.eps.scolarite.etudiant.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.eps.scolarite.etudiant.entities.Etudiant;
import tn.eps.scolarite.etudiant.entities.Filiere;

import java.util.List;
import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    Optional<Etudiant> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);

    boolean existsByMatriculeAndIdNot(String matricule, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    /** Recherche multicritère : chaque critère null est ignoré. */
    @Query("""
            select e from Etudiant e
            where (lower(e.nom) like lower(concat('%', :nom, '%'))
                   or lower(e.prenom) like lower(concat('%', :nom, '%')))
              and (:filiere is null or e.filiere = :filiere)
              and (:niveau is null or e.niveau = :niveau)
            order by e.nom, e.prenom
            """)
    List<Etudiant> rechercher(@Param("nom") String nom,
                              @Param("filiere") Filiere filiere,
                              @Param("niveau") Integer niveau);
}
