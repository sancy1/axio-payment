# Payment Service - API Quick Reference Guide

**Generated:** April 3, 2026

---

## 🔐 Authentication Endpoints

### Generate JWT Token
```
POST /v1/auth/generate-token?userId={userId}
POST /v1/auth/generate-token-by-email?email={email}
POST /v1/auth/generate-token-with-role?email={email}&role={role}

Response: { token, userId, email, name, expiresIn }
```

### Validate Token
```
POST /v1/auth/validate-token?token={token}

Response: { valid, expired, userId, email, role }
```

---

## 💳 Payment Processing Endpoints

### Initialize Payment
```
POST /v1/payments/initialize
Body: { userId, courseId, amount, currency, paymentMethod, metadata }

Response: { reference, paystackUrl, amount, status }
Status: 201 Created
```

### Verify Payment
```
GET /v1/payments/verify/{reference}
GET /v1/payments/reference/{reference}

Response: { reference, status, amount, paidAt, userId, courseId }
```

### Check Purchase Status
```
GET /v1/payments/user/{userId}/course/{courseId}/status

Response: true/false
```

---

## 🎓 Enrollment Management Endpoints

### Basic Enrollment Info
```
GET /api/v1/enrollments/user/{userId}/course/{courseId}
  → Single enrollment details

GET /api/v1/enrollments/user/{userId}/all
  → All user enrollments

GET /api/v1/enrollments/course/{courseId}/all
  → All course enrollments

GET /api/v1/enrollments/payment/{paymentId}
  → Enrollment from payment ID
```

### Access Control
```
GET /api/v1/enrollments/check-access/{userId}/{courseId}
GET /api/v1/enrollments/check-paid-access/{userId}/{courseId}

Response: true/false
```

### Progress Tracking
```
PUT /api/v1/enrollments/{enrollmentId}/progress
Body: { lessonsCompleted, quizzesCompleted, assignmentsCompleted }

GET /api/v1/enrollments/{enrollmentId}/progress
  → Returns progress percentage (0-100)

POST /api/v1/enrollments/{enrollmentId}/mark-completed
GET /api/v1/enrollments/{enrollmentId}/is-completed
```

### Course Type Management
```
GET /api/v1/enrollments/user/{userId}/paid-courses
GET /api/v1/enrollments/user/{userId}/free-courses
GET /api/v1/enrollments/{userId}/{courseId}/is-paid
GET /api/v1/enrollments/{userId}/{courseId}/has-lifetime-access
```

### Unenrollment Control
```
GET /api/v1/enrollments/{userId}/{courseId}/can-unenroll
POST /api/v1/enrollments/{userId}/{courseId}/unenroll
GET /api/v1/enrollments/{userId}/{courseId}/unenroll-block-reason
```

### Analytics
```
GET /api/v1/enrollments/user/{userId}/paid-courses-count
  → Returns count

GET /api/v1/enrollments/user/{userId}/total-spent
  → Returns amount in cents
```

---

## 🔔 Notification Endpoints

### Get Notifications
```
GET /v1/notifications/user/{userId}?page=0&size=20

Response: Page<NotificationResponse> with pagination metadata
```

### Notification Management
```
GET /v1/notifications/user/{userId}/unread/count
  → Returns { userId, unreadCount }

PUT /v1/notifications/{id}/read
PUT /v1/notifications/user/{userId}/read-all
  → Mark notifications as read
```

---

## 📊 Transaction Endpoints

### Create & Retrieve
```
POST /v1/transactions
Body: { reference, userId, paymentId, type, status, amount, currency, description }

GET /v1/transactions/{id}
GET /v1/transactions/reference/{reference}
```

### User Transactions
```
GET /v1/transactions/user/{userId}
  → List all user transactions

GET /v1/transactions/user/{userId}/summary
  → { totalTransactions, completedCount, pendingCount, failedCount, totalAmount }
```

### Payment Transactions
```
GET /v1/transactions/payment/{paymentId}
```

### Filtering & Analytics
```
GET /v1/transactions/filter?type={type}&status={status}
GET /v1/transactions/date-range?startDate={iso}&endDate={iso}
GET /v1/transactions/analytics/successful-count
```

---

## 🪝 Webhook Endpoint

### Paystack Webhook
```
POST /v1/webhooks/paystack
Header: x-paystack-signature: {signature}
Body: { event, data: { id, reference, amount, status, metadata } }

Response: "Webhook processed successfully" (200 OK)
Note: Always return 200 to acknowledge, even on errors
```

---

## ❤️ Health & Status Endpoints

### Health Check
```
GET /health
Response: { status, timestamp, database }

GET /health/ping
Response: "pong"
```

---

## 📋 Endpoint Count by Module

| Module | Count | Base Path |
|--------|-------|-----------|
| Auth | 4 | `/v1/auth` |
| Payments | 5 | `/v1/payments` |
| Enrollments | 20 | `/api/v1/enrollments` |
| Notifications | 4 | `/v1/notifications` |
| Transactions | 9 | `/v1/transactions` |
| Webhooks | 1 | `/v1/webhooks` |
| Health | 2 | `/health` |
| **TOTAL** | **45** | - |

---

## 🔗 Common Patterns

### Authenticated Endpoints
For endpoints requiring JWT:
```
Header: Authorization: Bearer {token}
Token obtained from: /v1/auth/generate-token-by-email?email={email}
```

### Standard Response Format
```json
{
  "success": true,
  "message": "Operation description",
  "data": { /* endpoint-specific data */ }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## ⚡ Common Status Codes

- **200** - Success
- **201** - Created
- **400** - Bad Request
- **401** - Unauthorized (Invalid signature/token)
- **403** - Forbidden (Access denied)
- **404** - Not Found
- **503** - Service Unavailable

---

## 🎯 Quick Usage Examples

### Get User's Paid Courses
```bash
curl -X GET "http://localhost:8080/api/v1/enrollments/user/{userId}/paid-courses"
```

### Check Course Access
```bash
curl -X GET "http://localhost:8080/api/v1/enrollments/check-access/{userId}/{courseId}"
```

### Initialize Payment
```bash
curl -X POST "http://localhost:8080/v1/payments/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "uuid",
    "courseId": "uuid",
    "amount": 50000,
    "currency": "NGN"
  }'
```

### Update Progress
```bash
curl -X PUT "http://localhost:8080/api/v1/enrollments/{enrollmentId}/progress" \
  -H "Content-Type: application/json" \
  -d '{
    "lessonsCompleted": 5,
    "quizzesCompleted": 2,
    "assignmentsCompleted": 1
  }'
```

### Get Transaction Summary
```bash
curl -X GET "http://localhost:8080/v1/transactions/user/{userId}/summary"
```

---

## 📞 Support

For detailed endpoint information, see `API_ENDPOINTS_COMPREHENSIVE.md`

**Missing Modules:**
- ❌ Customers (No CustomerController)
- ❌ Courses (No CourseController)
- ❌ Refunds (No RefundController)
