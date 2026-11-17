package tn.eps.scolarite.cours.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer volumeHoraire;

    @Column(nullable = false, length = 100)
    private String enseignant;

    @Column(nullable = false)
    private Integer capaciteMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeCours type;

    /** Côté propriétaire de la relation. Jamais sérialisé tel quel : on passe par des DTO (pas de boucle JSON). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    @ToString.Exclude
    private ModuleEnseignement module;
}
