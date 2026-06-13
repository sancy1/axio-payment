# 🎓 Axio Quan Payment Service

> A complete course enrollment and payment management system for educational platforms. Seamlessly handle course purchases, student enrollment, progress tracking, and transaction management.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?style=flat-square)
![Paystack](https://img.shields.io/badge/Paystack-Payments-purple?style=flat-square)
![Next.js](https://img.shields.io/badge/Next.js-14+-black?style=flat-square)
![REST API](https://img.shields.io/badge/REST-API-yellow?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Pricing & Currency Model](#-pricing--currency-model)
- [Features](#-features)
- [Technology Stack](#️-technology-stack)
- [Architecture](#️-architecture)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [API Endpoints](#-api-endpoints)
- [Authentication & Token Exchange](#-authentication--token-exchange)
- [Payment Flow](#-payment-flow)
- [Database Schema](#️-database-schema)
- [Configuration](#️-configuration)
- [Deployment](#-deployment)
- [Integration with Next.js](#-integration-with-nextjs)
- [Security](#-security)
- [Contributing](#-contributing)
- [Environment Variables Reference](#-environment-variables-reference)
- [Documentation Files](#-documentation-files)
- [Troubleshooting](#-troubleshooting)
- [Support](#-support)
- [License](#-license)
- [Acknowledgments](#-acknowledgments)
- [Project Statistics](#-project-statistics)
- [Changelog](#️-changelog)

---

## 🎯 Overview

**Axio Quan Payment Service** is a robust backend API for managing course purchases, student enrollments, and payments. It integrates with **Paystack** for secure payment processing and automatically enrolls students after successful payment.

The Next.js frontend owns the course catalog and passes price data at checkout. This service focuses on payments, enrollments, transactions, and notifications.

### Core Purpose

- ✅ Allow students to purchase courses
- ✅ Process payments securely via Paystack
- ✅ Automatically enroll students after payment
- ✅ Track learning progress and course completion
- ✅ Manage paid vs. free course access
- ✅ Audit all transactions
- ✅ Send in-app notifications to students

### Perfect For

- 📚 Online learning platforms
- 🏫 Educational institutions
- 💼 Corporate training programs
- 🎓 Skill development platforms
- 📖 Course marketplaces

### API Base Path

All endpoints are served under:

```
http://localhost:8080/api
```

> **Note:** Most controllers map to `/v1/...` (full path: `/api/v1/...`). The enrollment controller maps to `/api/v1/enrollments`, producing `/api/api/v1/enrollments/...` — see [API Endpoints](#-api-endpoints) for exact paths.

---

## 💰 Pricing & Currency Model

Course prices use the **kobo** model — Paystack's smallest NGN unit. Fields are named `*_cents` for API compatibility.

| Concept | Detail |
|---------|--------|
| **Storage format** | Integer in kobo (e.g. `500000` = ₦5,000.00) |
| **Display in Naira** | Divide by 100: `amountCents / 100` |
| **Who sends the price** | Next.js passes `amountCents` in the initialize-payment request |
| **Original amount** | Stored as `originalAmountCents` with `originalCurrency = "NGN"` |
| **Paystack charge** | Sent in the smallest unit of the charge currency (kobo for NGN) |

### Example

```
Course price:    ₦10,000.00
Stored as:       amountCents = 1_000_000   (10,000 × 100 kobo)
Displayed as:    1_000_000 / 100 = ₦10,000.00
```

### Multi-Currency Support

The service can convert from NGN to a user's currency before charging Paystack (`CurrencyConverter` + `UserCurrencyResolver`). Currently all users default to **NGN**. The original NGN amount is always preserved on payment and enrollment records.

> **Note:** A prior plan to remove kobo-based storage was **reverted**. The application uses the standard kobo model above.

### Out of Scope

- **Free course enrollment** — handled by the frontend/enrollment flow. Requests with `amountCents = 0` are rejected by the payment service.
- **Course content management** — owned by the Next.js application.

---

## ✨ Features

### 💳 Payment Processing

- Secure payment integration with **Paystack**
- Payment status tracking (`PENDING`, `SUCCESS`, `FAILED`)
- Automatic enrollment on successful payment
- Webhook support for real-time payment notifications
- Transaction audit logging
- Idempotent pending-payment reuse (same user + course)

### 🎓 Enrollment Management

- Automatic enrollment creation on payment success (application-level)
- Lifetime access for paid courses (`canUnenroll = false`)
- Optional unenrollment for free courses
- Paid course protection (cannot unenroll)
- Access control and verification endpoints

### 📊 Progress Tracking

- Track lessons completed
- Quiz and assignment scoring
- Overall grade calculation
- Time spent tracking
- Course completion status
- Progress visualization ready

### 💰 Financial Management

- Support for multiple currencies (NGN, USD — conversion layer present)
- Amount stored in kobo for precision
- User transaction history and analytics
- Payment date tracking
- Original NGN amount preserved alongside charge currency

### 🔔 Notifications

- In-app notification system
- Pagination support for notification feeds
- Mark notifications as read
- Unread count tracking
- Email notifications (async, non-blocking)

### 🔐 Security

- JWT authentication for protected endpoints
- Service-to-service token endpoint (`X-Service-Secret`)
- Paystack webhook signature verification (HMAC-SHA512)
- Rate limiting on payment and webhook endpoints
- HTTPS/TLS encryption in production
- Non-root Docker container execution
- Secure environment variable management

### 📱 Multi-Platform

- RESTful API design
- Works with any frontend (Web, Mobile, Desktop)
- Next.js integration guide included
- CORS enabled for frontend apps
- Swagger UI via SpringDoc OpenAPI

---

## 🛠️ Technology Stack

### Backend

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 (LTS) | Programming language |
| Spring Boot | 3.2.5 | Web framework |
| Spring Data JPA | 3.2 | Database ORM |
| Spring Security | 6.2 | Authentication & authorization |
| Spring Mail | 3.2 | Email notifications |
| Lombok | 1.18.30 | Code generation |
| Jackson | 2.17 | JSON processing |
| Guava | 33.0 | Rate limiting |
| SpringDoc OpenAPI | 2.5.0 | API documentation / Swagger |
| jjwt | 0.12.3 | JWT token handling |

### Database

| Technology | Version | Purpose |
|-----------|---------|---------|
| PostgreSQL | 15+ | Primary database (production) |
| Neon Cloud | Latest | Cloud database hosting |
| Flyway | 10.10 | Database migrations |
| H2 Database | 2.2 | Local dev / testing |

### External Services

| Service | Purpose |
|---------|---------|
| Paystack | Payment processing |
| Gmail SMTP | Email sending |
| Render Cloud | API hosting |
| Neon PostgreSQL | Database hosting |

### Development Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Maven | 3.9+ | Build automation |
| Docker | Latest | Containerization |
| Git | Latest | Version control |
| Postman | Latest | API testing |

### Frontend (Example)

| Technology | Version | Purpose |
|-----------|---------|---------|
| Next.js | 14+ | React framework |
| React | 18+ | UI library |
| TypeScript | 5+ | Type safety |
| Axios/Fetch | Latest | HTTP client |

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Next.js)                      │
│                                                               │
│  - Course browse & enrollment                               │
│  - Payment checkout                                          │
│  - Progress dashboard                                        │
│  - User notifications                                        │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP REST API  (/api/v1/...)
                     ↓
┌─────────────────────────────────────────────────────────────┐
│              Payment Service API (Java Spring Boot)          │
│                                                               │
│  • Auth Controller       (Token generation & exchange)       │
│  • Payment Controller    (Payment flow)                      │
│  • Enrollment Controller (Course enrollment & progress)      │
│  • Notification Controller (In-app alerts)                   │
│  • Transaction Controller (Audit logging)                    │
│  • Webhook Controller    (Paystack integration)              │
│  • Health Controller     (Liveness & DB check)               │
└────────────┬──────────────────────┬────────────────────────┘
             │                      │
             ↓                      ↓
    ┌────────────────┐    ┌──────────────────┐
    │  PostgreSQL    │    │  Paystack API    │
    │  (Neon Cloud)  │    │  (Payments)      │
    │                │    │                  │
    │ • Payments     │    │ • Charge cards   │
    │ • Enrollments  │    │ • Verify status  │
    │ • Transactions │    │ • Webhooks       │
    │ • Users        │    │                  │
    │ • Courses      │    │                  │
    │ • Notifications│    │                  │
    └────────────────┘    └──────────────────┘

    ┌────────────────┐
    │  Gmail SMTP    │
    │  (Email)       │
    │                │
    │ • Async email  │
    │ • Notifications│
    │ • Receipts     │
    └────────────────┘
```

### Data Flow: Complete Payment Journey

```
1. USER INITIATES PAYMENT
   │
   ├─→ POST /api/v1/payments/initialize
   │   Body: { userId, courseId, email, amountCents }
   │   Response: { reference, authorizationUrl, accessCode }
   │
2. PAYMENT RECORD CREATED
   │
   ├─→ Database: INSERT INTO payments (...)
   │   Status: PENDING
   │   originalAmountCents: price in kobo (NGN)
   │
3. USER REDIRECTED TO PAYSTACK
   │
   ├─→ window.location.href = authorizationUrl
   │
4. USER COMPLETES PAYMENT ON PAYSTACK
   │
5. PAYSTACK SENDS WEBHOOK
   │
   ├─→ POST /api/v1/webhooks/paystack
   │   Header: x-paystack-signature: {HMAC-SHA512}
   │   Payload: event=charge.success, data={...}
   │
6. VERIFY WEBHOOK SIGNATURE
   │
   ├─→ Calculate HMAC(payload, secret_key)
   │   Compare with x-paystack-signature
   │
7. UPDATE PAYMENT STATUS
   │
   ├─→ Database: UPDATE payments SET status='SUCCESS' WHERE reference=?
   │
8. CREATE ENROLLMENT (Application Service)
   │
   ├─→ PaymentServiceImpl.createEnrollmentIfNotExists()
   │   INSERT INTO enrollments (...)
   │   Values: userId, courseId, paymentId, canUnenroll=false
   │
9. CREATE AUDIT TRANSACTION
   │
   ├─→ TransactionService.createTransaction(...)
   │
10. SEND CONFIRMATION EMAIL (ASYNC)
   │
   ├─→ EmailService.sendHtmlAsync(...)
   │   Subject: "Payment Confirmed ✅ - {courseName}"
   │
11. CREATE IN-APP NOTIFICATION
   │
   ├─→ NotificationService.createNotification(...)
   │   Type: PAYMENT_SUCCESS
   │
12. FRONTEND VERIFIES PAYMENT
   │
   ├─→ GET /api/v1/payments/verify/{reference}
   │   Response: status='SUCCESS', enrollmentId={uuid}
   │
13. USER REDIRECTED TO COURSE
   │
   └─→ /course/{courseId}
      Access granted ✅
```

---

## 🚀 Quick Start

### Prerequisites

```bash
# Required
- Java 17 LTS
- Maven 3.9+
- PostgreSQL 13+  (or use dev profile with H2)

# Optional
- Docker & Docker Compose
- Postman (API testing)
- Git
- Node.js 16+ (for Next.js frontend)
```

### 1. Clone Repository

```bash
git clone https://github.com/sancy1/axio-payment.git
cd payment-service
```

### 2. Configure Environment Variables

Create a `.env` file in the project root (auto-loaded at startup via `dotenv-java`):

```env
# Profile
SPRING_PROFILES_ACTIVE=dev

# Database (production)
DATABASE_URL=jdbc:postgresql://localhost:5432/axio_prod
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Paystack
PAYSTACK_PUBLIC_KEY=pk_test_your_key
PAYSTACK_SECRET_KEY=sk_test_your_key
PAYSTACK_CALLBACK_URL=http://localhost:3000/payment-success
PAYSTACK_WEBHOOK_URL=http://localhost:8080/api/v1/webhooks/paystack
WEBHOOK_SECRET=your_webhook_secret

# JWT & Service Auth
JWT_SECRET=your_very_secret_jwt_key_at_least_32_chars_long
SERVICE_SECRET=your_internal_service_secret

# Email (optional for local dev)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your_app_password

# Server
SERVER_PORT=8080
SERVER_CONTEXT_PATH=/api
FRONTEND_URL=http://localhost:3000
```

### 3. Create Database (Production / PostgreSQL)

```bash
psql -U postgres

CREATE DATABASE axio_prod;
CREATE USER payment_user WITH PASSWORD 'password123';
GRANT ALL PRIVILEGES ON DATABASE axio_prod TO payment_user;

\q
```

### 4. Build & Run

```bash
# Build
mvn clean install

# Run — dev profile (H2 in-memory, no PostgreSQL needed)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run — production profile (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Or using JAR
java -jar target/payment-service-1.0.0.jar --spring.profiles.active=prod
```

### 5. Verify Running

```bash
curl http://localhost:8080/api/health
```

**Response:**

```json
{
  "status": "UP",
  "database": "CONNECTED",
  "timestamp": "2026-06-13T12:00:00"
}
```

**Swagger UI:** `http://localhost:8080/api/swagger-ui.html`

### Using Docker

```bash
# Build and run full stack (app + PostgreSQL)
docker compose up --build

# Or build image only
docker build -t axio-payment:latest .

docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/axio_prod \
  -e PAYSTACK_SECRET_KEY=sk_test_xxx \
  -e JWT_SECRET=your_secret \
  axio-payment:latest
```

---

## 📁 Project Structure

```
payment-service/
│
├── src/main/java/com/axioquan/payment_service/
│   │
│   ├── PaymentServiceApplication.java       # Entry point, .env + DATABASE_URL parsing
│   │
│   ├── config/                              # Security, JWT, Paystack, Database, Web
│   │   ├── SecurityConfig.java
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── PaystackProperties.java
│   │   ├── DatabaseConfig.java
│   │   └── WebConfig.java
│   │
│   ├── domain/
│   │   ├── entities/                        # JPA entities
│   │   │   ├── Payment.java
│   │   │   ├── Enrollment.java
│   │   │   ├── Transaction.java
│   │   │   ├── Notification.java
│   │   │   ├── User.java
│   │   │   ├── Course.java
│   │   │   └── WebhookLog.java
│   │   └── repositories/
│   │
│   ├── errors/                              # Custom exceptions
│   │   ├── PaystackApiException.java
│   │   ├── PaymentAlreadyProcessedException.java
│   │   └── RateLimitExceededException.java
│   │
│   ├── infrastructure/
│   │   └── paystack/                        # Paystack client, DTOs, webhook verifier
│   │       ├── PaystackClient.java
│   │       ├── PaystackHttpClient.java
│   │       ├── PaystackWebhookVerifier.java
│   │       └── dto/
│   │
│   ├── middleware/                          # Filters
│   │   ├── JwtAuthenticationFilter.java
│   │   └── RateLimitFilter.java
│   │
│   ├── modules/                               # Feature modules
│   │   ├── auth/          → AuthController, UserRepository
│   │   ├── payments/      → PaymentController, PaymentServiceImpl
│   │   ├── enrollments/   → EnrollmentController, EnrollmentServiceImpl
│   │   ├── notifications/ → NotificationController, EmailServiceImpl
│   │   ├── transactions/  → TransactionController, TransactionServiceImpl
│   │   ├── webhooks/      → WebhookController
│   │   ├── courses/       → CourseRepository
│   │   └── health/        → HealthController
│   │
│   └── utils/
│       ├── ApiResponse.java
│       ├── CurrencyConverter.java
│       └── UserCurrencyResolver.java
│
├── src/main/resources/
│   ├── application.yml                      # Base configuration
│   ├── application-dev.yml                  # H2 dev profile
│   ├── application-dev-local.yml            # Local H2 variant
│   └── application-prod.yml                 # PostgreSQL production profile
│
├── src/test/
│   └── resources/application-test.yml
│
├── infrastructure/scripts/                  # Deployment scripts
├── Dockerfile                               # Multi-stage Java 17 build
├── docker-compose.yml                       # App + PostgreSQL stack
├── docker-compose.dev.yml
├── pom.xml
├── mvnw / mvnw.cmd
│
├── README.md                                # This file
├── API_ENDPOINTS_COMPREHENSIVE.md
├── API_QUICK_REFERENCE.md
├── NEXTJS_PAYMENT_INTEGRATION_GUIDE.md
├── TOKEN_EXCHANGE_PATTERN_SUMMARY.md
└── ENV_VARIABLES_MAPPING.md
```

---

## 📡 API Endpoints

> **Base URL:** `http://localhost:8080/api` (production: `https://<your-domain>/api`)

### Complete Endpoint Overview

| Module | Count | Base Path |
|--------|-------|-----------|
| **Authentication** | 5 | `/api/v1/auth/**` |
| **Payments** | 5 | `/api/v1/payments/**` |
| **Enrollments** | 21 | `/api/api/v1/enrollments/**` |
| **Notifications** | 4 | `/api/v1/notifications/**` |
| **Transactions** | 9 | `/api/v1/transactions/**` |
| **Webhooks** | 1 | `/api/v1/webhooks/paystack` |
| **Health** | 2 | `/api/health/**` |
| **TOTAL** | **47** | — |

### Response Format

All JSON responses use a consistent wrapper:

```json
{
  "success": true,
  "message": "Payment initialized successfully",
  "data": { },
  "timestamp": "2026-06-13T12:00:00"
}
```

---

### 🔑 Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/service-token` | Server-to-server JWT (`X-Service-Secret` header required) |
| `POST` | `/api/v1/auth/generate-token?userId={uuid}` | JWT for existing user (testing) |
| `POST` | `/api/v1/auth/generate-token-by-email?email={email}` | JWT by email (testing) |
| `POST` | `/api/v1/auth/validate-token?token={jwt}` | Validate a JWT |
| `POST` | `/api/v1/auth/generate-token-with-role?email={email}&role={role}` | JWT with custom role |

```http
POST /api/v1/auth/generate-token?userId={uuid}
POST /api/v1/auth/generate-token-by-email?email={email}
POST /api/v1/auth/validate-token?token={jwt}
POST /api/v1/auth/generate-token-with-role?email={email}&role={role}
POST /api/v1/auth/service-token?userId={uuid}&email={email}&name={name}
X-Service-Secret: <SERVICE_SECRET>
```

---

### 💳 Payments (JWT Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/payments/initialize` | Create payment & get Paystack checkout URL |
| `GET` | `/api/v1/payments/verify/{reference}` | Verify payment status |
| `GET` | `/api/v1/payments/reference/{reference}` | Get payment by reference |
| `GET` | `/api/v1/payments/user/{userId}/course/{courseId}/status` | Check if course purchased |
| `GET` | `/api/v1/payments/user/{userId}/courses` | Purchased courses (placeholder) |

```http
POST /api/v1/payments/initialize
GET  /api/v1/payments/verify/{reference}
GET  /api/v1/payments/reference/{reference}
GET  /api/v1/payments/user/{userId}/course/{courseId}/status
GET  /api/v1/payments/user/{userId}/courses
```

**Initialize request body:**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "courseId": "660e8400-e29b-41d4-a716-446655440001",
  "email": "student@example.com",
  "amountCents": 1000000,
  "courseName": "Introduction to Java"
}
```

---

### 🎓 Enrollments (JWT Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/api/v1/enrollments/user/{userId}/course/{courseId}` | Get enrollment |
| `GET` | `/api/api/v1/enrollments/user/{userId}/all` | All enrollments for user |
| `GET` | `/api/api/v1/enrollments/course/{courseId}/all` | All enrollments for course |
| `GET` | `/api/api/v1/enrollments/payment/{paymentId}` | Enrollment by payment |
| `GET` | `/api/api/v1/enrollments/check-access/{userId}/{courseId}` | Check course access |
| `GET` | `/api/api/v1/enrollments/check-paid-access/{userId}/{courseId}` | Check paid access |
| `PUT` | `/api/api/v1/enrollments/{enrollmentId}/progress` | Update learning progress |
| `GET` | `/api/api/v1/enrollments/{enrollmentId}/progress` | Get progress |
| `POST` | `/api/api/v1/enrollments/{enrollmentId}/mark-completed` | Mark course completed |
| `GET` | `/api/api/v1/enrollments/{enrollmentId}/is-completed` | Check completion status |
| `GET` | `/api/api/v1/enrollments/user/{userId}/with-progress` | Enrollments with progress |
| `GET` | `/api/api/v1/enrollments/{userId}/{courseId}/can-unenroll` | Can user unenroll? |
| `POST` | `/api/api/v1/enrollments/{userId}/{courseId}/unenroll` | Unenroll (free courses) |
| `GET` | `/api/api/v1/enrollments/{userId}/{courseId}/unenroll-block-reason` | Why unenroll blocked |
| `GET` | `/api/api/v1/enrollments/user/{userId}/paid-courses` | Paid course list |
| `GET` | `/api/api/v1/enrollments/user/{userId}/free-courses` | Free course list |
| `GET` | `/api/api/v1/enrollments/{userId}/{courseId}/is-paid` | Is enrollment paid? |
| `GET` | `/api/api/v1/enrollments/{userId}/{courseId}/has-lifetime-access` | Lifetime access check |
| `GET` | `/api/api/v1/enrollments/user/{userId}/paid-courses-count` | Count of paid courses |
| `GET` | `/api/api/v1/enrollments/user/{userId}/total-spent` | Total amount spent |

```http
GET  /api/api/v1/enrollments/user/{userId}/course/{courseId}
GET  /api/api/v1/enrollments/user/{userId}/all
GET  /api/api/v1/enrollments/course/{courseId}/all
GET  /api/api/v1/enrollments/check-access/{userId}/{courseId}
GET  /api/api/v1/enrollments/check-paid-access/{userId}/{courseId}
PUT  /api/api/v1/enrollments/{enrollmentId}/progress
GET  /api/api/v1/enrollments/{enrollmentId}/progress
POST /api/api/v1/enrollments/{enrollmentId}/mark-completed
GET  /api/api/v1/enrollments/{enrollmentId}/is-completed
GET  /api/api/v1/enrollments/user/{userId}/with-progress
GET  /api/api/v1/enrollments/{userId}/{courseId}/can-unenroll
POST /api/api/v1/enrollments/{userId}/{courseId}/unenroll
GET  /api/api/v1/enrollments/{userId}/{courseId}/unenroll-block-reason
GET  /api/api/v1/enrollments/user/{userId}/paid-courses
GET  /api/api/v1/enrollments/user/{userId}/free-courses
GET  /api/api/v1/enrollments/{userId}/{courseId}/is-paid
GET  /api/api/v1/enrollments/{userId}/{courseId}/has-lifetime-access
GET  /api/api/v1/enrollments/user/{userId}/paid-courses-count
GET  /api/api/v1/enrollments/user/{userId}/total-spent
```

---

### 🔔 Notifications (JWT Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/notifications/user/{userId}` | Paginated notifications |
| `GET` | `/api/v1/notifications/user/{userId}/unread/count` | Unread count |
| `PUT` | `/api/v1/notifications/{id}/read` | Mark one as read |
| `PUT` | `/api/v1/notifications/user/{userId}/read-all` | Mark all as read |

```http
GET /api/v1/notifications/user/{userId}
GET /api/v1/notifications/user/{userId}/unread/count
PUT /api/v1/notifications/{id}/read
PUT /api/v1/notifications/user/{userId}/read-all
```

---

### 📊 Transactions (JWT Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/transactions` | Create audit transaction |
| `GET` | `/api/v1/transactions/{id}` | Get by ID |
| `GET` | `/api/v1/transactions/reference/{reference}` | Get by reference |
| `GET` | `/api/v1/transactions/user/{userId}` | User transaction history |
| `GET` | `/api/v1/transactions/user/{userId}/summary` | Spending summary |
| `GET` | `/api/v1/transactions/payment/{paymentId}` | Transactions for payment |
| `GET` | `/api/v1/transactions/filter?type=PAYMENT&status=SUCCESS` | Filter transactions |
| `GET` | `/api/v1/transactions/date-range?startDate={iso}&endDate={iso}` | Date range query |
| `GET` | `/api/v1/transactions/analytics/successful-count` | Successful count |

```http
POST /api/v1/transactions
GET  /api/v1/transactions/{id}
GET  /api/v1/transactions/reference/{reference}
GET  /api/v1/transactions/user/{userId}
GET  /api/v1/transactions/user/{userId}/summary
GET  /api/v1/transactions/payment/{paymentId}
GET  /api/v1/transactions/filter?type=PAYMENT&status=SUCCESS
GET  /api/v1/transactions/date-range?startDate={iso}&endDate={iso}
GET  /api/v1/transactions/analytics/successful-count
```

---

### 🔗 Webhooks (Public — Signature Verified)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/webhooks/paystack` | Paystack event handler |

```http
POST /api/v1/webhooks/paystack
Header: x-paystack-signature: <HMAC-SHA512>
```

---

### ❤️ Health (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/health` | Service + database health |
| `GET` | `/api/health/ping` | Simple liveness (`pong`) |

```http
GET /api/health
GET /api/health/ping
```

---

## 🔐 Authentication & Token Exchange

Protected endpoints require:

```
Authorization: Bearer <jwt_token>
```

### Production Flow (Recommended)

```
┌──────────────────────┐                      ┌──────────────────────┐
│  NEXT.JS FRONTEND    │                      │ JAVA SPRING BACKEND  │
└──────────────────────┘                      └──────────────────────┘
         │                                            │
         │  User logs in (userId available)           │
         ├───────────────────────────────────────────→│
         │  POST /api/v1/auth/service-token          │
         │  X-Service-Secret: <SERVICE_SECRET>       │
         │  ?userId=...&email=...&name=...            │
         │                                            │
         │  ←─────────────────────────────────────────┤
         │  200 OK { token, userId, email, expiresIn }│
         │                                            │
         │  Store token in localStorage               │
         │                                            │
         │  POST /api/v1/payments/initialize          │
         │  Authorization: Bearer <token>             │
         ├───────────────────────────────────────────→│
         │                                            │
         │  ← Payment initialized                     │
```

| Property | Value |
|----------|-------|
| Algorithm | HMAC-SHA512 |
| Expiry | 7 days (`JWT_EXPIRATION=604800000`) |
| Secret | `JWT_SECRET` environment variable |
| Service auth | `SERVICE_SECRET` + `X-Service-Secret` header |

See `TOKEN_EXCHANGE_PATTERN_SUMMARY.md` and `TOKEN_GENERATION_AND_EXCHANGE_GUIDE.md` for full details.

---

## 💳 Payment Flow

### Step-by-Step

1. **Get JWT** — via `/api/v1/auth/service-token` (production) or `/api/v1/auth/generate-token` (testing)
2. **Initialize** — `POST /api/v1/payments/initialize` with `amountCents` in kobo
3. **Redirect** — send user to `authorizationUrl` from response
4. **Webhook** — Paystack notifies `/api/v1/webhooks/paystack` asynchronously
5. **Verify** — frontend calls `GET /api/v1/payments/verify/{reference}` on success page
6. **Access** — enrollment created; user can access the course

### Idempotency

If a `PENDING` payment already exists for the same user and course, the service reuses it instead of creating a duplicate.

---

## 🗄️ Database Schema

### Key Tables

#### Payments

```sql
CREATE TABLE payments (
  id                    UUID PRIMARY KEY,
  reference             VARCHAR(100) UNIQUE NOT NULL,
  user_id               UUID NOT NULL,
  course_id             UUID NOT NULL,
  amount_cents          INTEGER NOT NULL,
  currency              VARCHAR(3),
  original_currency     VARCHAR(3) NOT NULL DEFAULT 'NGN',
  original_amount_cents INTEGER NOT NULL,
  exchange_rate         DECIMAL(10,6),
  settlement_currency   VARCHAR(3),
  status                VARCHAR(50) DEFAULT 'PENDING',
  payment_method        VARCHAR(50),
  paystack_reference    VARCHAR(100),
  paystack_response     JSONB,
  metadata              JSONB,
  paid_at               TIMESTAMP,
  created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP
);
```

#### Enrollments

```sql
CREATE TABLE enrollments (
  id                    UUID PRIMARY KEY,
  user_id               UUID NOT NULL,
  course_id             UUID NOT NULL,
  payment_id            UUID,
  enrolled_at           TIMESTAMP,
  enrolled_price_cents  INTEGER,
  original_amount_cents INTEGER,
  original_currency     VARCHAR(3),
  access_type           VARCHAR DEFAULT 'full',
  enrollment_source     VARCHAR,
  status                VARCHAR DEFAULT 'active',
  can_unenroll          BOOLEAN DEFAULT TRUE,
  progress_percentage   DECIMAL(5,2) DEFAULT 0,
  completed_lessons     INTEGER DEFAULT 0,
  total_lessons         INTEGER,
  total_time_spent      INTEGER DEFAULT 0,
  average_quiz_score    DECIMAL(5,2),
  assignment_average    DECIMAL(5,2),
  overall_grade         DECIMAL(5,2),
  completed_at          TIMESTAMP
);
```

#### Transactions

```sql
CREATE TABLE transactions (
  id                    UUID PRIMARY KEY,
  user_id               UUID NOT NULL,
  payment_id            UUID,
  transaction_type      VARCHAR(50) NOT NULL,
  amount_cents          INTEGER NOT NULL,
  currency              VARCHAR(3),
  original_currency     VARCHAR(3),
  original_amount_cents INTEGER,
  exchange_rate         DECIMAL(10,6),
  status                VARCHAR(50) NOT NULL,
  reference             VARCHAR(255) UNIQUE NOT NULL,
  metadata              JSONB,
  created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Notifications

```sql
CREATE TABLE notifications (
  id                UUID PRIMARY KEY,
  user_id           UUID NOT NULL,
  notification_type VARCHAR(50) NOT NULL,
  title             VARCHAR(255) NOT NULL,
  message           TEXT NOT NULL,
  is_read           BOOLEAN DEFAULT FALSE,
  action_url        VARCHAR(500),
  data              JSONB,
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  read_at           TIMESTAMP
);
```

### Enrollment Creation

Enrollments are created by **`PaymentServiceImpl`** after a successful payment (verify endpoint or webhook) — not via a database trigger.

```java
// Application-level enrollment on payment success
Enrollment enrollment = Enrollment.builder()
    .userId(payment.getUserId())
    .courseId(payment.getCourseId())
    .paymentId(payment.getId())
    .enrolledPriceCents(payment.getOriginalAmountCents())
    .originalAmountCents(payment.getOriginalAmountCents())
    .originalCurrency(payment.getOriginalCurrency())
    .canUnenroll(false)          // Paid = lifetime access
    .enrollmentSource("PAYMENT")
    .status("active")
    .build();
```

### Indexes for Performance

```sql
CREATE INDEX idx_payments_user_id       ON payments(user_id);
CREATE INDEX idx_payments_reference     ON payments(reference);
CREATE INDEX idx_payments_status        ON payments(status);
CREATE INDEX idx_enrollments_user_course ON enrollments(user_id, course_id);
CREATE INDEX idx_enrollments_payment_id ON enrollments(payment_id);
CREATE INDEX idx_notifications_user_id  ON notifications(user_id);
CREATE INDEX idx_notifications_is_read  ON notifications(is_read);
CREATE INDEX idx_transactions_user_id   ON transactions(user_id);
CREATE INDEX idx_transactions_status    ON transactions(status);
```

---

## ⚙️ Configuration

### Spring Profiles

| Profile | Database | Flyway | Use Case |
|---------|----------|--------|----------|
| `dev` | H2 in-memory | Disabled | Quick local development |
| `dev-local` | H2 in-memory | Disabled | Local H2 variant |
| `prod` | PostgreSQL (Neon) | Enabled | Production / staging |

### Application Properties

```yaml
# src/main/resources/application.yml

spring:
  application:
    name: payment-service

  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USER}
    password: ${SMTP_PASSWORD}

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${SERVER_CONTEXT_PATH:/api}

jwt:
  secret: ${JWT_SECRET}
  expiration: 604800000   # 7 days

paystack:
  secret-key: ${PAYSTACK_SECRET_KEY}
  public-key: ${PAYSTACK_PUBLIC_KEY}
  base-url: https://api.paystack.co
  callback-url: ${PAYSTACK_CALLBACK_URL}
  webhook-secret: ${WEBHOOK_SECRET}

app:
  frontend-url: ${FRONTEND_URL:http://localhost:3000}
  service-secret: ${SERVICE_SECRET}
  ratelimit:
    enabled: false   # Set true in production
```

### Rate Limiting (when enabled)

| Endpoint | Limit |
|----------|-------|
| `POST /api/v1/payments/initialize` | 5 requests / minute per user |
| `POST /api/v1/webhooks/paystack` | 100 requests / minute per IP |

---

## 🚢 Deployment

### Production Deployment (Render)

**Build command:**

```bash
mvn clean package -DskipTests
```

**Start command:**

```bash
java -jar target/payment-service-1.0.0.jar
```

**Environment variables on Render:**

```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://user:pass@ep-xxxx.neon.tech/axio_prod?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=<secret>
PAYSTACK_PUBLIC_KEY=pk_live_xxxx
PAYSTACK_SECRET_KEY=sk_live_xxxx
PAYSTACK_CALLBACK_URL=https://your-frontend.com/payment-success
PAYSTACK_WEBHOOK_URL=https://your-api.com/api/v1/webhooks/paystack
JWT_SECRET=<secret>
SERVICE_SECRET=<secret>
WEBHOOK_SECRET=<secret>
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=<app_password>
FRONTEND_URL=https://your-frontend.com
SERVER_CONTEXT_PATH=/api
```

### Database Setup

- Use **Neon PostgreSQL** (free tier available)
- Connection: `postgresql://user:password@ep-xxxx.neon.tech/dbname`
- Flyway runs migrations automatically on startup (`prod` profile)
- `PaymentServiceApplication` auto-converts `postgresql://` URIs to JDBC format

### Production URLs

```
API Base:  https://axio-payment.onrender.com/api
Webhook:   https://axio-payment.onrender.com/api/v1/webhooks/paystack
Health:    https://axio-payment.onrender.com/api/health
Swagger:   https://axio-payment.onrender.com/api/swagger-ui.html
```

Guides: `DOCKER_CICD_RENDER_SETUP_GUIDE.md` · `DEPLOYMENT_QUICK_CHECKLIST.md`

---

## 🔗 Integration with Next.js

### Quick Integration Steps

#### 1. Install Dependencies

```bash
npm install axios swr react-hot-toast
```

#### 2. Create API Client

```typescript
// lib/api.ts
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL; // e.g. http://localhost:8080/api

export const apiClient = {
  async request(endpoint: string, options: RequestInit = {}) {
    const token = localStorage.getItem('authToken');
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    if (!response.ok) throw new Error('API Error');
    return response.json();
  },

  get: (endpoint: string) => apiClient.request(endpoint),
  post: (endpoint: string, body: unknown) =>
    apiClient.request(endpoint, { method: 'POST', body: JSON.stringify(body) }),
  put: (endpoint: string, body: unknown) =>
    apiClient.request(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
};
```

#### 3. Initialize a Payment

```typescript
const response = await apiClient.post('/v1/payments/initialize', {
  userId: user.id,
  courseId: course.id,
  email: user.email,
  amountCents: course.price_cents,  // kobo — e.g. ₦10,000 = 1_000_000
  courseName: course.title,
});

// Redirect to Paystack
window.location.href = response.data.authorizationUrl;
```

#### 4. Full Guide

See **`NEXTJS_PAYMENT_INTEGRATION_GUIDE.md`** for complete examples:

- Generating JWT tokens (service-to-service)
- Initializing and verifying payments
- Checking enrollment status
- Updating progress
- Handling notifications

---

## 🔒 Security

### Features

| Feature | Implementation |
|---------|----------------|
| ✅ JWT Authentication | Stateless tokens, 7-day expiry |
| ✅ Service-to-Service Auth | `X-Service-Secret` header on `/service-token` |
| ✅ Webhook Verification | HMAC-SHA512 (`x-paystack-signature`) |
| ✅ Rate Limiting | Guava in-memory (5/min initialize, 100/min webhooks) |
| ✅ HTTPS/TLS | Encrypted data in transit (production) |
| ✅ SQL Injection Prevention | JPA parameterized queries |
| ✅ CORS Protection | Whitelist via `FRONTEND_URL` + `EXTRA_ALLOWED_ORIGINS` |
| ✅ Environment Secrets | No hardcoded credentials in source |
| ✅ Docker Security | Non-root `appuser` in container |

### Public Endpoints (No JWT)

- `/api/health/**`
- `/api/v1/auth/**`
- `/api/v1/webhooks/paystack`
- `/api/actuator/health`

### Best Practices

```java
// ✅ DO: Use repository methods (parameterized)
Payment payment = paymentRepository.findByReference(reference);

// ❌ DON'T: Concatenate SQL strings
// String query = "SELECT * FROM payments WHERE reference = '" + reference + "'";

// ✅ DO: Validate JWT signatures
jwtTokenProvider.validateToken(token);

// ✅ DO: Verify webhook signatures before processing
webhookVerifier.verifySignature(payload, signature);

// ❌ DON'T: Trust user input without validation
// Always use @Valid on request DTOs
```

---

## 🤝 Contributing

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit changes** (`git commit -m 'Add amazing feature'`)
4. **Push to branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Development Guidelines

- Follow existing code style and module structure
- Write tests for new features
- Update documentation for API changes
- Keep commits atomic with meaningful messages
- Do not commit secrets or `.env` files

---

## 📝 Environment Variables Reference

```env
# Core
SPRING_PROFILES_ACTIVE=dev|prod
SERVER_PORT=8080
SERVER_CONTEXT_PATH=/api
FRONTEND_URL=http://localhost:3000
LOG_LEVEL=INFO

# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/axio_prod
DB_USERNAME=postgres
DB_PASSWORD=password

# Paystack
PAYSTACK_PUBLIC_KEY=pk_test_xxx
PAYSTACK_SECRET_KEY=sk_test_xxx
PAYSTACK_CALLBACK_URL=http://localhost:3000/payment-success
PAYSTACK_WEBHOOK_URL=http://localhost:8080/api/v1/webhooks/paystack
WEBHOOK_SECRET=your_webhook_secret

# Auth
JWT_SECRET=your_32_char_secret_key_here
JWT_EXPIRATION=604800000
SERVICE_SECRET=your_internal_service_secret

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your@gmail.com
SMTP_PASSWORD=app_password
SMTP_FROM=AxioQuan <noreply@axioquan.com>

# CORS (optional extra origins, comma-separated)
EXTRA_ALLOWED_ORIGINS=https://yourdomain.com
```

See **`ENV_VARIABLES_MAPPING.md`** for CI/CD and Render mapping.

---

## 📚 Documentation Files

| Document | Purpose |
|----------|---------|
| **README.md** | This file — project overview |
| **API_ENDPOINTS_COMPREHENSIVE.md** | Detailed API reference with examples |
| **API_QUICK_REFERENCE.md** | Quick endpoint lookup |
| **COMPLETE_API_ENDPOINTS.md** | Production-ready docs with Postman steps |
| **NEXTJS_PAYMENT_INTEGRATION_GUIDE.md** | Frontend integration guide |
| **TOKEN_EXCHANGE_PATTERN_SUMMARY.md** | JWT token exchange architecture |
| **TOKEN_GENERATION_AND_EXCHANGE_GUIDE.md** | Detailed token guide |
| **NOTIFICATION_API_COMPLETE_GUIDE.md** | Notification API reference |
| **ENV_VARIABLES_MAPPING.md** | Environment variable mapping for CI/CD |
| **TESTING_AND_IMPLEMENTATION_GUIDE.md** | Testing and implementation notes |
| **DOCKER_CICD_RENDER_SETUP_GUIDE.md** | Docker & Render deployment guide |
| **DEPLOYMENT_QUICK_CHECKLIST.md** | Pre-deployment checklist |

---

## 🆘 Troubleshooting

### Database Connection Failed

```
Error: org.postgresql.util.PSQLException: Connection refused
```

**Solution:**
- Ensure PostgreSQL is running
- Check `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`
- For Neon, include `sslmode=require` in the connection string
- See `HIKARICP_CONNECTION_ERROR_FIX.md`

### Paystack Payment Fails

```
Error: Invalid API key
```

**Solution:**
- Use correct `PAYSTACK_PUBLIC_KEY` and `PAYSTACK_SECRET_KEY`
- Check test vs. live key environment
- Ensure `amountCents` is in kobo and greater than Paystack minimum

### JWT Token Invalid

```
Error: 401 Unauthorized
```

**Solution:**
- Ensure `JWT_SECRET` is set and consistent
- Check token hasn't expired (7 days)
- Regenerate via `/api/v1/auth/generate-token` or `/api/v1/auth/service-token`
- See `JWT_TOKEN_MALFORMED_FIX.md`

### CORS Errors

```
Error: Access blocked by CORS policy
```

**Solution:**
- Set `FRONTEND_URL` to your frontend origin
- Add extra origins via `EXTRA_ALLOWED_ORIGINS`
- Ensure preflight `OPTIONS` requests pass

### Webhook Not Firing

**Solution:**
- Register `https://<your-domain>/api/v1/webhooks/paystack` in Paystack dashboard
- Verify `WEBHOOK_SECRET` matches Paystack configuration
- Check webhook logs in `webhook_logs` table

---

## 📞 Support

### Need Help?

- 📖 Read the documentation files listed above
- 🐛 Check GitHub Issues
- 💬 Open a Discussion
- 📧 Email: alexander.s.cyril@gmail.com

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

Built using:

- **Spring Boot** — robust, production-ready backend
- **PostgreSQL** — reliable data storage
- **Paystack** — secure African payment processing
- **Next.js** — modern frontend experience

---

## 📊 Project Statistics

```
Language:            Java 17
Framework:           Spring Boot 3.2.5
Total Endpoints:     47
API Modules:         7 (Auth, Payments, Enrollments, Notifications, Transactions, Webhooks, Health)
Database Entities:   8+
Container:           Docker multi-stage (non-root)
API Documentation:   Swagger UI + Markdown guides
Token Expiry:        7 days
Default Currency:    NGN (kobo)
```

---

## 🗓️ Changelog

### Version 1.0.0 (Latest)

- ✅ Complete Paystack payment processing
- ✅ Student enrollment management with progress tracking
- ✅ Transaction audit logging
- ✅ In-app notification system + async email
- ✅ JWT authentication + service-to-service token exchange
- ✅ Paystack webhook handling with signature verification
- ✅ Rate limiting on payment endpoints
- ✅ Docker & Render deployment support
- ✅ 47 API endpoints across 7 modules
- ✅ Kobo-based pricing model (NGN)

---

**Last Updated:** June 13, 2026  
**Status:** Production Ready ✅  
**Maintainer:** Axio Quan Team

---

**Built by Alexander S. Cyril 🎉**
