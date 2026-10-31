# MaximizeCRM

MaximizeCRM is a full-stack Customer Relationship Management system built with modern enterprise technologies.

## 🚀 Technology Stack

### Backend
* **Java:** 25 LTS
* **Framework:** Spring Boot 3.5.x
* **Security & Auth:** Spring Security (Stateless JWT Authentication & BCrypt password hashing)
* **JWT Library:** JJWT 0.12.6 (HMAC-SHA256)
* **ORM / Database Access:** Spring Data JPA / Hibernate
* **Database:** MySQL 8.4 (with in-memory H2 for isolated unit test execution)
* **Validation:** Jakarta Validation / Hibernate Validator
* **Build Tool:** Apache Maven 3.9+

### Frontend
* **Framework:** Angular 21 (Standalone Component architecture)
* **UI Components:** Angular Material & CDK
* **State & Storage:** Angular Signals & browser `sessionStorage`
* **HTTP Client:** Angular `HttpClient` with functional `jwtInterceptor` & dev proxy
* **Unit Testing:** Vitest 4
* **Styling:** SCSS & Roboto typography

---

## 📁 Project Structure

```
MaximizeCRM/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/maximizecrm/
│   │   │   │   ├── MaximizeCrmApplication.java       # Main Spring Boot entry point
│   │   │   │   ├── config/
│   │   │   │   │   ├── DataInitializer.java          # Dev seed for initial admin user
│   │   │   │   │   ├── GlobalExceptionHandler.java   # Centralized error response handler
│   │   │   │   │   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter for Bearer token
│   │   │   │   │   └── SecurityConfig.java           # Stateless Security, CORS & filter chain
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java           # POST /api/auth/login, GET /api/auth/me
│   │   │   │   │   └── HealthController.java         # GET /api/health
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   ├── HealthResponse.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   └── LoginResponse.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Role.java                     # ADMIN, SALES_REP
│   │   │   │   │   └── User.java                     # User JPA Entity (BCrypt password hash)
│   │   │   │   ├── repository/
│   │   │   │   │   └── UserRepository.java           # JpaRepository for User
│   │   │   │   └── service/
│   │   │   │       ├── AuthService.java              # Login validation & response generator
│   │   │   │       └── JwtService.java              # JWT creation, claims & verification
│   │   │   └── resources/
│   │   │       └── application.properties            # MySQL, JWT & CORS configuration
│   │   └── test/
│   │       ├── java/com/maximizecrm/
│   │       │   ├── MaximizeCrmApplicationTests.java
│   │       │   ├── controller/AuthControllerTest.java
│   │       │   └── service/AuthServiceTest.java
│   │       └── resources/
│   │           └── application.properties            # H2 in-memory test configuration
│   ├── .env.example                                  # Template for DB and JWT secrets
│   └── pom.xml                                       # Maven config (Java 25, Spring Boot 3.5)
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── login/                            # Angular Material login form
│   │   │   │   └── dashboard/                        # Authenticated dashboard & API tester
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts                     # Protects routes via AuthService
│   │   │   ├── interceptors/
│   │   │   │   └── jwt.interceptor.ts                # Attaches Bearer token to API requests
│   │   │   ├── models/
│   │   │   │   └── auth.models.ts                    # User, LoginRequest, LoginResponse
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts                   # Login, logout, sessionStorage & signals
│   │   │   │   └── health.service.ts                 # Backend health client
│   │   │   ├── app.config.ts                         # Router, HttpClient with interceptor
│   │   │   ├── app.routes.ts                         # Route definitions
│   │   │   ├── app.ts                                # Root component with toolbar navigation
│   │   │   ├── app.html
│   │   │   └── app.scss
│   │   ├── environments/
│   │   │   ├── environment.ts
│   │   │   └── environment.development.ts
│   │   ├── index.html
│   │   ├── styles.scss
│   │   └── main.ts
│   ├── angular.json
│   ├── package.json
│   └── proxy.conf.json                               # Proxies /api to http://localhost:8080
├── .gitignore
└── README.md
```

---

## 🔑 Development Admin Account

On initial startup, `DataInitializer` automatically seeds an administrative user in MySQL if one does not already exist:

| Field | Value |
|---|---|
| **Email** | `admin@maximizecrm.local` |
| **Password** | `Admin@123` |
| **Role** | `ADMIN` |
| **Password Hash** | BCrypt |

> [!WARNING]
> This seed account is strictly for local development and testing. Do not use default credentials in staging or production.

---

## ⚙️ Prerequisites

Ensure the following tools are installed:
* **Java 25 LTS** (`java -version`)
* **Maven 3.9+** (`mvn -version`)
* **Node.js 24+** (`node -v`)
* **npm 11+** (`npm -v`)
* **Angular CLI 21+** (`ng version`)
* **MySQL 8.4+**

---

## 🛠️ Configuration & Setup

### 1. Database Setup (MySQL 8.4)

Create the MySQL database:

```sql
CREATE DATABASE IF NOT EXISTS maximizecrm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Environment Variables

Database and JWT properties can be passed via environment variables (see `backend/.env.example`):

| Variable | Description | Default Value |
|---|---|---|
| `DB_HOST` | MySQL database host | `localhost` |
| `DB_PORT` | MySQL database port | `3306` |
| `DB_NAME` | Database schema name | `maximizecrm` |
| `DB_USERNAME` | MySQL user | `root` |
| `DB_PASSWORD` | MySQL password | *(empty)* |
| `JWT_SECRET` | 256-bit secret key for HMAC-SHA256 | `404E63526655...` *(safe dev key)* |
| `JWT_EXPIRATION_MS`| Token lifetime in milliseconds | `86400000` *(24 hours)* |

#### Set Variables (PowerShell):
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="maximizecrm"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
```

---

## 🏃 Running the Application

### 1. Run Backend

Navigate into `backend/`:

```powershell
cd backend
mvn clean spring-boot:run
```

The backend server starts at **`http://localhost:8080`**.

#### Run Backend Tests:
```powershell
cd backend
mvn test
```

---

### 2. Run Frontend

Open a second terminal and navigate into `frontend/`:

```powershell
cd frontend
npm install
npm start
```

The Angular dev server starts at **`http://localhost:4200`**.

#### Run Frontend Tests:
```powershell
cd frontend
npm test -- --watch=false
```

---

## 🔒 Authentication API Endpoints

### 1. POST `/api/auth/login` (Public)
Authenticates a user and issues a JWT token.

* **Request Body:**
  ```json
  {
    "email": "admin@maximizecrm.local",
    "password": "Admin@123"
  }
  ```
* **Success Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 1,
    "fullName": "System Administrator",
    "email": "admin@maximizecrm.local",
    "role": "ADMIN"
  }
  ```
* **Failure Response (401 Unauthorized):**
  ```json
  {
    "status": 401,
    "message": "Invalid email or password",
    "timestamp": "2026-10-31T21:00:00"
  }
  ```

### 2. GET `/api/auth/me` (Protected)
Requires `Authorization: Bearer <token>`. Returns current user profile.

### 3. GET `/api/health` (Public)
Health check returning `{ "status": "UP", "application": "MaximizeCRM" }`.
