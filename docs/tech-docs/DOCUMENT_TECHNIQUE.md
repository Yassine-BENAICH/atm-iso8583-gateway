# Document technique — Passerelle ISO 8583 pour ATM

Date de référence : 17 mars 2026  
Portée : description opérationnelle et technique du gateway REST → ISO 8583:1987, incluant architecture, flux, API, configuration, déploiement, tests et axes d’évolution.

## 1. Architecture

- Pile technologique : Java 17, Spring Boot 3.2.x, jPOS 2.1.10, Maven, Docker/Docker Compose.
- Couches principales :
  - Controller : endpoints REST, validation, gestion des erreurs globales.
  - Service : orchestration JSON ↔ ISO 8583, gestion des codes de réponse, traçage.
  - Codec : mapping des modèles JSON vers `ISOMsg` et inversement.
  - Channel : client TCP (ASCIIChannel) vers le switch, gestion timeout/reconnexion.
  - Packager jPOS : défini dans `src/main/resources/packager/custom_iso87.xml`.
- Composants satellites : Mock Switch TCP (port 9000) pour tests locaux, Swagger UI (`/api/swagger-ui.html`).

## 2. Flux de traitement

1. Réception d’une requête POST JSON sur un endpoint `/api/iso8583/*` avec `X-Request-ID` optionnel.
2. Validation Bean Validation (MTI 4 chiffres, champs obligatoires présents).
3. Mapping JSON → `ISOMsg` via Iso8583Codec et packager custom.
4. Envoi binaire sur le canal TCP vers le switch/Mock Switch.
5. Réception de la trame réponse, unpack via packager, mapping vers modèle JSON.
6. Traduction du code ISO 8583 en code HTTP (ex : `00` → 200, `05/14/41/43` → 403, `51/61` → 402, `91/96` → 503).
7. Log structuré (Request-ID, MTI, durée) puis réponse JSON enrichie (timestamp, temps de traitement, `transactionRef`).

## 3. Modèle ISO 8583 géré (subset 12 champs)

MTI 4 digits. Champs : 2 PAN (LLVAR), 3 ProcessingCode (N6), 4 Amount (N12), 7 TransmissionDateTime (N10), 11 STAN (N6), 12 LocalTime (N6), 13 LocalDate (N4), 37 RRN (AN12), 39 ResponseCode (AN2), 41 TerminalId (ANS8), 49 CurrencyCode (AN3). Validation de longueur/type conforme au packager custom.

## 4. API REST (synthèse)

- `POST /api/iso8583/send` : payload MTI libre, champs dynamiques.
- `POST /api/iso8583/authorize` : MTI 0100.
- `POST /api/iso8583/financial` : MTI 0200.
- `POST /api/iso8583/reversal` : MTI 0400.
- `POST /api/iso8583/presetment` : MTI 1200.
- `POST /api/iso8583/echo` : MTI 0800 (test réseau).
- `GET /api/iso8583/health` : sonde de vie simple.
- `GET /api/iso8583/status` : détails d’état (connexion switch, config courante).
Toutes les réponses sont JSON ; validation automatique, erreurs normalisées (`errorCode`, `details`).

## 5. Configuration

Fichier : `src/main/resources/application.yml` ou variables d’environnement.

- `iso8583.host` (127.0.0.1), `iso8583.port` (9000)
- `iso8583.connect-timeout` (ms), `iso8583.read-timeout` (ms)
- `iso8583.header-length` (longueur préfixe, défaut 4)
- `iso8583.institution-id`
- Logging : niveau par défaut INFO, `com.atm` en DEBUG pour le mapping ISO.
Override exemple : `ISO8583_HOST=10.10.150.25 ISO8583_PORT=8583 mvn spring-boot:run`.

## 6. Déploiement et exécution

Prerequis : JDK 17+, Maven 3.8+, Docker optionnel.

- Build local : `mvn clean install`
- Lancer le Mock Switch : `mvn test-compile exec:java -Dexec.mainClass=com.atm.iso8583.simulator.Iso8583MockSwitch`
- Lancer l’application : `mvn spring-boot:run` puis Swagger sur `/api/swagger-ui.html`.
- Conteneurs : `docker-compose up --build` (services gateway + mock-switch).

## 7. Sécurité et fiabilité (état courant)

- Validation d’entrée stricte, gestion centralisée des exceptions.
- Timeouts réseau configurables, reconnexion automatique du channel.
- Traçabilité : `X-Request-ID` propagé, journaux structurés.
- À prévoir : authentification (JWT), rate limiting, circuit breaker, chiffrement TLS côté canal TCP si requis par le switch.

## 8. Tests et qualité

- Unitaires : Iso8583Codec (conversion bidirectionnelle).
- Intégration : Iso8583Controller (validation, codes HTTP, santé).
- Simulations : Mock Switch inclus, collection Postman `ISO8583-Gateway-API.postman_collection.json`.
- Résultats actuels : tests JUnit verts (voir `test_full.log`).

## 9. Monitoring et observabilité (backlog)

À implémenter : MonitoringService, modèles TrafficEvent/TrafficMetrics, endpoints de métriques, dashboard temps réel (`monitoring.html`), export Prometheus/Grafana. Non bloquant pour les fonctions cœur.

## 10. Exploitation et opérations

- Supervision : surveiller logs `logs/iso8583-gateway.log` et disponibilité `/api/iso8583/health`.
- Paramètres sensibles : host/port switch, timeouts, institution-id.
- Recommandation : isoler le réseau Docker, restreindre l’accès au port 9000, activer TLS côté reverse-proxy HTTP si exposé.

## 11. Roadmap courte

1. Finaliser monitoring et dashboard métriques.
2. Ajouter authentification JWT + rôles.
3. Étendre le packager à davantage de champs ISO 8583.
4. Tests de charge et optimisation des buffers TCP.
