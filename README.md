
# UserAdviceSecurity System — Spring Boot, Roles & Permissions, MariaDB

## 📖 Description
**UserAdviceSecurity** est une application moderne basée sur **Spring Boot** intégrant :
- Gestion des utilisateurs (inscription, authentification, rôles)
- Permissions via `PermissionEnum`
- Rôles dynamiques : `ROLE_USER` et `ROLE_ADMIN`
- Assignation automatique d’un rôle par défaut
- Initialisation automatique (permissions + rôles + admin)
- Support **Docker / XAMPP / WAMP**
- Outils intégrés : **Adminer**, **PhpMyAdmin**, **SMTP4DEV**

---

## 🏗️ Architecture du projet
src/main/java/ └── BlackAdhuleSystem/dev/userAdvicesMariadb/ ├── config/ │ ├── SecurityConfig.java │ └── DataInitializer.java ├── controller/ ├── dto/ ├── entity/ ├── mapper/ ├── repository/ ├── services/ └── UserAdvicesMariadbApplication.java

Code

---

## 🔐 Rôles & Permissions

### PermissionEnum
- **Utilisateur** : `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE`  
- **Conseils** : `ADVICE_READ`, `ADVICE_CREATE`, `ADVICE_UPDATE`, `ADVICE_DELETE`  
- **Système** : `SYSTEM_CONFIG`, `SYSTEM_MONITORING`  

### Rôle par défaut : `ROLE_USER`
- Lire les conseils  
- Créer des conseils  
- Lire son profil  

### Rôle administrateur : `ROLE_ADMIN`
- Accès à **toutes les permissions**

### 👤 Utilisateur admin par défaut
- **Email** : `admin@gmail.com`  
- **Password** : `admin123`  

---

## ⚙️ Prérequis
- **Java 17+**
- **Maven**
- **Docker** ou **XAMPP/WAMP**

---

## 🚀 Lancer l’application

### Option 1 : Avec Docker Compose

docker-compose up -d
Adminer → http://localhost:9080

PhpMyAdmin → http://localhost:9090

SMTP4DEV → http://localhost:5001

Option 2 : Avec XAMPP / WAMP
Créer la base : useradvicesmariadb

Configurer application.properties

Lancer l’application :

bash
mvn spring-boot:run
🔗 Connexion API
Endpoint :

http
POST /api/login
Body :

json
{
  "email": "admin@gmail.com",
  "password": "admin123"
}
📜 Licence
MIT

Code

---

👉 Ce format est directement prêt à être utilisé comme **README.md** sur GitHub : titres clairs, sections séparées, code formaté, et liens cliquables.  

Veux-tu que je rajoute un **schéma visuel (diagramme ER)** pour illustrer la relation **Utilisateur ↔ Rôle ↔ Permission** afin de rendre le README encore plus professionnel ?
faite en sorte que l'ensemble soit copier
Parfait 👍, voici ton README.md final prêt à être copié-collé directement dans ton projet GitHub :

markdown
# UserAdviceSecurity System — Spring Boot, Roles & Permissions, MariaDB

## 📖 Description
**UserAdviceSecurity** est une application moderne basée sur **Spring Boot** intégrant :
- Gestion des utilisateurs (inscription, authentification, rôles)
- Permissions via `PermissionEnum`
- Rôles dynamiques : `ROLE_USER` et `ROLE_ADMIN`
- Assignation automatique d’un rôle par défaut
- Initialisation automatique (permissions + rôles + admin)
- Support **Docker / XAMPP / WAMP**
- Outils intégrés : **Adminer**, **PhpMyAdmin**, **SMTP4DEV**

---

## 🏗️ Architecture du projet
src/main/java/ └── BlackAdhuleSystem/dev/userAdvicesMariadb/ ├── config/ │ ├── SecurityConfig.java │ └── DataInitializer.java ├── controller/ ├── dto/ ├── entity/ ├── mapper/ ├── repository/ ├── services/ └── UserAdvicesMariadbApplication.java

Code

---

## 🔐 Rôles & Permissions

### PermissionEnum
- **Utilisateur** : `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE`  
- **Conseils** : `ADVICE_READ`, `ADVICE_CREATE`, `ADVICE_UPDATE`, `ADVICE_DELETE`  
- **Système** : `SYSTEM_CONFIG`, `SYSTEM_MONITORING`  

### Rôle par défaut : `ROLE_USER`
- Lire les conseils  
- Créer des conseils  
- Lire son profil  

### Rôle administrateur : `ROLE_ADMIN`
- Accès à **toutes les permissions**

### 👤 Utilisateur admin par défaut
- **Email** : `admin@gmail.com`  
- **Password** : `admin123`  

---

## ⚙️ Prérequis
- **Java 17+**
- **Maven**
- **Docker** ou **XAMPP/WAMP**

---

## 🚀 Lancer l’application

### Option 1 : Avec Docker Compose

docker-compose up -d
Adminer → http://localhost:9080

PhpMyAdmin → http://localhost:9090

SMTP4DEV → http://localhost:5001

Option 2 : Avec XAMPP / WAMP
Créer la base : useradvicesmariadb

Configurer application.properties

Lancer l’application :

bash
mvn spring-boot:run
🔗 Connexion API
Endpoint :

http
POST /api/login
Body :

json
{
  "email": "admin@gmail.com",
  "password": "admin123"
}
📜 Licence
MIT

Code

---

Tu peux maintenant **copier-coller** ce bloc tel quel dans ton fichier `README.md` et il sera parfaitement formaté pour GitHub 🎉.  

Veux-tu que je t’ajoute aussi un **diagramme entité-relation (Utilisateur ↔ Rôle ↔ Permission)** en Markdown pour enrichir encore ton README ?
