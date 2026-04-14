# Rapport PFE Academique Long

## Titre

**Conception et developpement d'une passerelle ISO 8583**

## Introduction generale

L'evolution rapide des services numeriques a profondement transforme les attentes des utilisateurs et des institutions financieres. Les systemes modernes doivent etre plus ouverts, plus rapides a integrer et plus faciles a superviser. Cependant, dans l'univers monetique, une grande partie des infrastructures repose encore sur des standards historiques tels que la norme ISO 8583. Cette norme, largement adoptee dans les transactions par carte bancaire, constitue un langage commun entre terminaux, ATM, switchs et systemes d'autorisation. Malgre sa robustesse, elle reste peu adaptee a une consommation directe par des applications web modernes.

Le projet realise au sein de Hightech Payment Systems vise ainsi a mettre en place une passerelle logicielle capable de servir d'intermediaire intelligent entre un monde applicatif oriente API REST et un monde transactionnel oriente messages ISO 8583 transmis via TCP/IP. Une telle passerelle permet non seulement de simplifier l'integration des services de paiement, mais aussi d'ameliorer la testabilite, la tracabilite et l'observabilite de l'ensemble des echanges.

## Chapitre 1 : Entreprise et cadre du stage

### 1.1 Presentation de l'entreprise

Hightech Payment Systems evolue dans le domaine des solutions de paiement, un secteur ou les exigences techniques sont elevees et ou les notions de disponibilite, de securite et de compatibilite sont structurantes. Les plateformes de paiement doivent garantir la continuite des echanges, la lisibilite des integrations et le respect des standards du secteur.

### 1.2 Contexte du stage

Le stage s'est deroule du 13 janvier 2026 au 12 juin 2026 a Casablanca. Il s'inscrit dans une logique de modernisation et d'abstraction des echanges monetiques. L'idee centrale etait de proposer une couche logicielle intermediaire capable de rendre les flux ISO 8583 plus accessibles a des clients applicatifs modernes.

### 1.3 Mission principale

La mission consistait a concevoir puis developper une passerelle ISO 8583 capable de :

- recevoir des requetes REST en JSON ou XML,
- convertir ces requetes en messages ISO 8583,
- transmettre les messages a un switch via TCP/IP,
- recevoir, decoder et traduire les reponses,
- exposer un monitoring lisible par les equipes techniques,
- offrir un environnement autonome de test grace a un mock switch.

## Chapitre 2 : Etude de l'existant et expression du besoin

### 2.1 ISO 8583 et systemes de paiement

ISO 8583 est une norme de messagerie largement utilisee dans les systemes bancaires. Elle organise les donnees en message type indicator, bitmap et data elements. Chaque data element correspond a une information metier precise telle que le numero de carte, le montant, le type de traitement, l'identifiant terminal ou le code de reponse. Cette structure est optimisee pour les environnements transactionnels mais reste peu intuitive pour les clients applicatifs n'ayant pas de connaissance protocolaire.

### 2.2 Limites de l'approche traditionnelle

Dans une architecture classique, toute application souhaitant parler a un switch ISO 8583 doit maitriser le format des messages, leur serialization, la gestion du canal TCP, le framing, la lecture des reponses et l'interpretation des codes retour. Cette approche augmente la complexite technique et rend les integrations plus fragiles. Elle complique egalement les tests et la supervision.

### 2.3 Besoins fonctionnels

Le projet devait prendre en charge :

- l'envoi libre de messages ISO 8583,
- des routes raccourcies pour les types courants comme l'autorisation, la transaction financiere, le presentment, le reversal et l'echo test,
- une facade XML dediee a un cas powerCARD direct debit,
- un monitoring temps reel,
- un simulateur local de switch,
- une documentation exploitable par les developpeurs.

### 2.4 Besoins non fonctionnels

La solution devait egalement etre :

- modulaire,
- maintenable,
- testable localement,
- observable,
- facilement deployable,
- extensible vers de nouveaux cas d'usage.

## Chapitre 3 : Analyse et conception

### 3.1 Architecture generale

L'architecture retenue est de type en couches. Cette approche facilite la comprehension du systeme et permet d'isoler les responsabilites. Les controleurs traitent les requetes entrantes. Les services orchestrent la logique metier. Le codec gere la transformation vers ISO 8583. Le canal reseau se charge du dialogue TCP. Le monitoring suit le comportement global du systeme.

### 3.2 Composants principaux

- `Iso8583Controller` : facade REST principale.
- `Iso8583GatewayService` : orchestration du traitement complet.
- `Iso8583Codec` : conversion `JSON/XML -> ISO 8583 -> JSON/XML`.
- `Iso8583Channel` : transport TCP/IP.
- `MonitoringService` : metriques et historique recent.
- `MonitoringController` : exposition des donnees de supervision.
- `PowerCardDirectDebitController` : adaptation XML metier.
- `Iso8583MockSwitch` : simulateur de switch.

### 3.3 Flux de traitement

Le flux standard suit les etapes suivantes :

1. reception de la requete HTTP,
2. validation des donnees,
3. mapping vers `ISOMsg`,
4. packing via le packager jPOS,
5. envoi au switch par socket TCP,
6. reception de la reponse,
7. unpacking,
8. conversion en objet de sortie,
9. mise a jour des metriques de monitoring,
10. retour au client.

### 3.4 Choix technologiques

Java 17 a ete retenu pour sa maturite et sa stabilite. Spring Boot 3.2.3 a ete choisi pour la structuration rapide du backend et sa richesse en termes de validation, de configuration et de web services. jPOS 2.1.10 s'est impose pour la gestion fiable des messages ISO 8583. Angular 20 a ete retenu pour realiser un dashboard reactif, moderne et directement integrable dans les ressources statiques du backend.

## Chapitre 4 : Realisation

### 4.1 Backend et configuration

Le projet backend expose ses services sur le port `8080`. La configuration ISO 8583 permet de definir l'hote cible, le port, les timeouts, la longueur du header reseau et l'identifiant d'institution. Cette parametrisation rend la solution souple et adaptable a plusieurs environnements.

### 4.2 Endpoints ISO 8583

Les endpoints exposes sont :

- `/api/iso8583/send`
- `/api/iso8583/authorize`
- `/api/iso8583/financial`
- `/api/iso8583/presentment`
- `/api/iso8583/reversal`
- `/api/iso8583/echo`
- `/api/iso8583/config`
- `/api/iso8583/health`
- `/api/iso8583/status`

Ils sont completes par :

- `/api/powercard/direct-debit`
- `/api/monitoring/metrics`
- `/api/monitoring/events`
- `/api/monitoring/errors`
- `/dashboard/`

### 4.3 Mapping ISO 8583

Le codec gere explicitement plusieurs champs importants :

- `DE2` : PAN
- `DE3` : processing code
- `DE4` : montant
- `DE7` : date et heure de transmission
- `DE11` : STAN
- `DE37` : retrieval reference number
- `DE41` : terminal ID
- `DE42` : merchant ID
- `DE49` : currency code
- `DE52` : PIN data
- `DE55` : donnees EMV
- `DE70` : code de gestion reseau

En plus de ces champs nommes, des maps generiques permettent de transporter d'autres data elements sans modifier le modele principal.

### 4.4 Facade powerCARD

La facade XML powerCARD constitue une extension metier du gateway. Elle permet d'accepter un message XML `DirectDebitTransferRequest`, de le transformer en message `1200` et de remonter une reponse XML structuree. Les champs `DE102` et `DE103` representent respectivement les comptes source et destination. Le champ `DE48` est utilise pour transporter le narratif lorsqu'il est present.

### 4.5 Gestion des erreurs

Le projet met en oeuvre plusieurs mecanismes de robustesse :

- validation Bean Validation sur les payloads,
- interceptors de journalisation avec `X-Request-ID`,
- gestion centralisee des exceptions,
- traduction des codes ISO vers des statuts HTTP adaptes,
- enregistrement des erreurs dans le monitoring.

### 4.6 Monitoring et observabilite

Le module de monitoring enregistre le volume de transactions, le nombre de succes, de refus et d'erreurs, la latence moyenne, la latence P95 ainsi que les evenements recents. Cette couche donne une visibilite immediate sur le comportement du systeme. Le dashboard Angular transforme ces metriques en visualisations accessibles aux developpeurs et aux responsables techniques.

## Chapitre 5 : Tests, validation et deploiement

### 5.1 Tests

Le depot contient des tests pour :

- le codec ISO 8583,
- le controleur principal,
- la facade powerCARD,
- le monitoring,
- l'intercepteur de journalisation.

Ces tests couvrent la validation des requetes, la disponibilite des endpoints, la propagation du `X-Request-ID`, le calcul des metriques et le mapping XML/ISO.

### 5.2 Mock switch

Le `Iso8583MockSwitch` simplifie considerablement la validation fonctionnelle. Il permet de simuler un comportement minimal du switch bancaire, d'envoyer des reponses approuvees et de verifier la chaine complete sans dependance externe.

### 5.3 Documentation

La solution est documentee via Swagger/OpenAPI. Une collection Postman complete le dispositif en proposant plusieurs scenarios de test preconfigures. Cela rend l'appropriation du projet plus rapide et facilite la demonstration.

### 5.4 Deploiement

Le deploiement a ete prepare au moyen de Docker et Docker Compose. Cette conteneurisation permet de lancer le gateway et le mock switch de maniere reproductible. Le projet est ainsi mieux positionne pour une integration dans un cycle CI/CD.

### 5.5 Limites observees

Plusieurs limites doivent etre relevees :

- monitoring non persistant,
- canal TCP synchrone avec un socket par echange,
- absence d'authentification et de rate limiting,
- validation du modele d'entree parfois stricte,
- necessite d'une verification du mapping de port du mock switch dans Docker Compose si exposition externe attendue.

## Chapitre 6 : Bilan et perspectives

### 6.1 Apports techniques

Ce projet m'a permis de progresser sur :

- l'integration de protocoles bancaires,
- le developpement backend avec Spring Boot,
- la structuration d'une architecture modulaire,
- l'observabilite et le monitoring,
- la conteneurisation,
- la conception d'interfaces de supervision.

### 6.2 Apports methodologiques

Le travail m'a egalement appris a raisonner de maniere plus rigoureuse sur la separation des responsabilites, la testabilite, la documentation et la prise en compte des limites techniques des solutions developpees.

### 6.3 Perspectives

Les evolutions les plus pertinentes sont :

- ajout d'une persistance des evenements,
- integration Prometheus/Grafana,
- securisation des endpoints,
- resilience reseau avec circuit breaker,
- optimisation du transport TCP,
- integration avec un veritable switch bancaire.

## Conclusion generale

Le projet `ATM ISO 8583 Gateway` constitue une reponse concrete a un besoin d'interoperabilite entre services applicatifs modernes et infrastructures monetiques historiques. En encapsulant la complexite d'ISO 8583 derriere une API claire et documentee, la solution ameliore la maintenabilite, la testabilite et l'observabilite des integrations de paiement. Ce travail represente une experience formatrice majeure, a la croisee de l'architecture logicielle, de l'integration applicative et du domaine des paiements electroniques.

## Annexes conseillees

- capture Swagger UI,
- capture dashboard Angular,
- diagramme de sequence,
- diagramme de deploiement,
- exemple JSON,
- exemple XML powerCARD,
- extrait de collection Postman.
