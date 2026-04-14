# Note De Synthese Ultra Formelle

## Objet

Le present document constitue une synthese formelle du projet de fin d'etudes intitule **Conception et developpement d'une passerelle ISO 8583**, realise par **YASSINE BENAICH** dans le cadre de la formation **Big Data and Cloud Computing** de l'**ENSET Mohammedia**, au sein de **Hightech Payment Systems**, durant la periode allant du **13 janvier 2026** au **12 juin 2026**.

## Contexte

Les systemes de paiement electronique reposent encore largement sur des infrastructures heritagees exploitant la norme ISO 8583 pour les echanges monetiques. En parallele, les architectures applicatives contemporaines privilegient des interfaces REST, des formats JSON/XML et des mecanismes d'integration documentes. Cette dualite cree une fracture technique entre les couches modernes et les couches transactionnelles historiques.

## Finalite du projet

Le projet a eu pour finalite de concevoir et de realiser une passerelle capable :

- d'abstraire la complexite du protocole ISO 8583,
- de fournir une interface applicative moderne pour les transactions monetiques,
- de garantir la communication avec un switch via TCP/IP,
- d'assurer la supervision des flux,
- et de permettre une validation autonome du systeme grace a un mock switch.

## Solution mise en oeuvre

La solution developpee repose sur une architecture modulaire composee :

- d'une couche d'exposition REST et XML,
- d'une couche de service assurant l'orchestration complete des transactions,
- d'un codec de transformation vers et depuis le format ISO 8583,
- d'un canal TCP/IP dedie a la communication avec le switch,
- d'un module de monitoring applicatif,
- et d'un dashboard Angular pour la visualisation des indicateurs.

Le backend a ete realise avec **Java 17** et **Spring Boot 3.2.3**. La gestion du protocole ISO 8583 s'appuie sur **jPOS 2.1.10** et sur un packager XML personnalise. Le frontend de supervision a ete developpe avec **Angular 20**. La solution est documentee via **Swagger/OpenAPI** et preparee au deploiement a l'aide de **Docker** et **Docker Compose**.

## Apports majeurs

Les apports principaux du projet peuvent etre resumes comme suit :

- simplification de l'integration entre applications modernes et switchs ISO 8583,
- amelioration de la lisibilite et de la tracabilite des echanges,
- mise a disposition d'un environnement de test autonome,
- ajout d'une couche d'observabilite temps reel,
- structuration d'une base technique evolutive pour de futurs cas d'usage.

## Limites et prolongements

L'etat actuel de la solution revele toutefois certaines limites, notamment l'absence de persistance des metriques de monitoring, le caractere synchrone du canal reseau et l'absence de mecanismes de securisation avances. Ces points constituent des axes prioritaires d'evolution pour une future industrialisation de la passerelle.

## Conclusion

Ce projet repond a un besoin concret d'interoperabilite dans le domaine des paiements electroniques. Il illustre la capacite a faire converger des systemes modernes et des infrastructures heritagees a travers une approche logicielle structuree, documentee et orientee qualite. Il constitue egalement une experience formatrice significative sur les plans technique, methodologique et metier.
