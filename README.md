# 🛡️ UserAdviceSecurity System

**Spring Boot, Rôles & Permissions, MariaDB**

---

## 💡 Description du Projet

**UserAdviceSecurity** est une application moderne et robuste développée avec **Spring Boot**, conçue pour une gestion complète des utilisateurs et de la sécurité.

Elle intègre les fonctionnalités clés suivantes :

* **Gestion des Utilisateurs** : Inscription, authentification, et assignation de rôles.
* **Sécurité Granulaire** : Gestion des permissions via l'énumération **`PermissionEnum`**.
* **Rôles Dynamiques** : Support des rôles **`ROLE_USER`** et **`ROLE_ADMIN`**.
* **Initialisation Automatique** : Configuration et insertion par défaut des permissions, des rôles, et du compte administrateur.
* **Déploiement Simplifié** : Support **Docker** ou environnements locaux (XAMPP / WAMP).
* **Outils Intégrés** : Configuration avec **Adminer**, **PhpMyAdmin**, et **SMTP4DEV** pour le développement.

---

## 🔑 Rôles & Permissions

La sécurité est gérée par des rôles et un ensemble de permissions bien définies.

### `PermissionEnum`

| Catégorie | Permissions | Description |
| :--- | :--- | :--- |
| **Utilisateur** | `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE` | Gérer les informations des utilisateurs. |
| **Conseils** | `ADVICE_READ`, `ADVICE_CREATE`, `ADVICE_UPDATE`, `ADVICE_DELETE` | Gérer les conseils (ressource principale de l'application). |
| **Système** | `SYSTEM_CONFIG`, `SYSTEM_MONITORING` | Accès aux configurations et au monitoring système. |

### Définition des Rôles

* **Rôle par défaut : `ROLE_USER`**
    * **Accès Standard** : Lire les conseils, créer des conseils, lire son propre profil.
* **Rôle Administrateur : `ROLE_ADMIN`**
    * **Accès Complet** : Accès à **toutes les permissions** du système.

### 👤 Utilisateur Admin par Défaut

Le système est initialisé avec un compte administrateur pour les tests :

| Champ | Valeur |
| :--- | :--- |
| **Email** | `admin@gmail.com` |
| **Mot de passe** | `admin123` |

---

## 🛠️ Prérequis

Pour lancer le projet, assurez-vous d'avoir les éléments suivants installés :

* **Java 17+**
* **Maven**
* **Docker** (recommandé) ou **XAMPP/WAMP**

---

## 🚀 Lancer l'Application

### Option 1 : 🐳 Avec Docker Compose (Recommandé)

Lancez l'application et ses services dépendants (MariaDB, Adminer, etc.) avec une seule commande :

```bash
docker-compose up -d
