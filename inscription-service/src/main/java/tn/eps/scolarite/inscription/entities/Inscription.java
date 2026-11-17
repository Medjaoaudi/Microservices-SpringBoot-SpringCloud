package tn.eps.scolarite.inscription.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.Etudiant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Inscription d'un étudiant à un cours pour une année universitaire.
 * <p>
 * Seuls les identifiants distants (etudiantId, coursId) sont persistés : chaque microservice
 * reste maître de ses données. Les objets complets sont portés par des champs {@code @Transient}
 * (ignorés par JPA) remplis à la demande via les clients OpenFeign.
 */
@Entity
@Table(name = "inscriptions", indexes = {
        @Index(name = "idx_inscription_etudiant", columnList = "etudiant_id"),
        @Index(name = "idx_inscription_cours_annee", columnList = "cours_id, annee_universitaire")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Column(name = "cours_id", nullable = false)
    private Long coursId;

    @Column(nullable = false)
    private LocalDate dateInscription;

    @Column(name = "annee_universitaire", nullable = false, length = 9)
    private String anneeUniversitaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatutInscription statut;

    /** Note sur 20, nulle tant que l'évaluation n'a pas eu lieu. */
    private Double note;

    // ----------- Champs NON persistés, remplis via Feign -----------

    @Transient
    private Etudiant etudiant;

    @Transient
    private Cours cours;

    /** Avertissements de dégradation (service partenaire hors ligne, cache utilisé...). */
    @Transient
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @PrePersist
    void avantInsertion() {
        if (dateInscription == null) {
            dateInscription = LocalDate.now();
        }
        if (statut == null) {
            statut = StatutInscription.EN_ATTENTE;
        }
    }
}
