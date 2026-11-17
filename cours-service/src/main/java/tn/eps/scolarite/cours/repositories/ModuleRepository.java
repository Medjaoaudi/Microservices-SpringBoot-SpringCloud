package tn.eps.scolarite.cours.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.eps.scolarite.cours.entities.ModuleEnseignement;

public interface ModuleRepository extends JpaRepository<ModuleEnseignement, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
