# Script Oral De Soutenance

## Slide 1

Bonjour a toutes et a tous. Je m'appelle Yassine Benaich, et je vais vous presenter mon projet de fin d'etudes realise a Hightech Payment Systems, intitule : Conception et developpement d'une passerelle ISO 8583.

## Slide 2

Le contexte du projet est lie a la modernisation des systemes de paiement. Aujourd'hui, beaucoup d'applications utilisent des API REST, alors que les infrastructures de paiement continuent de s'appuyer sur la norme ISO 8583 et sur des echanges TCP/IP.

## Slide 3

La problematique etait donc la suivante : comment permettre a des applications modernes de communiquer simplement avec un switch bancaire ISO 8583, sans exposer toute la complexite technique du protocole ?

## Slide 4

Pour repondre a cette problematique, mes objectifs etaient de concevoir une passerelle capable de convertir des requetes JSON ou XML en ISO 8583, de dialoguer avec un switch via TCP, d'ajouter un monitoring temps reel et de preparer le deploiement avec Docker.

## Slide 5

L'architecture retenue repose sur un client applicatif, une passerelle Spring Boot, un moteur de mapping base sur jPOS, un canal reseau TCP/IP, un switch bancaire ou un mock switch, ainsi qu'un dashboard Angular pour la supervision.

## Slide 6

Du point de vue technique, j'ai utilise Java 17, Spring Boot 3.2.3, jPOS 2.1.10 et Angular 20. Le backend expose plusieurs endpoints ISO 8583 ainsi qu'une facade XML dediee a un cas d'usage powerCARD.

## Slide 7

Le fonctionnement global est simple : la requete est recue et validee, puis transformee en message ISO 8583. Ce message est ensuite envoye au switch via TCP/IP. La reponse est recue, decodee, transformee en objet applicatif puis renvoyee au client sous forme JSON ou XML.

## Slide 8

J'ai egalement developpe un module de monitoring qui permet de suivre le nombre total de transactions, le taux de succes, la latence moyenne, le P95 ainsi que les erreurs recentes. Ces informations sont affichees dans un dashboard Angular mis a jour en temps reel.

## Slide 9

Pour la validation, le projet comprend des tests unitaires et des tests d'integration, une documentation Swagger, une collection Postman et un mock switch local qui permet de simuler le comportement du systeme distant.

## Slide 10

Le principal resultat est la mise en place d'une passerelle fonctionnelle, documentee, testable et observable. Elle permet de masquer la complexite d'ISO 8583 et de proposer une interface moderne pour des clients applicatifs.

## Slide 11

Parmi les limites actuelles, on peut citer le monitoring en memoire, l'absence de securisation avancee et le caractere synchrone du canal TCP. Les principales perspectives concernent la persistance, l'observabilite avancee, la resilience reseau et l'industrialisation.

## Slide 12

Pour conclure, ce projet m'a permis de travailler sur une problematique reelle d'interoperabilite dans le domaine des paiements. Il m'a permis de consolider mes competences en backend, integration, protocoles financiers et supervision applicative. Je vous remercie pour votre attention.
