# Presentation De Soutenance

## Slide 1 - Titre

- Conception et developpement d'une passerelle ISO 8583
- YASSINE BENAICH
- ENSET Mohammedia
- Hightech Payment Systems
- Periode : 13/01/2026 au 12/06/2026

## Slide 2 - Contexte

- Modernisation des systemes de paiement
- Coexistence entre API modernes et switchs legacy
- Besoin d'interoperabilite entre HTTP/JSON et ISO 8583/TCP

## Slide 3 - Problematique

- Les applications modernes ne manipulent pas nativement ISO 8583
- Les echanges avec les switchs sont complexes a tester et superviser
- Besoin d'une passerelle simple, maintenable et observable

## Slide 4 - Objectifs

- Convertir JSON/XML en ISO 8583
- Communiquer avec un switch via TCP/IP
- Superviser le trafic en temps reel
- Fournir un mock switch pour les tests
- Preparer le deploiement avec Docker

## Slide 5 - Architecture

- Client REST / XML
- Spring Boot Gateway
- jPOS + packager ISO 8583
- Canal TCP/IP
- Switch bancaire ou mock switch
- Dashboard Angular

## Slide 6 - Realisation backend

- Java 17
- Spring Boot 3.2.3
- jPOS 2.1.10
- Endpoints `/api/iso8583/*`
- Facade `/api/powercard/direct-debit`

## Slide 7 - Fonctionnement

- Reception de la requete
- Validation
- Mapping vers `ISOMsg`
- Packing
- Envoi TCP
- Reception reponse
- Unpacking
- Reponse HTTP

## Slide 8 - Monitoring

- Total transactions
- Taux de succes
- P95 latence
- Evenements recents
- Erreurs recentes
- Dashboard Angular temps reel

## Slide 9 - Tests

- Tests unitaires
- Tests d'integration
- Validation via Swagger
- Collection Postman
- Mock switch local

## Slide 10 - Resultats

- Passerelle fonctionnelle
- Simulation locale complete
- API documentee
- Monitoring embarque
- Deploiement conteneurise

## Slide 11 - Limites et perspectives

- Monitoring en memoire
- Canal TCP synchrone
- Pas encore d'authentification
- Perspectives : Prometheus, securite, circuit breaker, persistance

## Slide 12 - Conclusion

- Projet utile pour l'interoperabilite monetique
- Competences acquises en integration, backend, protocoles et observabilite
- Ouverture sur une industrialisation future
