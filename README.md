# OnboardFlow Backend

REST backend for **OnboardFlow**, an application for user management and onboarding workflows.

The project is built with **Kotlin** and **Spring Boot**, uses **MySQL** for persistence, **JWT** for authentication, **Spring Mail/Mailpit** for email verification, and **OpenAPI/Swagger** for API documentation.

> API Version: `1.0.0`  
> Project Version: `0.0.1-SNAPSHOT`

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Installation](#installation)
  - [With Docker](#with-docker-recommended)
  - [Locally](#locally)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Database](#database)
- [JWT Authentication](#jwt-authentication)
- [API](#api)
- [Swagger / OpenAPI](#swagger--openapi)
- [Development Emails](#development-emails)
- [Rate Limiting](#rate-limiting)
- [Tests](#tests)
- [Docker](#docker)
- [Initial Admin Account](#initial-admin-account)
- [Data Model](#data-model)
- [Security](#security)
- [Important Notes](#important-notes)
- [Troubleshooting](#troubleshooting)
- [Recommended Improvements](#recommended-improvements)
- [License](#license)

---

## Overview

OnboardFlow Backend provides a REST API for managing the complete user onboarding lifecycle:

1. account creation;
2. email verification;
3. account activation;
4. login with access and refresh tokens;
5. profile retrieval and updates;
6. secure token refresh;
7. logout;
8. paginated user management for administrators.

The backend is designed as a **stateless API**. No traditional HTTP session is used; user identity is carried through a JWT access token.

---

## Features

### Account Management

- User registration with request validation.
- Email address validation.
- Password policy:
  - minimum 8 characters;
  - at least one uppercase letter;
  - at least one lowercase letter;
  - at least one digit;
  - at least one special character.
- Password hashing with **BCrypt**.
- Email verification.
- Resending verification emails.
- Current-user profile retrieval.
- Profile updates.
- Password changes.
- Profile image URL management.
- User status management.

### Authentication

- JWT access tokens.
- JWT refresh tokens.
- Access token lifetime: **1 hour**.
- Refresh token lifetime: **30 days**.
- Refresh-token rotation.
- SHA-256 hashing of stored refresh tokens.
- Refresh-token deletion on logout.
- One refresh-token session stored per user.

### Security

- Spring Security.
- JWT authentication filter.
- Stateless API.
- Login rate limiting using Bucket4j.
- JSON responses for `401 Unauthorized`.
- Request validation with Jakarta Validation.
- BCrypt password hashing.

### Administration

- Paginated user listing.
- Filtering by role.
- Filtering by email verification status.
- Search by name or email.
- Pagination and sorting using Spring Data.

### Documentation

- OpenAPI 3.
- Swagger UI.
- Bearer JWT authentication scheme.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Java 17 | Runtime / toolchain |
| Spring Boot 4.1.0 | Backend framework |
| Spring Web | REST API |
| Spring Security | Security |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| MySQL 8 | Database |
| JJWT 0.12.6 | JWT creation and validation |
| BCrypt | Password hashing |
| Bucket4j 8.10.1 | Rate limiting |
| Spring Mail | Email sending |
| Mailpit | Email capture in development |
| SpringDoc OpenAPI 2.3.0 | Swagger / OpenAPI |
| Spring Actuator | Health checks |
| Gradle | Build and dependency management |
| Docker | Containerization |

---

## Architecture

The project follows a layered architecture inspired by Clean Architecture:

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

### Layer Responsibilities

**API**

Exposes HTTP endpoints, validates requests, and returns API responses.

**Application**

Contains business logic for registration, login, profile management, token handling, and email operations.

**Domain**

Contains business entities, enums, exceptions, and repository contracts.

**Infrastructure**

Contains technical implementations such as JWT security, password hashing, Swagger configuration, and database initialization.

---

## Project Structure

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

## Requirements

### Docker

- Docker
- Docker Compose

### Local Development

- Java 17
- MySQL 8
- Git
- Gradle Wrapper included with the project

You do not need to install Gradle globally because the project includes `gradlew` and `gradlew.bat`.

---

# Installation

## With Docker (Recommended)

The project provides a `docker-compose.yml` that starts three services:

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

### 1. Clone the project

```bash
git clone <REPOSITORY_URL>
cd OnboardingFlow-Backend
```

### 2. Generate a JWT secret

The project expects a Base64 value in `JWT_SECRET_BASE64`.

```bash
openssl rand -base64 32
```

Keep the generated value secure.

### 3. Set the variable

Linux / macOS:

```bash
export JWT_SECRET_BASE64="YOUR_BASE64_SECRET"
```

Windows PowerShell:

```powershell
$env:JWT_SECRET_BASE64="YOUR_BASE64_SECRET"
```

### 4. Start the services

```bash
docker compose up --build
```

The API will be available at:

```text
http://localhost:8080
```

MySQL is exposed locally on:

```text
localhost:3307
```

Mailpit is available at:

```text
http://localhost:8025
```

---

# Locally

## 1. Prepare MySQL

Create the database:

```sql
CREATE DATABASE onboardflow_db;
```

Then configure:

```text
Host: localhost
Port: 3306
Database: onboardflow_db
Username: root
Password: your_password
```

## 2. Configure environment variables

Example:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/onboardflow_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_password"
export JWT_SECRET_BASE64="YOUR_BASE64_SECRET"
```

## 3. Run the application

Linux / macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The local application listens on:

```text
http://localhost:8081
```

---

# Configuration

The main configuration file is:

```text
src/main/resources/application.properties
```

Default configuration:

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

# Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/onboardflow_db...` |
| `SPRING_DATASOURCE_USERNAME` | MySQL username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password | `password` |
| `JWT_SECRET_BASE64` | Base64-encoded JWT secret | `...` |
| `MAILTRAP_USERNAME` | Mailtrap SMTP username | `...` |
| `MAILTRAP_PASSWORD` | Mailtrap SMTP password | `...` |
| `APP_VERIFICATION_BASE_URL` | Email verification URL | `http://localhost:8081/auth/verify-email` |

In Docker, SMTP configuration is replaced by Mailpit:

```text
Host: mailpit
Port: 1025
Authentication: disabled
TLS: disabled
```

---

# Running the Application

## Development

```bash
./gradlew bootRun
```

## Build

```bash
./gradlew build
```

## Generate the JAR

```bash
./gradlew bootJar
```

The JAR is generated under:

```text
build/libs/
```

## Run the JAR

```bash
java -jar build/libs/<jar-file>.jar
```

---

# Database

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Therefore, database tables are automatically created or updated by Hibernate.

## Main Entities

### `users`

Stores user information:

- UUID
- email
- hashed password
- full name
- profile image
- status
- email verification status
- role
- creation date
- update date

### `refresh_tokens`

Stores refresh tokens as hashes.

### `email_verification_tokens`

Stores tokens used to verify email addresses.

---

# JWT Authentication

After a successful login, the API returns:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

The access token must be sent using:

```http
Authorization: Bearer <accessToken>
```

### Token Lifetimes

| Token | Lifetime |
|---|---:|
| Access token | 1 hour |
| Refresh token | 30 days |

The refresh token is also stored server-side as a SHA-256 hash.

During refresh:

```text
Valid refresh token
       │
       ▼
JWT validation
       │
       ▼
Hash lookup in database
       │
       ▼
Old refresh token deleted
       │
       ▼
New access + refresh tokens
```

This rotation reduces the risk of reusing an old refresh token.

---

# API

Base URL for local development:

```text
http://localhost:8081
```

With Docker:

```text
http://localhost:8080
```

---

## 1. Register

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

### Response

```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "PENDING_VERIFICATION",
  "profileImageUrl": null
}
```

A verification email is then sent.

---

## 2. Verify Email

### Endpoint

```http
GET /auth/verify-email?token=<TOKEN>
```

### Response

```text
Successfully verified email ! You now have full access
```

After verification:

```text
isEmailVerified = true
status = ACTIVE
```

The verification token is then deleted.

Verification tokens expire after **3 days**.

---

## 3. Resend Verification Email

### Endpoint

```http
POST /auth/resend-verification-email
```

The endpoint retrieves the authenticated user, removes the previous token, and generates a new token valid for 3 days.

---

## 4. Login

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

### Response

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### Rate Limit

The login endpoint is limited to **5 requests per minute per IP address**.

When the limit is exceeded:

```http
429 Too Many Requests
```

---

## 5. Current User Profile

### Endpoint

```http
GET /auth/me
```

### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

### Response

```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "profileImageUrl": null
}
```

---

## 6. Update Profile

### Endpoint

```http
PUT /auth/me
```

### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

### Example Body

```json
{
  "fullName": "John Updated",
  "profileImageUrl": "https://example.com/profile.jpg"
}
```

To change the password:

```json
{
  "password": "NewPassword123!"
}
```

Available fields include:

```text
password
fullName
status
profileImageUrl
```

---

## 7. Refresh Tokens

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

### Response

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

The previous refresh token is invalidated after use.

---

## 8. Logout

### Endpoint

```http
POST /auth/logout
```

### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Logout deletes the user's stored refresh tokens.

---

## 9. User Administration

### Endpoint

```http
GET /admin/users
```

### Available Parameters

| Parameter | Description |
|---|---|
| `role` | `ADMIN` or `USER` |
| `isEmailVerified` | `true` / `false` |
| `search` | Search by name or email |
| `page` | Page number |
| `size` | Page size |
| `sort` | Field and sort direction |

### Example

```http
GET /admin/users?page=0&size=10&sort=createdAt,desc
```

With search:

```http
GET /admin/users?search=john&page=0&size=10
```

With filters:

```http
GET /admin/users?role=USER&isEmailVerified=true
```
---

# Swagger / OpenAPI

OpenAPI documentation is generated automatically using SpringDoc.

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

With Docker:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

In Swagger UI, click:

```text
Authorize
```

Then enter:

```text
Bearer <ACCESS_TOKEN>
```

---

# Development Emails

In Docker, the project uses **Mailpit**.

Web interface:

```text
http://localhost:8025
```

SMTP server:

```text
mailpit:1025
```

When a user registers, the verification email is captured by Mailpit.

This allows the complete email verification workflow to be tested without sending real emails.

---

# Rate Limiting

The `RateLimitFilter` protects:

```http
POST /auth/login
```

Current limit:

```text
5 requests / minute / IP
```

After the quota is exceeded, the API returns:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many login request. Please try again in 1 minute"
}
```

The current implementation uses **Bucket4j** with an in-memory concurrent map.

---

# Tests

The project contains tests such as:

```text
OnboardflowApplicationTests.kt
AdminEndpointSecurityTest.kt
AuthFlowIntegrationtest.kt
UserProfileSecurityTest.kt
```

Run all tests:

```bash
./gradlew test
```

Build the project including tests:

```bash
./gradlew build
```

Test reports are available at:

```text
build/reports/tests/test/index.html
```

---

# Docker

The `docker-compose.yml` provides three services.

### API

```text
onboardflow-api
```

Port:

```text
8080 -> 8081
```

### MySQL

```text
onboardflow-db
```

Port:

```text
3307 -> 3306
```

### Mailpit

```text
onboardflow-mailpit
```

Ports:

```text
8025 -> 8025
1025 -> 1025
```

### Start

```bash
docker compose up --build
```

### Stop

```bash
docker compose down
```

### Stop and delete MySQL data

```bash
docker compose down -v
```

> `down -v` removes the `mysql_data` volume and therefore deletes persisted MySQL data.

---

# Initial Admin Account

At startup, `DatabaseSeeder` automatically creates an administrator account if it does not already exist:

```text
Email    : admin@onboardflow.com
Password : AdminPass123!
Role     : ADMIN
Status   : ACTIVE
Verified : true
```

### ⚠️ Important

These credentials are development credentials defined directly in the seeder code.

**They must be changed or removed before production deployment.**

---

# Data Model

Simplified relationship:

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

### User Statuses

```text
PENDING_VERIFICATION
        │
        │ email verification
        ▼
      ACTIVE
        │
        │ deactivation
        ▼
   DEACTIVATED
```

### Roles

```text
USER
ADMIN
```

---

# Security

The project implements several security mechanisms.

## Password Hashing

Passwords are never stored in plain text.

They are hashed using:

```text
BCryptPasswordEncoder
```

## JWT

JWTs are signed using:

```text
HS256
```

The secret is provided through:

```text
JWT_SECRET_BASE64
```

It must never be committed to Git.

## Refresh Tokens

Raw refresh tokens are not stored in the database.

The backend stores their SHA-256 hashes.

## Stateless API

Spring Security uses:

```text
SessionCreationPolicy.STATELESS
```

Therefore, there is no traditional HTTP session.

## Validation

Incoming requests use Jakarta Validation:

```text
@NotBlank
@Email
@Size
@Pattern
```

---

# Important Notes

These points were identified directly from the current codebase.

### 1. ADMIN Authorization

`/admin/users` requires authentication, but there is no explicit `ADMIN` role check in the current `AdminUserController` / security configuration.

For proper privilege separation, add a rule such as:

```kotlin
.requestMatchers("/admin/**").hasRole("ADMIN")
```

or use an appropriate method-level security annotation.

### 2. Hardcoded Admin Credentials

The following account:

```text
admin@onboardflow.com
AdminPass123!
```

is automatically created by `DatabaseSeeder`.

Production environments should use environment variables or a secure secret-management system.

### 3. `ddl-auto=update`

The current configuration:

```properties
spring.jpa.hibernate.ddl-auto=update
```

is convenient for development but is generally not sufficient as a production database migration strategy.

Consider using Flyway or Liquibase.

### 4. In-Memory Rate Limiting

Rate limiting currently uses a local `ConcurrentHashMap`.

If multiple API instances are deployed, each instance will have its own rate-limit counters.

For distributed deployments, use shared storage or an appropriate API gateway / distributed rate-limiting solution.

### 5. Secrets

Never commit the following values to Git:

```text
JWT_SECRET_BASE64
SPRING_DATASOURCE_PASSWORD
MAILTRAP_PASSWORD
```

Use environment variables or a secret manager.

### 6. Verification URL

The default verification URL is:

```text
http://localhost:8081/auth/verify-email
```

It must be replaced with the actual client-accessible URL in production.

---

# Troubleshooting

## The application cannot connect to MySQL

Check:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

With Docker:

```bash
docker compose ps
```

Then:

```bash
docker compose logs db
```

---

## JWT errors

Make sure:

```text
JWT_SECRET_BASE64
```

is defined and contains a sufficiently long Base64-encoded secret suitable for HS256.

Generate one with:

```bash
openssl rand -base64 32
```

---

## Verification emails are not appearing

With Docker:

1. Check that Mailpit is running:

```bash
docker compose ps
```

2. Open:

```text
http://localhost:8025
```

3. Verify the SMTP configuration.

---

## `401 Unauthorized`

Make sure the request contains:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

and that the access token has not expired.

---

## `429 Too Many Requests`

Login is limited to 5 attempts per minute per IP address.

Wait for the quota to reset before trying again.

---

# Useful Commands

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Run locally
./gradlew bootRun

# Clean
./gradlew clean

# Build JAR
./gradlew bootJar

# Docker
docker compose up --build

# View logs
docker compose logs -f app

# Stop containers
docker compose down

# Stop containers and remove volumes
docker compose down -v
```
---

# Contribution

1. Create a branch:

```bash
git checkout -b feature/my-feature
```

2. Make your changes.

3. Run the tests:

```bash
./gradlew test
```

4. Verify the build:

```bash
./gradlew build
```

5. Commit:

```bash
git add .
git commit -m "feat: describe the feature"
```

6. Push:

```bash
git push origin feature/my-feature
```
---

## Project

**OnboardFlow Backend**

An onboarding and user-management backend built with Kotlin and Spring Boot.
