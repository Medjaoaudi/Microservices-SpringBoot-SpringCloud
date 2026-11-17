package tn.eps.scolarite.cours.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.eps.scolarite.cours.entities.Cours;
import tn.eps.scolarite.cours.entities.ModuleEnseignement;
import tn.eps.scolarite.cours.entities.Semestre;
import tn.eps.scolarite.cours.entities.TypeCours;
import tn.eps.scolarite.cours.repositories.ModuleRepository;

/**
 * Catalogue initial : 5 modules du S9 (5ème GL) et 10 cours.
 * Les cours sont insérés dans l'ordre => ids 1..10, référencés par inscription-service.
 * Le cours 6 (BIGD-TP) a volontairement une capacité de 3 pour démontrer le refus "capacité atteinte".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JeuDeDonneesCatalogue implements CommandLineRunner {

    private final ModuleRepository moduleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (moduleRepository.count() > 0) {
            log.info("Catalogue déjà alimenté ({} modules) : pas de seeding", moduleRepository.count());
            return;
        }
        ModuleEnseignement jee = module("UE-JEE2", "Développement JEE avancé (JEE2)", 3.0, 5);
        ModuleEnseignement archi = module("UE-ARCHI", "Architecture logicielle", 2.5, 4);
        ModuleEnseignement bigData = module("UE-BIGD", "Big Data et NoSQL", 2.0, 4);
        ModuleEnseignement secu = module("UE-SECU", "Sécurité des systèmes d'information", 2.0, 3);
        ModuleEnseignement gp = module("UE-GPRO", "Gestion de projet agile", 1.5, 2);

        // Ordre d'insertion = ids des cours 1..10
        add(jee, "JEE2-C", "Microservices avec Spring Cloud", "Eureka, Gateway, OpenFeign, Resilience4j",
                21, "Dr. Hela Ben Amor", 40, TypeCours.COURS);
        add(jee, "JEE2-TP", "TP Spring Boot et OpenFeign", "Mise en oeuvre d'une architecture microservices",
                24, "M. Walid Khemiri", 20, TypeCours.TP);
        moduleRepository.save(jee);

        add(archi, "ARCHI-C", "Patterns d'architecture", "Hexagonale, CQRS, Event Sourcing, Saga",
                21, "Pr. Karim Zouari", 40, TypeCours.COURS);
        add(archi, "ARCHI-TD", "Conception orientée domaine (DDD)", "Bounded contexts et agrégats",
                14, "Pr. Karim Zouari", 25, TypeCours.TD);
        moduleRepository.save(archi);

        add(bigData, "BIGD-C", "Écosystème Hadoop et Spark", "HDFS, MapReduce, Spark SQL",
                21, "Dr. Leila Mansour", 40, TypeCours.COURS);
        add(bigData, "BIGD-TP", "TP MongoDB et Cassandra", "Modélisation NoSQL orientée requêtes",
                18, "Dr. Leila Mansour", 3, TypeCours.TP);
        moduleRepository.save(bigData);

        add(secu, "SECU-C", "Cryptographie appliquée", "Chiffrement symétrique/asymétrique, PKI, JWT",
                21, "Mme Ines Sassi", 40, TypeCours.COURS);
        add(secu, "SECU-TP", "TP Pentest et OWASP Top 10", "Audit d'une application web vulnérable",
                18, "Mme Ines Sassi", 20, TypeCours.TP);
        moduleRepository.save(secu);

        add(gp, "GPRO-C", "Scrum et Kanban", "Rôles, cérémonies et artefacts agiles",
                14, "M. Nizar Jebali", 40, TypeCours.COURS);
        add(gp, "GPRO-TD", "Atelier estimation et planification", "Planning poker, burndown, vélocité",
                10, "M. Nizar Jebali", 30, TypeCours.TD);
        moduleRepository.save(gp);

        log.info("Seeding cours_db : 5 modules et 10 cours insérés");
    }

    private static ModuleEnseignement module(String code, String intitule, double coef, int credits) {
        return ModuleEnseignement.builder().code(code).intitule(intitule).semestre(Semestre.S9)
                .coefficient(coef).credits(credits).build();
    }

    private static void add(ModuleEnseignement m, String code, String titre, String description,
                            int volume, String enseignant, int capacite, TypeCours type) {
        m.ajouterCours(Cours.builder().code(code).titre(titre).description(description)
                .volumeHoraire(volume).enseignant(enseignant).capaciteMax(capacite).type(type).build());
    }

}
