package tn.eps.scolarite.cours.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Module = Unité d'Enseignement (UE). Nommé "ModuleEnseignement" pour éviter
 * toute confusion avec java.lang.Module ; la table s'appelle bien "modules".
 */
@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleEnseignement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String intitule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private Semestre semestre;

    @Column(nullable = false)
    private Double coefficient;

    @Column(nullable = false)
    private Integer credits;

    /** Relation 1-N : un module regroupe plusieurs cours (COURS / TD / TP). */
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Cours> cours = new ArrayList<>();

    public void ajouterCours(Cours c) {
        cours.add(c);
        c.setModule(this);
    }
}
