# OnboardFlow Backend

Backend REST de **OnboardFlow**, une application de gestion d'utilisateurs et de parcours d'onboarding.  
Le projet est développé en **Kotlin** avec **Spring Boot**, utilise **MySQL** pour la persistance, **JWT** pour l'authentification, **Spring Mail/Mailpit** pour la vérification d'adresse e-mail et **OpenAPI/Swagger** pour la documentation de l'API.

> Version API : `1.0.0`  
> Version du projet : `0.0.1-SNAPSHOT`

---

## Sommaire

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Structure du projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Installation](#installation)
  - [Avec Docker](#avec-docker-recommandé)
  - [En local](#en-local)
- [Configuration](#configuration)
- [Variables d'environnement](#variables-denvironnement)
- [Lancement](#lancement)
- [Base de données](#base-de-données)
- [Authentification JWT](#authentification-jwt)
- [API](#api)
  - [Inscription](#1-inscription)
  - [Vérification e-mail](#2-vérification-e-mail)
  - [Renvoyer l'e-mail de vérification](#3-renvoyer-le-mail-de-vérification)
  - [Connexion](#4-connexion)
  - [Profil courant](#5-profil-courant)
  - [Modification du profil](#6-modification-du-profil)
  - [Rafraîchir les tokens](#7-rafraîchir-les-tokens)
  - [Déconnexion](#8-déconnexion)
  - [Administration](#9-administration-des-utilisateurs)
- [Swagger / OpenAPI](#swagger--openapi)
- [E-mails de développement](#e-mails-de-développement)
- [Rate limiting](#rate-limiting)
- [Tests](#tests)
- [Docker](#docker)
- [Compte administrateur initial](#compte-administrateur-initial)
- [Modèle de données](#modèle-de-données)
- [Sécurité](#sécurité)
- [Points d'attention](#points-dattention)
- [Dépannage](#dépannage)
- [Améliorations recommandées](#améliorations-recommandées)
- [Licence](#licence)

---

## Présentation

OnboardFlow Backend fournit une API REST permettant de gérer le cycle de vie d'un utilisateur :

1. création d'un compte ;
2. envoi d'un e-mail de vérification ;
3. activation du compte après vérification ;
4. connexion avec génération d'un access token et d'un refresh token ;
5. consultation et modification du profil ;
6. renouvellement sécurisé du refresh token ;
7. déconnexion ;
8. consultation paginée des utilisateurs côté administration.

Le backend est conçu comme une API **stateless** : aucune session HTTP classique n'est utilisée. L'identité de l'utilisateur est transportée par un JWT d'accès.

---

## Fonctionnalités

### Gestion des comptes

- Inscription avec validation des données.
- Vérification du format de l'adresse e-mail.
- Politique de mot de passe :
  - minimum 8 caractères ;
  - au moins une majuscule ;
  - au moins une minuscule ;
  - au moins un chiffre ;
  - au moins un caractère spécial.
- Hashage des mots de passe avec **BCrypt**.
- Vérification de l'adresse e-mail.
- Renvoi du lien de vérification.
- Consultation du profil.
- Modification du nom complet, mot de passe, statut et image de profil.

### Authentification

- Access token JWT.
- Refresh token JWT.
- Access token valable **1 heure**.
- Refresh token valable **30 jours**.
- Rotation du refresh token lors du renouvellement.
- Stockage du refresh token sous forme de hash SHA-256 en base.
- Suppression du refresh token à la déconnexion.
- Une seule session refresh token est conservée par utilisateur.

### Sécurité

- Spring Security.
- Filtre d'authentification JWT.
- API stateless.
- Protection contre les tentatives excessives de connexion avec Bucket4j.
- Réponses JSON pour les erreurs `401 Unauthorized`.
- Validation des données avec Jakarta Validation.

### Administration

- Liste paginée des utilisateurs.
- Filtrage par rôle.
- Filtrage par statut de vérification e-mail.
- Recherche par nom ou e-mail.
- Tri et pagination avec les mécanismes Spring Data.

### Documentation

- OpenAPI 3.
- Swagger UI.
- Schéma d'authentification `Bearer JWT`.

---

## Stack technique

| Technologie | Utilisation |
|---|---|
| Kotlin | Langage principal |
| Java 17 | Runtime / toolchain |
| Spring Boot 4.1.0 | Framework backend |
| Spring Web | API REST |
| Spring Security | Sécurité |
| Spring Data JPA | Accès aux données |
| Hibernate | ORM |
| MySQL 8 | Base de données |
| JJWT 0.12.6 | Création et validation JWT |
| BCrypt | Hashage des mots de passe |
| Bucket4j 8.10.1 | Rate limiting |
| Spring Mail | Envoi des e-mails |
| Mailpit | Capture des e-mails en développement |
| SpringDoc OpenAPI 2.3.0 | Swagger / OpenAPI |
| Spring Actuator | Health check |
| Gradle | Build et dépendances |
| Docker | Conteneurisation |

---

## Architecture

Le projet suit une organisation inspirée d'une architecture en couches / Clean Architecture :

```text
src/main/kotlin/com/example/onboardflow/
│
├── api/
│   ├── controllers/
│   │   ├── AuthControllers.kt
│   │   └── AdminController.kt
│   │
│   ├── dto/
│   │   └── PageResponse.kt
│   │
│   └── exception/
│       └── GlobalHandlerException.kt
│
├── application/
│   └── service/
│       ├── AuthService.kt
│       ├── AdminUserService.kt
│       └── EmailService.kt
│
├── domain/
│   ├── exceptions/
│   ├── model/
│   │   ├── User.kt
│   │   ├── RefreshToken.kt
│   │   ├── Audit.kt
│   │   └── DomainEnums.kt
│   │
│   └── repository/
│       ├── UserRepository.kt
│       ├── RefreshTokenRepository.kt
│       └── EmailVerificationTokenRepository.kt
│
└── infrastructure/
    ├── config/
    │   ├── DatabaseSeeder.kt
    │   └── OpenApiSwagger.kt
    │
    └── security/
        ├── HashEncoder.kt
        ├── JwtAuthFilter.kt
        ├── JwtService.kt
        ├── RateLimitFilter.kt
        └── SecurityConfig.kt
```

### Rôle des principales couches

**API**

Expose les endpoints HTTP, valide les requêtes et retourne les réponses.

**Application**

Contient la logique métier, notamment l'inscription, la connexion, la gestion du profil, les tokens et les e-mails.

**Domain**

Contient les entités métier, les enums, les exceptions et les contrats de repository.

**Infrastructure**

Contient les implémentations et mécanismes techniques : sécurité JWT, hashage, configuration Swagger et initialisation de la base.

---

## Structure du projet

```text
OnboardingFlow-Backend/
├── Dockerfile
├── docker-compose.yml
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│
└── src/
    ├── main/
    │   ├── kotlin/
    │   └── resources/
    │       └── application.properties
    │
    └── test/
        ├── kotlin/
        └── resources/
            └── application.properties
```

---

## Prérequis

### Pour Docker

- Docker
- Docker Compose

### Pour un lancement local

- Java 17
- MySQL 8
- Git
- Gradle Wrapper inclus dans le projet

Il n'est pas nécessaire d'installer Gradle globalement : le projet fournit `gradlew` et `gradlew.bat`.

---

# Installation

## Avec Docker (recommandé)

Le projet fournit un `docker-compose.yml` qui démarre trois services :

```text
┌──────────────────────────────┐
│        OnboardFlow API       │
│        Spring Boot :8080     │
└──────────────┬───────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
┌─────────────┐  ┌──────────────┐
│   MySQL 8   │  │   Mailpit    │
│   :3307     │  │ :8025 / :1025│
└─────────────┘  └──────────────┘
```

### 1. Cloner le projet

```bash
git clone <URL_DU_REPOSITORY>
cd OnboardingFlow-Backend
```

### 2. Générer un secret JWT

Le projet attend une valeur Base64 dans `JWT_SECRET_BASE64`.

Exemple :

```bash
openssl rand -base64 32
```

Conservez la valeur obtenue.

### 3. Définir la variable

Linux / macOS :

```bash
export JWT_SECRET_BASE64="VOTRE_SECRET_BASE64"
```

Windows PowerShell :

```powershell
$env:JWT_SECRET_BASE64="VOTRE_SECRET_BASE64"
```

### 4. Démarrer les services

```bash
docker compose up --build
```

L'API sera disponible sur :

```text
http://localhost:8080
```

La base MySQL est exposée localement sur :

```text
localhost:3307
```

Mailpit est disponible sur :

```text
http://localhost:8025
```

---

# En local

## 1. Préparer MySQL

Créer une base :

```sql
CREATE DATABASE onboardflow_db;
```

Puis configurer les paramètres :

```text
Host: localhost
Port: 3306
Database: onboardflow_db
Username: root
Password: votre_mot_de_passe
```

## 2. Configurer les variables

Exemple :

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/onboardflow_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="votre_mot_de_passe"
export JWT_SECRET_BASE64="VOTRE_SECRET_BASE64"
```

## 3. Lancer l'application

Linux / macOS :

```bash
./gradlew bootRun
```

Windows :

```powershell
.\gradlew.bat bootRun
```

L'application écoute sur :

```text
http://localhost:8081
```

---

# Configuration

Le fichier principal est :

```text
src/main/resources/application.properties
```

Configuration par défaut :

```properties
spring.application.name=onboardflow
server.port=8081

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/onboardflow_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update

jwt.secret=${JWT_SECRET_BASE64}

app.email.from=no-reply@onboardflow.com
app.email.verification-base-url=${APP_VERIFICATION_BASE_URL:http://localhost:8081/auth/verify-email}
```

---

# Variables d'environnement

| Variable | Description | Exemple |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC MySQL | `jdbc:mysql://localhost:3306/onboardflow_db...` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe MySQL | `password` |
| `JWT_SECRET_BASE64` | Secret JWT encodé en Base64 | `...` |
| `MAILTRAP_USERNAME` | Identifiant SMTP Mailtrap | `...` |
| `MAILTRAP_PASSWORD` | Mot de passe SMTP Mailtrap | `...` |
| `APP_VERIFICATION_BASE_URL` | URL de vérification e-mail | `http://localhost:8081/auth/verify-email` |

En environnement Docker, la configuration SMTP est remplacée par Mailpit :

```text
Host: mailpit
Port: 1025
Auth: désactivée
TLS: désactivé
```

---

# Lancement

## Développement

```bash
./gradlew bootRun
```

## Compilation

```bash
./gradlew build
```

## Génération du JAR

```bash
./gradlew bootJar
```

Le fichier JAR est généré dans :

```text
build/libs/
```

## Démarrer le JAR

```bash
java -jar build/libs/<fichier>.jar
```

---

# Base de données

Hibernate est configuré avec :

```properties
spring.jpa.hibernate.ddl-auto=update
```

Les tables sont donc créées / mises à jour automatiquement par Hibernate.

## Entités principales

### `users`

Stocke les informations des utilisateurs :

- UUID
- e-mail
- mot de passe hashé
- nom complet
- image de profil
- statut
- vérification e-mail
- rôle
- dates de création et de modification

### `refresh_tokens`

Stocke les refresh tokens sous forme de hash.

### `email_verification_tokens`

Stocke les tokens utilisés pour vérifier les adresses e-mail.

---

# Authentification JWT

Lors d'une connexion réussie, l'API retourne :

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

L'access token doit être envoyé dans l'en-tête :

```http
Authorization: Bearer <accessToken>
```

### Durées

| Token | Durée |
|---|---:|
| Access token | 1 heure |
| Refresh token | 30 jours |

Le refresh token est également enregistré côté serveur sous forme de hash SHA-256.

Lors d'un refresh :

```text
Refresh token valide
       │
       ▼
Vérification JWT
       │
       ▼
Recherche du hash en base
       │
       ▼
Ancien refresh token supprimé
       │
       ▼
Nouveaux access + refresh tokens
```

Cette rotation limite la réutilisation d'un ancien refresh token.

---

# API

Base URL en développement local :

```text
http://localhost:8081
```

Avec Docker :

```text
http://localhost:8080
```

---

## 1. Inscription

### Endpoint

```http
POST /auth/register
```

### Body

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "fullName": "John Doe"
}
```

### Réponse

```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "PENDING_VERIFICATION",
  "profileImageUrl": null
}
```

Un e-mail de vérification est ensuite envoyé.

---

## 2. Vérification e-mail

### Endpoint

```http
GET /auth/verify-email?token=<TOKEN>
```

### Réponse

```text
Successfully verified email ! You now have full access
```

Après vérification :

```text
isEmailVerified = true
status = ACTIVE
```

Le token de vérification est ensuite supprimé.

Les tokens de vérification expirent après **3 jours**.

---

## 3. Renvoyer l'e-mail de vérification

### Endpoint

```http
POST /auth/resend-verification-email
```

Le endpoint récupère l'utilisateur connecté, supprime son ancien token et génère un nouveau token valable 3 jours.

---

## 4. Connexion

### Endpoint

```http
POST /auth/login
```

### Body

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

### Réponse

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### Limitation

Le endpoint de login est limité à **5 requêtes par minute et par adresse IP**.

En cas de dépassement :

```http
429 Too Many Requests
```

---

## 5. Profil courant

### Endpoint

```http
GET /auth/me
```

### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

### Réponse

```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "profileImageUrl": null
}
```

---

## 6. Modification du profil

### Endpoint

```http
PUT /auth/me
```

### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

### Body exemple

```json
{
  "fullName": "John Updated",
  "profileImageUrl": "https://example.com/profile.jpg"
}
```

Pour changer le mot de passe :

```json
{
  "password": "NewPassword123!"
}
```

Les champs disponibles sont :

```text
password
fullName
status
profileImageUrl
```

---

## 7. Rafraîchir les tokens

### Endpoint

```http
POST /auth/refresh
```

### Body

```json
{
  "refreshToken": "eyJ..."
}
```

### Réponse

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

L'ancien refresh token est invalidé après utilisation.

---

## 8. Déconnexion

### Endpoint

```http
POST /auth/logout
```

### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

La déconnexion supprime les refresh tokens enregistrés pour l'utilisateur.

---

## 9. Administration des utilisateurs

### Endpoint

```http
GET /admin/users
```

### Paramètres disponibles

| Paramètre | Description |
|---|---|
| `role` | `ADMIN` ou `USER` |
| `isEmailVerified` | `true` / `false` |
| `search` | Recherche dans le nom ou l'e-mail |
| `page` | Numéro de page |
| `size` | Taille de page |
| `sort` | Champ et direction de tri |

### Exemple

```http
GET /admin/users?page=0&size=10&sort=createdAt,desc
```

Avec recherche :

```http
GET /admin/users?search=john&page=0&size=10
```

Avec filtre :

```http
GET /admin/users?role=USER&isEmailVerified=true
```

> Le code actuel authentifie les requêtes `/admin/users`, mais le contrôleur ne contient pas de règle explicite `hasRole("ADMIN")`. Il est donc recommandé d'ajouter une autorisation basée sur le rôle avant une mise en production.

---

# Swagger / OpenAPI

La documentation OpenAPI est générée automatiquement avec SpringDoc.

Swagger UI :

```text
http://localhost:8081/swagger-ui/index.html
```

Avec Docker :

```text
http://localhost:8080/swagger-ui/index.html
```

Documentation OpenAPI JSON :

```text
http://localhost:8081/v3/api-docs
```

Dans Swagger, utiliser :

```text
Authorize
```

puis :

```text
Bearer <ACCESS_TOKEN>
```

---

# E-mails de développement

En environnement Docker, le projet utilise **Mailpit**.

Interface web :

```text
http://localhost:8025
```

Serveur SMTP :

```text
mailpit:1025
```

Lorsqu'un utilisateur s'inscrit, l'e-mail de vérification est intercepté par Mailpit.

Cela permet de tester le processus de vérification sans envoyer de vrais e-mails.

---

# Rate limiting

Le `RateLimitFilter` protège spécifiquement :

```http
POST /auth/login
```

La limite actuelle est :

```text
5 requêtes / minute / IP
```

Après consommation du quota, l'API retourne :

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many login request. Please try again in 1 minute"
}
```

Le mécanisme utilise **Bucket4j** et une map concurrente en mémoire.

---

# Tests

Le projet contient notamment :

```text
OnboardflowApplicationTests.kt
AdminEndpointSecurityTest.kt
AuthFlowIntegrationtest.kt
UserProfileSecurityTest.kt
```

Pour lancer tous les tests :

```bash
./gradlew test
```

Pour construire le projet avec les tests :

```bash
./gradlew build
```

Pour obtenir un rapport de tests, consulter :

```text
build/reports/tests/test/index.html
```

---

# Docker

Le `docker-compose.yml` fournit trois services :

### API

```text
onboardflow-api
```

Port :

```text
8080 -> 8081
```

### MySQL

```text
onboardflow-db
```

Port :

```text
3307 -> 3306
```

### Mailpit

```text
onboardflow-mailpit
```

Ports :

```text
8025 -> 8025
1025 -> 1025
```

### Démarrage

```bash
docker compose up --build
```

### Arrêt

```bash
docker compose down
```

### Arrêt avec suppression des données MySQL

```bash
docker compose down -v
```

> La commande `down -v` supprime le volume `mysql_data` et donc les données persistées de MySQL.

---

# Compte administrateur initial

Au démarrage, `DatabaseSeeder` crée automatiquement un compte administrateur s'il n'existe pas déjà :

```text
Email    : admin@onboardflow.com
Password : AdminPass123!
Role     : ADMIN
Status   : ACTIVE
Verified : true
```

### ⚠️ Important

Ces identifiants sont des identifiants de développement présents directement dans le code du seeder.

**Ils doivent impérativement être modifiés ou supprimés avant une mise en production.**

---

# Modèle de données

Relation simplifiée :

```text
                 ┌─────────────────────┐
                 │        User         │
                 ├─────────────────────┤
                 │ id : UUID           │
                 │ email               │
                 │ hashedPassword      │
                 │ fullName            │
                 │ profileImageUrl     │
                 │ status              │
                 │ isEmailVerified     │
                 │ role                │
                 │ createdAt           │
                 │ updatedAt           │
                 └─────────┬───────────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
             ▼                           ▼
┌────────────────────────┐   ┌─────────────────────────┐
│    RefreshToken        │   │ EmailVerificationToken │
├────────────────────────┤   ├─────────────────────────┤
│ id                     │   │ id                      │
│ hashedToken            │   │ hashedToken             │
│ expiresAt              │   │ expiresAt                │
│ createdAt              │   │ createdAt                │
│ user_id                │   │ user_id                  │
└────────────────────────┘   └─────────────────────────┘
```

### Statuts utilisateur

```text
PENDING_VERIFICATION
        │
        │ vérification e-mail
        ▼
      ACTIVE
        │
        │ désactivation
        ▼
   DEACTIVATED
```

### Rôles

```text
USER
ADMIN
```

---

# Sécurité

Le projet implémente plusieurs mécanismes de sécurité.

## Hashage des mots de passe

Les mots de passe ne sont jamais enregistrés en clair.

Ils sont hashés avec :

```text
BCryptPasswordEncoder
```

## JWT

Les JWT sont signés avec :

```text
HS256
```

Le secret est fourni via :

```text
JWT_SECRET_BASE64
```

Il ne doit jamais être commit dans Git.

## Refresh tokens

Les refresh tokens bruts ne sont pas enregistrés en base.

Le backend stocke leur hash SHA-256.

## API stateless

Spring Security utilise :

```text
SessionCreationPolicy.STATELESS
```

Il n'y a donc pas de session HTTP classique.

## Validation

Les données entrantes utilisent Jakarta Validation :

```text
@NotBlank
@Email
@Size
@Pattern
```

---

# Points d'attention

Cette section décrit des éléments observés directement dans le code actuel.

### 1. Autorisation ADMIN

`/admin/users` nécessite une authentification, mais aucune vérification explicite du rôle `ADMIN` n'est présente dans `AdminUserController` ou `SecurityConfig`.

Pour une vraie séparation des privilèges, ajouter par exemple une règle du type :

```kotlin
.requestMatchers("/admin/**").hasRole("ADMIN")
```

ou utiliser une annotation de méthode adaptée.

### 2. Compte administrateur codé en dur

Le compte :

```text
admin@onboardflow.com
AdminPass123!
```

est créé automatiquement par `DatabaseSeeder`.

Il faut utiliser des secrets/configurations d'environnement en production.

### 3. `ddl-auto=update`

La configuration :

```properties
spring.jpa.hibernate.ddl-auto=update
```

est pratique en développement mais n'est généralement pas suffisante pour une stratégie de migration de base de données en production.

Une solution comme Flyway ou Liquibase peut être introduite.

### 4. Rate limiting en mémoire

Le rate limiting utilise une `ConcurrentHashMap` locale.

En cas de déploiement avec plusieurs instances de l'API, chaque instance aura son propre compteur.

Pour un environnement distribué, utiliser un stockage partagé ou un reverse proxy/API gateway adapté.

### 5. Secrets

Ne jamais mettre les valeurs suivantes directement dans Git :

```text
JWT_SECRET_BASE64
SPRING_DATASOURCE_PASSWORD
MAILTRAP_PASSWORD
```

Utiliser des variables d'environnement ou un gestionnaire de secrets.

### 6. URL de vérification

L'URL par défaut est :

```text
http://localhost:8081/auth/verify-email
```

Elle doit être remplacée par l'URL réellement accessible au client en production.

---

# Dépannage

## L'application ne démarre pas à cause de MySQL

Vérifier :

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Avec Docker, vérifier :

```bash
docker compose ps
```

Puis :

```bash
docker compose logs db
```

---

## L'API retourne une erreur JWT

Vérifier que :

```text
JWT_SECRET_BASE64
```

est défini et qu'il s'agit bien d'une chaîne Base64 correspondant à une clé suffisamment longue pour HS256.

Exemple de génération :

```bash
openssl rand -base64 32
```

---

## Aucun e-mail n'apparaît

Avec Docker :

1. Vérifier que Mailpit tourne :

```bash
docker compose ps
```

2. Ouvrir :

```text
http://localhost:8025
```

3. Vérifier la configuration SMTP.

---

## Erreur `401 Unauthorized`

Vérifier que le header est bien envoyé :

```http
Authorization: Bearer <ACCESS_TOKEN>
```

et que l'access token n'est pas expiré.

---

## Erreur `429 Too Many Requests`

Le login est limité à 5 tentatives par minute et par IP.

Attendre le renouvellement du quota avant de réessayer.

---

# Améliorations recommandées

Pour faire évoluer le projet vers un environnement de production, les améliorations suivantes sont recommandées :

- Ajouter une vraie autorisation RBAC pour `/admin/**`.
- Déplacer les identifiants administrateur dans des variables d'environnement.
- Remplacer `ddl-auto=update` par Flyway ou Liquibase.
- Ajouter une configuration CORS explicite selon les clients autorisés.
- Ajouter des logs structurés et une stratégie de monitoring.
- Ajouter des métriques métier via Actuator.
- Utiliser un rate limiter distribué si plusieurs instances sont déployées.
- Ajouter une rotation/gestion avancée des secrets JWT.
- Prévoir une révocation globale des tokens en cas de compromission.
- Ajouter des tests couvrant les rôles `ADMIN` et `USER`.
- Ajouter des tests d'expiration des tokens.
- Ajouter des tests de vérification et d'expiration des e-mails.
- Ajouter une stratégie de migration de schéma.
- Configurer HTTPS/TLS devant l'API en production.
- Éviter d'exposer des détails internes dans les messages d'erreur en production.
- Ajouter une configuration séparée `application-dev.properties` / `application-prod.properties`.

---

# Commandes utiles

```bash
# Compiler
./gradlew build

# Tester
./gradlew test

# Lancer localement
./gradlew bootRun

# Nettoyer
./gradlew clean

# Construire le JAR
./gradlew bootJar

# Docker
docker compose up --build

# Voir les logs
docker compose logs -f app

# Arrêter les conteneurs
docker compose down

# Arrêter et supprimer les volumes
docker compose down -v
```

---

# Flux d'utilisation recommandé

```text
                    ┌───────────────┐
                    │ POST /register│
                    └───────┬───────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │ PENDING_VERIFICATION │
                 └──────────┬───────────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │ Email de vérification│
                 └──────────┬───────────┘
                            │
                            ▼
                 ┌──────────────────────┐
                 │ GET /verify-email    │
                 └──────────┬───────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │    ACTIVE     │
                    └───────┬───────┘
                            │
                            ▼
                     ┌────────────┐
                     │ POST /login│
                     └─────┬──────┘
                           │
                    ┌──────┴──────┐
                    ▼             ▼
              Access Token   Refresh Token
                    │             │
                    ▼             ▼
             API protégée     /auth/refresh
                    │
                    ▼
              /auth/me
              /auth/me PUT
              /auth/logout
                    │
                    ▼
              /admin/users
              (authentifié)
```

---

# Contribution

1. Créer une branche :

```bash
git checkout -b feature/ma-fonctionnalite
```

2. Effectuer les modifications.

3. Lancer les tests :

```bash
./gradlew test
```

4. Vérifier le build :

```bash
./gradlew build
```

5. Commit :

```bash
git add .
git commit -m "feat: description de la fonctionnalité"
```

6. Push :

```bash
git push origin feature/ma-fonctionnalite
```

---

# Licence

Aucune licence open source n'est actuellement déclarée dans le projet.

Si le projet doit être distribué publiquement, il est recommandé d'ajouter un fichier `LICENSE` avec la licence choisie.

---

## Auteur / Projet

**OnboardFlow Backend**

Backend d'onboarding et de gestion des utilisateurs basé sur Kotlin + Spring Boot.

