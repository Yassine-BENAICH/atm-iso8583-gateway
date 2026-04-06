# Document technique - Passerelle ISO 8583 pour ATM
 
Portee : Etat de `atm-iso8583-gateway`, incluant le gateway Spring Boot, la facade XML powerCARD, le dashboard Angular embarque, le monitoring et les options de deploiement.

## 1. Vue d'ensemble

- Application backend : Java 17, Spring Boot 3.2.3, Maven, jPOS 2.1.10.
- Documentation API : Swagger UI expose sur `/api/swagger-ui.html`.
- Observabilite : Spring Boot Actuator et service de monitoring applicatif en memoire.
- Interface utilisateur : dashboard Angular 20 compile dans `src/main/resources/static/dashboard`.
- Simulateur : mock switch TCP `Iso8583MockSwitch` pour les tests et le developpement local.

Le projet sert de passerelle entre des clients HTTP modernes et un hote ISO 8583:1987 joignable en TCP. Le coeur de l'application convertit des payloads JSON ou XML en `ISOMsg`, applique le packager jPOS defini dans `src/main/resources/packager/custom_iso87.xml`, transmet le message au switch, puis reconvertit la reponse vers un format HTTP exploitable.

## 2. Architecture logique

- `Iso8583Controller` : facade REST principale pour les flux ISO 8583 JSON/XML.
- `PowerCardDirectDebitController` : facade XML dediee au use case powerCARD direct debit.
- `Iso8583GatewayService` : orchestration bout en bout, packing/unpacking ISO, mesure de latence, alimentation du monitoring.
- `Iso8583Codec` : mapping `Iso8583Request` -> `ISOMsg` puis `ISOMsg` -> `Iso8583Response`.
- `Iso8583Channel` : client TCP synchrone avec header de longueur configurable et timeout de lecture/connexion.
- `MonitoringService` : calcul de metriques agreges et retention des evenements recents.
- `RequestLoggingInterceptor` : injection ou propagation du header `X-Request-ID` pour toutes les routes `/api/**`.
- `GlobalExceptionHandler` : normalisation des erreurs de validation, contraintes et exceptions non gerees.
- `FrontendController` : exposition du dashboard, de la racine `/` et des redirections de compatibilite Swagger.

## 3. Flux de traitement

### 3.1 Flux ISO 8583 REST/XML

1. Le client appelle `POST /api/iso8583/*` ou `POST /iso8583/*`.
2. L'intercepteur pose `X-Request-ID` en entree/sortie et mesure la duree HTTP.
3. Bean Validation controle la requete (`mti` valide, `fields` non vide, contraintes de taille sur les champs nommes).
4. `Iso8583Codec` remplit un `ISOMsg` avec les data elements connus et les maps generiques.
5. `Iso8583GatewayService` charge le packager ISO 8583 custom et packe le message.
6. `Iso8583Channel` ouvre un socket TCP, ecrit un header de longueur big-endian puis le payload ISO.
7. Le switch renvoie une trame ISO 8583 ; le gateway lit le header, unpacke la reponse et la convertit en `Iso8583Response`.
8. `Iso8583ResponseStatusResolver` traduit le code ISO 8583 ou les erreurs reseau en statut HTTP.
9. `MonitoringService` enregistre le resultat, la latence et les informations utiles de suivi.

### 3.2 Flux powerCARD direct debit

1. Le client envoie un XML `DirectDebitTransferRequest` sur `POST /api/powercard/direct-debit`.
2. `PowerCardDirectDebitMapper` construit une requete ISO MTI `1200`.
3. Les comptes source et destination sont injectes dans `DE102` et `DE103`.
4. Le champ narratif est mappe vers `DE48`.
5. Si absents, `stan`, `transmissionDateTime`, `localTime`, `localDate`, `retrievalReferenceNumber` et `acquiringInstitutionId` sont derives automatiquement.
6. La reponse ISO est remappee vers `DirectDebitTransferResponse` au format XML.

## 4. Surfaces exposees

### 4.1 API ISO 8583

Base path : `/api/iso8583` avec alias `/iso8583`.

- `POST /send` : envoi libre d'un message ISO 8583.
- `POST /authorize` : force `mti=0100`.
- `POST /financial` : force `mti=0200`.
- `POST /presentment` : force `mti=1200`.
- `POST /reversal` : force `mti=0400`.
- `POST /echo` : envoie un echo test `0800` avec `DE70=301`.
- `GET /config` : retourne la configuration reseau active.
- `GET /health` : sonde simple (`status=UP`).
- `GET /status` : statut applicatif minimal (`status`, `version`, `uptime`).

Caracteristiques :

- `send`, `authorize`, `financial`, `presentment` et `reversal` acceptent et produisent JSON ou XML.
- `echo`, `config`, `health` et `status` exposent JSON ou XML.
- Les routes de compatibilite Swagger `/swagger-ui.html`, `/swagger-ui-html` et `/api/swagger-ui-html` redirigent vers `/api/swagger-ui.html`.

### 4.2 API powerCARD

Base path : `/api/powercard`.

- `POST /direct-debit`
  - `Content-Type` : `application/xml`
  - `Accept` : `application/xml`
  - Use case : facade XML pour un transfert de debit direct mappe vers ISO 8583 MTI `1200`

### 4.3 API de monitoring

Base path : `/api/monitoring`.

- `GET /metrics` : agregats du trafic.
- `GET /events?limit=N` : evenements recents, avec `N` entre 1 et 500.
- `GET /errors?limit=N` : sous-ensemble des evenements en erreur, avec `N` entre 1 et 500.

### 4.4 Actuator et UI

- `GET /api/actuator/health`
- `GET /api/actuator/info`
- `GET /api/actuator/metrics`
- `GET /` : redirection HTML vers `/dashboard/`
- `GET /dashboard/` : dashboard Angular embarque
- `GET /monitoring.html` : redirection legacy vers `/dashboard/`

## 5. Modele ISO 8583 implemente

### 5.1 Packager

Le packager `custom_iso87.xml` declare les data elements `0` a `128` en format ASCII ISO 8583:1987. Il couvre les champs classiques (PAN, processing code, amount, dates, track data, comptes, champs prives, etc.) et autorise donc bien plus que le sous-ensemble initialement documente.

### 5.2 Champs nommes supportes cote requete

`Iso8583Request` expose explicitement les champs suivants :

- `mti`
- `pan` (`DE2`)
- `processingCode` (`DE3`)
- `amount` (`DE4`)
- `transmissionDateTime` (`DE7`)
- `stan` (`DE11`)
- `localTime` (`DE12`)
- `localDate` (`DE13`)
- `expirationDate` (`DE14`)
- `merchantCategoryCode` (`DE18`)
- `posEntryMode` (`DE22`)
- `cardSequenceNumber` (`DE23`)
- `posConditionCode` (`DE25`)
- `acquiringInstitutionId` (`DE32`)
- `track2Data` (`DE35`)
- `retrievalReferenceNumber` (`DE37`)
- `terminalId` (`DE41`)
- `merchantId` (`DE42`)
- `cardAcceptorNameLocation` (`DE43`)
- `currencyCode` (`DE49`)
- `pinData` (`DE52`, hexadecimal binaire)
- `emvData` (`DE55`, hexadecimal TLV)
- `additionalPosData` (`DE60`)
- `networkManagementCode` (`DE70`)
- `originalDataElements` (`DE90`)
- `fields` : map generique `bit -> valeur`
- `additionalFields` : map `Integer -> String` passee telle quelle
- `transactionRef` : reference de suivi cote client

Point important : pour les endpoints publics `POST /api/iso8583/*`, la propriete `fields` est actuellement obligatoire et doit etre non vide, meme si des champs nommes comme `amount` ou `stan` sont renseignes. Cette contrainte vient directement de la validation Bean Validation du modele.

### 5.3 Champs nommes supportes cote reponse

`Iso8583Response` remonte notamment :

- `mti`
- `processingCode` (`DE3`)
- `amount` (`DE4`)
- `transmissionDateTime` (`DE7`)
- `stan` (`DE11`)
- `localTime` (`DE12`)
- `localDate` (`DE13`)
- `retrievalReferenceNumber` (`DE37`)
- `authorizationCode` (`DE38`)
- `responseCode` (`DE39`)
- `terminalId` (`DE41`)
- `merchantId` (`DE42`)
- `currencyCode` (`DE49`)
- `emvResponseData` (`DE55`, re-exprime en hex)
- `networkManagementCode` (`DE70`)
- `additionalFields` pour les data elements non deja nommes
- `responseDescription`
- `timestamp`
- `status`
- `errorMessage`
- `processingTimeMs`

## 6. Regles de statut HTTP

La traduction ISO 8583 -> HTTP est centralisee dans `Iso8583ResponseStatusResolver`.

- `status=ERROR` avec mention de timeout -> `504 Gateway Timeout`
- `status=ERROR` sans timeout -> `503 Service Unavailable`
- `DE39=00` -> `200 OK`
- `DE39=05`, `14`, `41`, `43` -> `403 Forbidden`
- `DE39=51`, `61` -> `402 Payment Required`
- `DE39=91`, `96` -> `503 Service Unavailable`
- autre `DE39` connu ou non -> `400 Bad Request`
- reponse sans code ISO exploitable -> `500 Internal Server Error`

## 7. Configuration

Configuration principale : `src/main/resources/application.yml`

- `server.port=8080`
- `iso8583.host=127.0.0.1`
- `iso8583.port=9000`
- `iso8583.connect-timeout=5000`
- `iso8583.read-timeout=30000`
- `iso8583.header-length=4`
- `iso8583.institution-id=000001`
- logs fichiers : `logs/iso8583-gateway.log`
- niveau de logs : `root=INFO`, `com.atm=DEBUG`
- Actuator : `/api/actuator`, endpoints exposes `health`, `info`, `metrics`
- OpenAPI JSON : `/api/docs`
- Swagger UI : `/api/swagger-ui.html`

Les proprietes `iso8583.*` peuvent etre surchargees via variables d'environnement ou arguments Spring Boot, par exemple :

```bash
ISO8583_HOST=10.10.150.25 ISO8583_PORT=8583 mvn spring-boot:run
```

## 8. Monitoring, logs et gestion des erreurs

- `RequestLoggingInterceptor` trace toutes les routes `/api/**`, propage `X-Request-ID` et journalise la duree de traitement.
- `MonitoringService` conserve jusqu'a 500 evenements recents et calcule :
  - volume total
  - succes / refus / erreurs
  - taux de succes
  - latence moyenne
  - p95
  - min / max
  - transactions observees sur la derniere minute
- Les erreurs d'API sont capturees pour les routes `/api/iso8583/*` et `/api/powercard/*`.
- `GlobalExceptionHandler` retourne :
  - `ErrorResponse` pour les erreurs de validation de payload
  - `ApiError` pour les violations de contraintes, exceptions generiques et routes introuvables

## 9. Deploiement et execution

### 9.1 Local

Prerequis :

- JDK 17+
- Maven 3.8+
- Node.js/NPM si le dashboard Angular doit etre regenere
- Docker en option

Commandes usuelles :

```bash
mvn clean install
```

```bash
mvn test-compile exec:java -Dexec.mainClass=com.atm.iso8583.simulator.Iso8583MockSwitch
```

```bash
mvn spring-boot:run
```

Acces principaux :

- Gateway : `http://localhost:8080`
- Dashboard : `http://localhost:8080/dashboard/`
- Swagger UI : `http://localhost:8080/api/swagger-ui.html`

### 9.2 Regeneration du dashboard Angular

Le code source du dashboard se trouve dans `frontend/`. Les assets servis par Spring sont copies dans `src/main/resources/static/dashboard/`.

```bash
cd frontend
npm install
npm run build:spring
```

### 9.3 Docker

- `Dockerfile` : build multi-stage Maven puis execution sur image Temurin 17 JRE.
- `docker-compose.yml` : deux services `gateway-app` et `mock-switch` sur le reseau `atm-net`.
- Le service `gateway-app` expose `8080:8080`.
- Le service `mock-switch` lance `com.atm.iso8583.simulator.Iso8583MockSwitch 9000`.
- Le compose versionne pointe le gateway vers `ISO8583_HOST=mock-switch` et `ISO8583_PORT=9000`.

Commande :

```bash
docker-compose up --build
```

Note d'exploitation : le fichier `docker-compose.yml` publie actuellement `9000:9002` pour `mock-switch` alors que la commande du conteneur demarre le mock sur `9000`. Si un acces externe direct au mock switch est necessaire, cette correspondance de ports est a verifier.

## 10. Tests et couverture actuelle

Le depot contient des tests automatises pour :

- `Iso8583Codec`
- `RequestLoggingInterceptor`
- `Iso8583Controller`
- `MonitoringController`
- `PowerCardDirectDebitController`
- `MonitoringService`
- `Iso8583Exception`

Les tests couvrent principalement :

- validation des payloads
- presence des endpoints critiques
- propagation de `X-Request-ID`
- format XML powerCARD
- bornes des parametres `limit`
- calcul des metriques de monitoring

## 11. Limites et points d'attention

- Le monitoring est en memoire uniquement ; il n'y a pas de persistance des evenements ni d'export Prometheus natif.
- Le canal TCP est synchrone et ouvre un socket par echange ; il n'y a pas de pool de connexions.
- L'authentification, l'autorisation et le rate limiting ne sont pas implementes.
- Le endpoint `/status` renvoie un champ `uptime` base sur `System.currentTimeMillis()` et non une duree calculee depuis le demarrage.
- La validation actuelle de `Iso8583Request` impose `fields` non vide, ce qui peut surprendre sur les endpoints raccourcis (`/authorize`, `/financial`, `/presentment`, `/reversal`).

## 12. Resume operationnel

Le projet n'est plus seulement un gateway REST JSON vers ISO 8583. Dans son etat actuel, il fournit :

- une facade ISO 8583 en JSON ou XML
- une facade XML dediee a powerCARD direct debit
- un dashboard Angular embarque
- un monitoring applicatif expose par API
- un mock switch pour developpement local

Toute mise a jour future de ce document doit etre alignee en priorite sur :

- `src/main/java/com/atm/iso8583/controller`
- `src/main/java/com/atm/iso8583/service`
- `src/main/java/com/atm/iso8583/codec`
- `src/main/resources/application.yml`
- `src/main/resources/packager/custom_iso87.xml`
- `docker-compose.yml`
