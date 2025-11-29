UserAdviceSecurity System — Spring Boot, Roles & Permissions, MariaDB

Description

UserAdviceSecurity est une application Spring Boot moderne intégrant :
- Gestion des utilisateurs (inscription, authentification, rôles)
- Permissions via PermissionEnum
- Rôles dynamiques : ROLE_USER et ROLE_ADMIN
- Assignation automatique d’un rôle par défaut
- Initialisation automatique (permissions + rôles + admin)
- Support Docker / XAMPP / WAMP
- Outils : Adminer, PhpMyAdmin, SMTP4DEV

Architecture du projet

src/main/java/
 └── BlackAdhuleSystem/dev/userAdvicesMariadb/
      ├── config/
      │     ├── SecurityConfig.java
      │     └── DataInitializer.java
      ├── controller/
      ├── dto/
      ├── entity/
      ├── mapper/
      ├── repository/
      ├── services/
      └── UserAdvicesMariadbApplication.java

Rôles & Permissions

PermissionEnum
USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE  
ADVICE_READ, ADVICE_CREATE, ADVICE_UPDATE, ADVICE_DELETE  
SYSTEM_CONFIG, SYSTEM_MONITORING  

Rôle par défaut : ROLE_USER
- Lire les conseils  
- Créer des conseils  
- Lire son profil  

Rôle administrateur : ROLE_ADMIN
- Toutes les permissions  

Utilisateur admin par défaut
email: admin@gmail.com  
password: admin123  

Prérequis
Java 17+, Maven, Docker ou XAMPP/WAMP

Option 1 : Lancer avec Docker Compose

docker-compose up -d

Adminer → http://localhost:9080  
PhpMyAdmin → http://localhost:9090  
SMTP4DEV → http://localhost:5001  

Option 2 : Utiliser XAMPP / WAMP

Créer la base : useradvicesmariadb  
Configurer application.properties  

Lancer l’application
mvn spring-boot:run

Connexion API
POST /api/login  
{ "email": "admin@gmail.com", "password": "admin123" }

Licence
MIT
