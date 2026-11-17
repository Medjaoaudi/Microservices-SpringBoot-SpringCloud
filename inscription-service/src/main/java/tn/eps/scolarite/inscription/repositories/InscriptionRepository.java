package tn.eps.scolarite.inscription.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.eps.scolarite.inscription.entities.Inscription;
import tn.eps.scolarite.inscription.entities.StatutInscription;

import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByEtudiantIdOrderByAnneeUniversitaireDescIdAsc(Long etudiantId);

    List<Inscription> findByEtudiantIdAndAnneeUniversitaireOrderByIdAsc(Long etudiantId, String annee);

    List<Inscription> findByCoursIdOrderByIdAsc(Long coursId);

    /** Doublon : même étudiant, même cours, même année, inscription non annulée. */
    boolean existsByEtudiantIdAndCoursIdAndAnneeUniversitaireAndStatutNot(
            Long etudiantId, Long coursId, String annee, StatutInscription statutExclu);

    /** Nombre de places occupées dans un cours pour une année (inscriptions non annulées). */
    long countByCoursIdAndAnneeUniversitaireAndStatutNot(Long coursId, String annee, StatutInscription statutExclu);
}
