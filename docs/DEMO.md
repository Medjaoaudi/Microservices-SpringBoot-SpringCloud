# Script de soutenance (10 minutes)

## Avant de passer (5 min avant, hors chrono)

1. `start-all.bat` (ou `./start-all.sh`) **déjà lancé** ; vérifier http://localhost:5173 → tout est **UP** / **CLOSED**.
2. Ouvrir à l'avance, dans cet ordre, les onglets du navigateur :
   1. http://localhost:8761 (Eureka)
   2. http://localhost:8888/api/inscriptions/1 (JSON ; installer une extension de mise en forme JSON si possible)
   3. http://localhost:8083/actuator/circuitbreakers
   4. http://localhost:8888/swagger-ui.html (Swagger agrégé)
   5. http://localhost:5173 (front, tableau de bord)
3. Un terminal ouvert **dans le dossier du projet**, prêt pour `stop-service.bat etudiant`.
4. « Chauffer » le cache : ouvrir une fois la page **Inscriptions** du front (ou `GET /api/inscriptions`).
5. Si besoin de repartir de données propres : `stop-all`, supprimer `data/`, `start-all --no-build`.

---

## 1. Présentation (2 min)

**Ce qu'il faut dire :**

- *Domaine* : « La scolarité d'une école d'ingénieurs : les étudiants, le catalogue des modules et cours, et les
  inscriptions pédagogiques avec les notes et le relevé. »
- *Découpage* (montrer le schéma Mermaid du README) :
  - `etudiant-service` (8081, `etudiant_db`) : le référentiel des étudiants ;
  - `cours-service` (8082, `cours_db`) : modules (UE, coefficient, crédits) en relation **1-N** avec les cours ;
  - `inscription-service` (8083, `inscription_db`) : l'**orchestrateur** ; il ne stocke que `etudiantId` / `coursId`
    et agrège le reste via **OpenFeign** ;
  - `discovery-server` (Eureka, 8761) et `api-gateway` (8888, WebFlux) pour l'infrastructure.
- *Pourquoi* : un service par contexte métier, **une base par service** (autonomie, pas de couplage par le schéma),
  montée en charge ciblée (période d'inscriptions), isolation des pannes.
- *Technique* : POM parent unique (Boot 3.4.13, BOM Spring Cloud 2024.0.3), aucune URL en dur (`lb://`,
  `@FeignClient(name=...)`), Swagger sur chaque service, Resilience4j.

---

## 2. Démonstration (4 min)

| Temps | Action | Ce qu'on montre / dit |
|---|---|---|
| 0:00 | Onglet **Eureka** | Les 4 instances `UP` : API-GATEWAY, ETUDIANT-, COURS-, INSCRIPTION-SERVICE. « Chaque service s'enregistre avec son nom logique ; personne ne connaît l'IP des autres. » |
| 0:30 | Onglet `localhost:8888/api/inscriptions/1` | Appel **via la Gateway**. Montrer `etudiant` et `cours` remplis : « Ce sont des champs **`@Transient`** de l'entité `Inscription`, non persistés, remplis par Feign. » Montrer aussi `cours.module.coefficient`. |
| 1:00 | Terminal : `stop-service.bat etudiant` | « Je simule la panne du référentiel étudiants. » |
| 1:10 | Rafraîchir `/api/inscriptions/1` **3 fois** | Toujours **200**. `sourceDonnees: CACHE_SECOURS`, `serviceStatus`, `warnings`. « Le fallback a servi la dernière valeur connue : la consultation continue. » (les 1ers appels mettent ~4 s : c'est le **TimeLimiter**) |
| 1:50 | Onglet `actuator/circuitbreakers` | `ETUDIANT-SERVICE` = **OPEN**. Rafraîchir l'inscription : réponse instantanée. « Le circuit est ouvert : on n'appelle même plus le service. » |
| 2:20 | Front → **Tableau de bord** | Badge « Service indisponible », bandeau « Mode dégradé », disjoncteur rouge **OPEN** |
| 2:40 | Front → **Inscriptions** → inscrire Ferchichi Skander (identifiant 9) au cours 2 | **503** clair : « En écriture on refuse d'inscrire à l'aveugle. » |
| 3:00 | Relancer : `java -jar etudiant-service\target\etudiant-service-1.0.0.jar` (ou `start-all.bat --no-build`) | Pendant le démarrage (~15 s) : circuit **HALF_OPEN** au bout de 10 s |
| 3:30 | Rafraîchir `/api/inscriptions/1` puis `circuitbreakers` | `serviceStatus` UP, circuit **CLOSED**. Montrer `/actuator/circuitbreakerevents/ETUDIANT-SERVICE` (transitions) |
| 3:45 | **Swagger agrégé** `localhost:8888/swagger-ui.html` | Sélecteur des 3 APIs ; « Try it out » `POST /api/inscriptions` avec `{"etudiantId":8,"coursId":6}` → **409 cours complet**. Puis `GET /api/inscriptions/etudiant/1/releve` → moyenne pondérée 14,47 |

**Plan B** (si quelque chose ne répond pas) : `docs/requests.http` contient toutes les requêtes ; la démonstration de
panne est aussi entièrement visible dans le front (tableau de bord + inscriptions).

---

## 3. Questions probables de l'enseignant et réponses

**1. Quel est le rôle d'Eureka ?**
C'est l'**annuaire** (service registry). Au démarrage, chaque service s'enregistre (nom logique, IP, port) puis envoie un
*heartbeat* toutes les 5 s (`lease-renewal-interval-in-seconds`). Sans heartbeat pendant 10 s
(`lease-expiration-duration-in-seconds`), l'instance est retirée. Les clients (Gateway, Feign) récupèrent le registre et
résolvent un nom comme `ETUDIANT-SERVICE` en adresse réelle. On peut ainsi ajouter, déplacer ou répliquer des instances
sans rien reconfigurer. J'ai désactivé l'auto-préservation pour que la panne se voie vite en démo.

**2. Que signifie `lb://ETUDIANT-SERVICE` ?**
`lb` = *load balancer*. La Gateway ne contacte pas une URL fixe : Spring Cloud LoadBalancer demande à Eureka la liste
des instances de `ETUDIANT-SERVICE` et en choisit une (round-robin par défaut). Avec 2 instances, la charge est répartie
automatiquement.

**3. Différence entre routes statiques et discovery locator ?**
Les routes du YAML (`/api/etudiants/**` → `lb://ETUDIANT-SERVICE`) sont déclarées explicitement et peuvent porter des
filtres (CircuitBreaker…). Le *discovery locator* crée **dynamiquement** une route par service Eureka :
`/etudiant-service/**` (ex. `http://localhost:8888/etudiant-service/api/etudiants`), utile mais sans contrôle fin.

**4. Pourquoi des champs `@Transient` ?**
L'inscription n'est propriétaire que des identifiants. Les objets `Etudiant` et `Cours` appartiennent à d'autres services
et bases : les persister créerait une copie désynchronisée. `@Transient` indique à JPA de **ne pas mapper** ces champs ;
on les remplit à chaque lecture via Feign. Les classes `model.Etudiant` / `model.Cours` sont de simples POJO, pas des
`@Entity`.

**5. Comment fonctionne OpenFeign ?**
On déclare une **interface** annotée `@FeignClient(name = "ETUDIANT-SERVICE")` avec des annotations Spring MVC
(`@GetMapping`, `@PathVariable`). Au démarrage, `@EnableFeignClients` génère un **proxy** qui, à chaque appel, résout le
nom via Eureka/LoadBalancer, construit la requête HTTP, l'envoie et désérialise le JSON. Pas d'attribut `url` : aucune
adresse en dur. Avec `spring.cloud.openfeign.circuitbreaker.enabled=true`, chaque appel passe par un Circuit Breaker
et la `fallbackFactory`.

**6. Différence entre Gateway WebFlux et Gateway MVC ?**
`spring-cloud-starter-gateway` (utilisé ici) est **réactive** : Netty + Project Reactor, modèle non bloquant avec peu de
threads, adapté à un composant qui ne fait que relayer des milliers de requêtes. La variante *Server MVC*
(`spring-cloud-starter-gateway-mvc`) tourne sur Tomcat/servlets (un thread par requête). On ne doit pas mettre
`spring-boot-starter-web` dans une Gateway WebFlux : les deux piles entreraient en conflit (Spring choisirait MVC et la
Gateway ne démarrerait pas).

**7. À quoi sert le BOM / `dependencyManagement` ?**
Le BOM `spring-cloud-dependencies` (importé avec `<scope>import</scope>`) fixe des versions **compatibles entre elles**
pour tous les artefacts Spring Cloud (Eureka, Gateway, OpenFeign, CircuitBreaker…) ; le parent `spring-boot-starter-parent`
fait de même pour Spring Boot. Les modules déclarent leurs dépendances **sans version**, ce qui évite les conflits et
permet de monter de version en un seul endroit (`<spring-cloud.version>`). Le release train 2024.0.x correspond à
Spring Boot 3.4.x.

**8. Expliquez le Circuit Breaker de votre projet.**
Fenêtre des 5 derniers appels, évaluée dès 3 appels ; au-delà de 50 % d'échecs, le circuit passe **OPEN** pendant
10 s : les appels vont directement au fallback (`CallNotPermittedException`), en quelques ms. Puis **HALF_OPEN** :
2 appels de test ; s'ils réussissent → **CLOSED**. Les 404 sont ignorés : une ressource inexistante n'est pas une panne.
Un disjoncteur par service partenaire grâce au `CircuitBreakerNameResolver`.

**9. Pourquoi Resilience4j et pas Hystrix ?**
Hystrix est en maintenance depuis 2018 et a été retiré de Spring Cloud (2020.0). Resilience4j est léger, modulaire
(circuitbreaker, retry, timelimiter, bulkhead, ratelimiter), compatible Spring Boot 3 et réactif, et c'est
l'implémentation de référence de l'abstraction Spring Cloud CircuitBreaker.

**10. Que se passe-t-il pour un POST quand un service est en panne ?**
Refus explicite **503** avec `Retry-After`. Inscrire sans pouvoir vérifier l'étudiant ni la capacité risquerait des
incohérences (étudiant supprimé, cours sur-rempli). En lecture, au contraire, on dégrade gracieusement (200 + warnings).

**11. Pourquoi une base de données par service ?**
Autonomie (chaque équipe fait évoluer son schéma et peut choisir son SGBD), déploiement indépendant, isolation des pannes
et de la charge, encapsulation : les données ne sont accessibles **que par l'API** du service. Contrepartie : pas de
jointure ni de transaction distribuée simple. On compose par API (Feign) et on accepte la cohérence à terme (Saga,
événements) pour les cas complexes.

**12. Comment garantir la cohérence si un étudiant est supprimé alors qu'il a des inscriptions ?**
Dans ce projet, l'orchestrateur le détecte (404 → `serviceStatus: INTROUVABLE` + warning). En production on publierait
un événement `EtudiantSupprime` (Kafka/RabbitMQ) consommé par `inscription-service` pour annuler ses inscriptions
(cohérence à terme), ou on interdirait la suppression d'un étudiant inscrit.

**13. Retry : quels risques ?**
Rejouer une opération non idempotente (POST) peut créer des doublons ; trop de tentatives surchargent un service qui
redémarre (d'où le backoff et la limite à 3). Ici, seuls des GET sont rejoués, et le TimeLimiter (4 s) borne la durée totale.

**14. Comment est géré le cycle Module → Cours en JSON ?**
L'entité `Cours` a un `@ManyToOne module` et `ModuleEnseignement` un `@OneToMany cours`. Sérialiser les entités
provoquerait une boucle infinie. On expose des **DTO** : `CoursResponseDTO` contient un `ModuleResumeDTO` (sans la liste
des cours) et `ModuleResponseDTO` un simple `nombreCours`.

**15. Comment l'API est-elle documentée et testée ?**
springdoc-openapi génère la spécification OpenAPI 3 et Swagger UI sur chaque service (`/swagger-ui.html`), enrichie par
`@Tag`, `@Operation`, `@ApiResponse` et `@Schema(example=...)`. La Gateway agrège les 3 specs (routes
`/v3/api-docs/{service}`). Tests : JUnit 5 + Mockito pour les services (dont les cas de fallback) et `@WebMvcTest` pour
les contrôleurs : 43 tests, `mvn clean install` passe.

**16. Comment passer à l'échelle ?**
Lancer une 2e instance sur un autre port (`java -jar ... --server.port=8091`) : elle s'enregistre dans Eureka et
`lb://` / Feign répartissent automatiquement les appels, sans modifier aucune configuration.

**17. Pourquoi le front ne parle-t-il qu'à la Gateway ?**
Point d'entrée unique : un seul domaine/port pour le CORS, l'authentification future, les logs, le rate limiting et les
fallbacks. Les microservices pourraient ne pas être exposés du tout hors du réseau interne.
