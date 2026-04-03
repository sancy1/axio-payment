# Payment Service - Complete REST API Endpoints Documentation

**Generated:** April 3, 2026  
**Project:** Payment Service  
**Version:** 1.0.0

---

## Table of Contents
1. [Authentication Module](#1-authentication-module)
2. [Payment Module](#2-payment-module)
3. [Webhook Module](#3-webhook-module)
4. [Enrollment Module](#4-enrollment-module)
5. [Notification Module](#5-notification-module)
6. [Transaction Module](#6-transaction-module)
7. [Health Check Module](#7-health-check-module)
8. [Module Summary](#module-summary)

---

## 1. Authentication Module

**Base Path:** `/v1/auth`  
**Controller:** `AuthController`  
**Authentication:** Not required for most endpoints (for testing)

### 1.1 Generate JWT Token
- **HTTP Method:** `POST`
- **Full Path:** `/v1/auth/generate-token`
- **Endpoint Name:** Generate JWT Token (Testing Only)
- **Authentication Required:** No
- **Path Parameters:** None
- **Query Parameters:**
  - `userId` (required): UUID of the user
- **Request Body:** None (query param based)
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "JWT token generated successfully",
    "data": {
      "token": "jwt_token_string",
      "userId": "uuid",
      "email": "user@email.com",
      "name": "User Name",
      "expiresIn": "7 days"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Token generated successfully
  - `404 Not Found`: User not found

---

### 1.2 Generate JWT Token by Email
- **HTTP Method:** `POST`
- **Full Path:** `/v1/auth/generate-token-by-email`
- **Endpoint Name:** Generate JWT Token by Email (Testing Only)
- **Authentication Required:** No
- **Path Parameters:** None
- **Query Parameters:**
  - `email` (required): User's email address
- **Request Body:** None (query param based)
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "JWT token generated successfully",
    "data": {
      "token": "jwt_token_string",
      "userId": "uuid",
      "email": "user@email.com",
      "name": "User Name",
      "expiresIn": "7 days"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Token generated successfully
  - `404 Not Found`: User with email not found

---

### 1.3 Validate JWT Token
- **HTTP Method:** `POST`
- **Full Path:** `/v1/auth/validate-token`
- **Endpoint Name:** Validate JWT Token
- **Authentication Required:** No
- **Path Parameters:** None
- **Query Parameters:**
  - `token` (required): JWT token string
- **Request Body:** None (query param based)
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Token validated",
    "data": {
      "valid": true,
      "expired": false,
      "userId": "uuid",
      "email": "user@email.com",
      "role": "student"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Token validation complete

---

### 1.4 Generate JWT Token with Custom Role
- **HTTP Method:** `POST`
- **Full Path:** `/v1/auth/generate-token-with-role`
- **Endpoint Name:** Generate JWT Token with Custom Role
- **Authentication Required:** No
- **Path Parameters:** None
- **Query Parameters:**
  - `email` (required): User's email address
  - `role` (optional): Role identifier (student, instructor, admin)
- **Request Body:** None (query params based)
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "JWT token generated successfully with role",
    "data": {
      "token": "jwt_token_string",
      "userId": "uuid",
      "email": "user@email.com",
      "name": "User Name",
      "role": "instructor",
      "expiresIn": "7 days"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Token generated successfully
  - `404 Not Found`: User not found

---

## 2. Payment Module

**Base Path:** `/v1/payments`  
**Controller:** `PaymentController`  
**Authentication:** Optional (Bearer token)

### 2.1 Initialize Payment
- **HTTP Method:** `POST`
- **Full Path:** `/v1/payments/initialize`
- **Endpoint Name:** Initialize a Payment
- **Authentication Required:** No (but Bearer token optional)
- **Path Parameters:** None
- **Request Body:**
  ```json
  {
    "userId": "uuid",
    "courseId": "uuid",
    "amount": 50000,
    "currency": "NGN",
    "paymentMethod": "paystack",
    "metadata": {
      "key": "value"
    }
  }
  ```
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Payment initialized successfully",
    "data": {
      "reference": "payment_reference_string",
      "paystackUrl": "https://checkout.paystack.com/...",
      "amount": 50000,
      "currency": "NGN",
      "status": "pending",
      "expiresAt": "2026-04-03T12:00:00Z"
    }
  }
  ```
- **Response Codes:**
  - `201 Created`: Payment initialized successfully
  - `400 Bad Request`: Invalid request data
  - `404 Not Found`: User or course not found

---

### 2.2 Verify Payment
- **HTTP Method:** `GET`
- **Full Path:** `/v1/payments/verify/{reference}`
- **Endpoint Name:** Verify Payment
- **Authentication Required:** No
- **Path Parameters:**
  - `reference` (required): Payment reference string
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Payment verified successfully",
    "data": {
      "reference": "payment_reference_string",
      "status": "completed",
      "amount": 50000,
      "currency": "NGN",
      "paidAt": "2026-04-03T10:30:00Z",
      "userId": "uuid",
      "courseId": "uuid"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Payment verified successfully
  - `404 Not Found`: Payment not found

---

### 2.3 Get Payment by Reference
- **HTTP Method:** `GET`
- **Full Path:** `/v1/payments/reference/{reference}`
- **Endpoint Name:** Get Payment by Reference
- **Authentication Required:** No
- **Path Parameters:**
  - `reference` (required): Payment reference string
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Payment retrieved successfully",
    "data": {
      "reference": "payment_reference_string",
      "status": "completed",
      "amount": 50000,
      "currency": "NGN",
      "paidAt": "2026-04-03T10:30:00Z"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Payment retrieved successfully
  - `404 Not Found`: Payment not found

---

### 2.4 Check Course Purchase Status
- **HTTP Method:** `GET`
- **Full Path:** `/v1/payments/user/{userId}/course/{courseId}/status`
- **Endpoint Name:** Check Purchase Status
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Purchase status retrieved",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Status retrieved successfully

---

### 2.5 Get User's Purchased Courses
- **HTTP Method:** `GET`
- **Full Path:** `/v1/payments/user/{userId}/courses`
- **Endpoint Name:** Get User's Purchased Courses
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Feature coming soon",
    "data": null
  }
  ```
- **Response Codes:**
  - `200 OK`: Feature endpoint (future implementation)

---

## 3. Webhook Module

**Base Path:** `/v1/webhooks`  
**Controller:** `WebhookController`  
**Authentication:** Signature verification (x-paystack-signature header)

### 3.1 Handle Paystack Webhook
- **HTTP Method:** `POST`
- **Full Path:** `/v1/webhooks/paystack`
- **Endpoint Name:** Handle Paystack Webhook
- **Authentication Required:** Signature Verification (Header: x-paystack-signature)
- **Path Parameters:** None
- **Request Headers:**
  - `x-paystack-signature` (required): HMAC signature for webhook verification
- **Request Body:**
  ```json
  {
    "event": "charge.success",
    "data": {
      "id": 123456,
      "reference": "payment_reference",
      "amount": 5000000,
      "currency": "NGN",
      "status": "success",
      "customer": {
        "email": "customer@example.com"
      },
      "metadata": {
        "userId": "uuid",
        "courseId": "uuid"
      }
    }
  }
  ```
- **Response Format:**
  ```text
  Webhook processed successfully
  ```
- **Response Codes:**
  - `200 OK`: Webhook processed successfully
  - `401 Unauthorized`: Invalid signature

---

## 4. Enrollment Module

**Base Path:** `/api/v1/enrollments`  
**Controller:** `EnrollmentController`  
**Authentication:** Not required

### 4.1 Get Enrollment
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/course/{courseId}`
- **Endpoint Name:** Get Enrollment
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Enrollment retrieved",
    "data": {
      "id": "uuid",
      "userId": "uuid",
      "courseId": "uuid",
      "enrolledAt": "2026-04-01T10:00:00Z",
      "progress": 45.50,
      "status": "active"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Enrollment retrieved successfully
  - `404 Not Found`: Enrollment not found

---

### 4.2 Get User Enrollments
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/all`
- **Endpoint Name:** Get User Enrollments
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "User enrollments retrieved",
    "data": [
      {
        "id": "uuid",
        "userId": "uuid",
        "courseId": "uuid",
        "enrolledAt": "2026-04-01T10:00:00Z",
        "progress": 45.50,
        "status": "active"
      }
    ]
  }
  ```
- **Response Codes:**
  - `200 OK`: Enrollments retrieved successfully

---

### 4.3 Get Course Enrollments
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/course/{courseId}/all`
- **Endpoint Name:** Get Course Enrollments
- **Authentication Required:** No
- **Path Parameters:**
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Course enrollments retrieved",
    "data": [
      {
        "id": "uuid",
        "userId": "uuid",
        "courseId": "uuid",
        "enrolledAt": "2026-04-01T10:00:00Z",
        "progress": 45.50,
        "status": "active"
      }
    ]
  }
  ```
- **Response Codes:**
  - `200 OK`: Enrollments retrieved successfully

---

### 4.4 Get Enrollment by Payment
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/payment/{paymentId}`
- **Endpoint Name:** Get Enrollment by Payment
- **Authentication Required:** No
- **Path Parameters:**
  - `paymentId` (required): UUID of payment
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Enrollment retrieved",
    "data": {
      "id": "uuid",
      "userId": "uuid",
      "courseId": "uuid",
      "paymentId": "uuid",
      "enrolledAt": "2026-04-01T10:00:00Z"
    }
  }
  ```
- **Response Codes:**
  - `200 OK`: Enrollment retrieved successfully
  - `404 Not Found`: Enrollment not found

---

### 4.5 Check Course Access
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/check-access/{userId}/{courseId}`
- **Endpoint Name:** Check Course Access
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Access check completed",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Access check completed

---

### 4.6 Check Paid Access
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/check-paid-access/{userId}/{courseId}`
- **Endpoint Name:** Check Paid Access
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Paid access check completed",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Paid access check completed

---

### 4.7 Update Enrollment Progress
- **HTTP Method:** `PUT`
- **Full Path:** `/api/v1/enrollments/{enrollmentId}/progress`
- **Endpoint Name:** Update Enrollment Progress
- **Authentication Required:** No
- **Path Parameters:**
  - `enrollmentId` (required): UUID of enrollment
- **Request Body:**
  ```json
  {
    "lessonsCompleted": 5,
    "quizzesCompleted": 2,
    "assignmentsCompleted": 1
  }
  ```
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Progress updated successfully",
    "data": null
  }
  ```
- **Response Codes:**
  - `200 OK`: Progress updated successfully
  - `404 Not Found`: Enrollment not found

---

### 4.8 Get Enrollment Progress
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/{enrollmentId}/progress`
- **Endpoint Name:** Get Enrollment Progress
- **Authentication Required:** No
- **Path Parameters:**
  - `enrollmentId` (required): UUID of enrollment
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Progress retrieved",
    "data": 45.50
  }
  ```
- **Response Codes:**
  - `200 OK`: Progress retrieved successfully

---

### 4.9 Mark Enrollment as Completed
- **HTTP Method:** `POST`
- **Full Path:** `/api/v1/enrollments/{enrollmentId}/mark-completed`
- **Endpoint Name:** Mark Enrollment as Completed
- **Authentication Required:** No
- **Path Parameters:**
  - `enrollmentId` (required): UUID of enrollment
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Enrollment marked as completed",
    "data": null
  }
  ```
- **Response Codes:**
  - `200 OK`: Enrollment marked as completed

---

### 4.10 Check if Enrollment is Completed
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/{enrollmentId}/is-completed`
- **Endpoint Name:** Check if Enrollment is Completed
- **Authentication Required:** No
- **Path Parameters:**
  - `enrollmentId` (required): UUID of enrollment
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Completion status retrieved",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Completion status retrieved

---

### 4.11 Get User Enrollments with Progress
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/with-progress`
- **Endpoint Name:** Get User Enrollments with Progress
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "User enrollments with progress retrieved",
    "data": [
      {
        "id": "uuid",
        "userId": "uuid",
        "courseId": "uuid",
        "progress": 45.50,
        "isCompleted": false
      }
    ]
  }
  ```
- **Response Codes:**
  - `200 OK`: Enrollments with progress retrieved

---

### 4.12 Check if Can Unenroll
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/{userId}/{courseId}/can-unenroll`
- **Endpoint Name:** Check if Can Unenroll
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Unenrollment eligibility checked",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Eligibility checked

---

### 4.13 Unenroll from Course
- **HTTP Method:** `POST`
- **Full Path:** `/api/v1/enrollments/{userId}/{courseId}/unenroll`
- **Endpoint Name:** Unenroll from Course
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Unenrolled from course successfully",
    "data": null
  }
  ```
- **Response Codes:**
  - `200 OK`: Unenrolled successfully
  - `403 Forbidden`: Cannot unenroll (paid course)

---

### 4.14 Get Unenrollment Block Reason
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/{userId}/{courseId}/unenroll-block-reason`
- **Endpoint Name:** Get Unenrollment Block Reason
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Block reason retrieved",
    "data": "User has paid for this course and cannot unenroll"
  }
  ```
- **Response Codes:**
  - `200 OK`: Block reason retrieved

---

### 4.15 Get User's Paid Courses
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/paid-courses`
- **Endpoint Name:** Get User's Paid Courses
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Paid courses retrieved",
    "data": [
      {
        "id": "uuid",
        "userId": "uuid",
        "courseId": "uuid",
        "courseTitle": "Course Name",
        "price": 50000,
        "currency": "NGN"
      }
    ]
  }
  ```
- **Response Codes:**
  - `200 OK`: Paid courses retrieved

---

### 4.16 Get User's Free Courses
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/free-courses`
- **Endpoint Name:** Get User's Free Courses
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Free courses retrieved",
    "data": [
      {
        "id": "uuid",
        "userId": "uuid",
        "courseId": "uuid",
        "courseTitle": "Free Course Name"
      }
    ]
  }
  ```
- **Response Codes:**
  - `200 OK`: Free courses retrieved

---

### 4.17 Check if Enrollment is Paid
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/{userId}/{courseId}/is-paid`
- **Endpoint Name:** Check if Enrollment is Paid
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Enrollment payment status retrieved",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Payment status retrieved

---

### 4.18 Check Lifetime Access
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/{userId}/{courseId}/has-lifetime-access`
- **Endpoint Name:** Check Lifetime Access
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
  - `courseId` (required): UUID of course
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Lifetime access status retrieved",
    "data": true
  }
  ```
- **Response Codes:**
  - `200 OK`: Lifetime access status retrieved

---

### 4.19 Count Paid Enrollments
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/paid-courses-count`
- **Endpoint Name:** Count Paid Enrollments
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Paid enrollment count retrieved",
    "data": 3
  }
  ```
- **Response Codes:**
  - `200 OK`: Count retrieved

---

### 4.20 Get Total Spent on Courses
- **HTTP Method:** `GET`
- **Full Path:** `/api/v1/enrollments/user/{userId}/total-spent`
- **Endpoint Name:** Get Total Spent on Courses
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "success": true,
    "message": "Total spent retrieved",
    "data": 150000
  }
  ```
- **Response Codes:**
  - `200 OK`: Total amount retrieved (in cents)

---

## 5. Notification Module

**Base Path:** `/v1/notifications`  
**Controller:** `NotificationController`  
**Authentication:** Not required

### 5.1 Get User Notifications (Paginated)
- **HTTP Method:** `GET`
- **Full Path:** `/v1/notifications/user/{userId}`
- **Endpoint Name:** Get User Notifications
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Query Parameters:**
  - `page` (optional, default: 0): Page number (0-indexed)
  - `size` (optional, default: 20): Page size
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "content": [
      {
        "id": "uuid",
        "userId": "uuid",
        "title": "Notification Title",
        "message": "Notification message",
        "type": "payment_success",
        "isRead": false,
        "createdAt": "2026-04-03T10:00:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 50,
    "totalPages": 3
  }
  ```
- **Response Codes:**
  - `200 OK`: Notifications retrieved successfully

---

### 5.2 Get Unread Notification Count
- **HTTP Method:** `GET`
- **Full Path:** `/v1/notifications/user/{userId}/unread/count`
- **Endpoint Name:** Get Unread Notification Count
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "userId": "uuid",
    "unreadCount": 5
  }
  ```
- **Response Codes:**
  - `200 OK`: Unread count retrieved

---

### 5.3 Mark Notification as Read
- **HTTP Method:** `PUT`
- **Full Path:** `/v1/notifications/{id}/read`
- **Endpoint Name:** Mark Notification as Read
- **Authentication Required:** No
- **Path Parameters:**
  - `id` (required): UUID of notification
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "notificationId": "uuid",
    "success": true,
    "message": "Notification marked as read"
  }
  ```
- **Response Codes:**
  - `200 OK`: Notification marked as read

---

### 5.4 Mark All Notifications as Read
- **HTTP Method:** `PUT`
- **Full Path:** `/v1/notifications/user/{userId}/read-all`
- **Endpoint Name:** Mark All Notifications as Read
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "userId": "uuid",
    "message": "All notifications marked as read"
  }
  ```
- **Response Codes:**
  - `200 OK`: All notifications marked as read

---

## 6. Transaction Module

**Base Path:** `/v1/transactions`  
**Controller:** `TransactionController`  
**Authentication:** Not required

### 6.1 Create Transaction
- **HTTP Method:** `POST`
- **Full Path:** `/v1/transactions`
- **Endpoint Name:** Create Transaction
- **Authentication Required:** No
- **Path Parameters:** None
- **Request Body:**
  ```json
  {
    "reference": "txn_reference_123",
    "userId": "uuid",
    "paymentId": "uuid",
    "type": "PAYMENT",
    "status": "COMPLETED",
    "amount": 50000,
    "currency": "NGN",
    "description": "Course payment"
  }
  ```
- **Response Format:**
  ```json
  {
    "id": "uuid",
    "reference": "txn_reference_123",
    "userId": "uuid",
    "paymentId": "uuid",
    "type": "PAYMENT",
    "status": "COMPLETED",
    "amount": 50000,
    "currency": "NGN",
    "description": "Course payment",
    "createdAt": "2026-04-03T10:00:00Z"
  }
  ```
- **Response Codes:**
  - `201 Created`: Transaction created successfully
  - `400 Bad Request`: Invalid request data

---

### 6.2 Get Transaction by ID
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/{id}`
- **Endpoint Name:** Get Transaction by ID
- **Authentication Required:** No
- **Path Parameters:**
  - `id` (required): UUID of transaction
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "id": "uuid",
    "reference": "txn_reference_123",
    "userId": "uuid",
    "paymentId": "uuid",
    "type": "PAYMENT",
    "status": "COMPLETED",
    "amount": 50000,
    "currency": "NGN",
    "createdAt": "2026-04-03T10:00:00Z"
  }
  ```
- **Response Codes:**
  - `200 OK`: Transaction retrieved successfully
  - `404 Not Found`: Transaction not found

---

### 6.3 Get Transaction by Reference
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/reference/{reference}`
- **Endpoint Name:** Get Transaction by Reference
- **Authentication Required:** No
- **Path Parameters:**
  - `reference` (required): Transaction reference string
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "id": "uuid",
    "reference": "txn_reference_123",
    "userId": "uuid",
    "type": "PAYMENT",
    "status": "COMPLETED",
    "amount": 50000,
    "currency": "NGN",
    "createdAt": "2026-04-03T10:00:00Z"
  }
  ```
- **Response Codes:**
  - `200 OK`: Transaction retrieved successfully
  - `404 Not Found`: Transaction not found

---

### 6.4 Get User Transactions
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/user/{userId}`
- **Endpoint Name:** Get User Transactions
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  [
    {
      "id": "uuid",
      "reference": "txn_reference_123",
      "userId": "uuid",
      "type": "PAYMENT",
      "status": "COMPLETED",
      "amount": 50000,
      "currency": "NGN",
      "createdAt": "2026-04-03T10:00:00Z"
    }
  ]
  ```
- **Response Codes:**
  - `200 OK`: Transactions retrieved successfully

---

### 6.5 Get User Transaction Summary
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/user/{userId}/summary`
- **Endpoint Name:** Get User Transaction Summary
- **Authentication Required:** No
- **Path Parameters:**
  - `userId` (required): UUID of user
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "userId": "uuid",
    "totalTransactions": 15,
    "completedCount": 12,
    "pendingCount": 2,
    "failedCount": 1,
    "totalAmount": 750000,
    "currency": "NGN"
  }
  ```
- **Response Codes:**
  - `200 OK`: Summary retrieved successfully

---

### 6.6 Get Payment Transactions
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/payment/{paymentId}`
- **Endpoint Name:** Get Payment Transactions
- **Authentication Required:** No
- **Path Parameters:**
  - `paymentId` (required): UUID of payment
- **Request Body:** None
- **Response Format:**
  ```json
  [
    {
      "id": "uuid",
      "reference": "txn_reference_123",
      "paymentId": "uuid",
      "type": "PAYMENT",
      "status": "COMPLETED",
      "amount": 50000,
      "currency": "NGN",
      "createdAt": "2026-04-03T10:00:00Z"
    }
  ]
  ```
- **Response Codes:**
  - `200 OK`: Transactions retrieved successfully

---

### 6.7 Get Transactions by Type and Status
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/filter`
- **Endpoint Name:** Get Transactions by Type and Status
- **Authentication Required:** No
- **Path Parameters:** None
- **Query Parameters:**
  - `type` (required): Transaction type (e.g., PAYMENT, REFUND)
  - `status` (required): Transaction status (e.g., COMPLETED, PENDING, FAILED)
- **Request Body:** None
- **Response Format:**
  ```json
  [
    {
      "id": "uuid",
      "reference": "txn_reference_123",
      "type": "PAYMENT",
      "status": "COMPLETED",
      "amount": 50000,
      "createdAt": "2026-04-03T10:00:00Z"
    }
  ]
  ```
- **Response Codes:**
  - `200 OK`: Transactions retrieved successfully

---

### 6.8 Get Transactions by Date Range
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/date-range`
- **Endpoint Name:** Get Transactions by Date Range
- **Authentication Required:** No
- **Path Parameters:** None
- **Query Parameters:**
  - `startDate` (required): Start date in ISO format (yyyy-MM-dd'T'HH:mm:ss)
  - `endDate` (required): End date in ISO format (yyyy-MM-dd'T'HH:mm:ss)
- **Request Body:** None
- **Response Format:**
  ```json
  [
    {
      "id": "uuid",
      "reference": "txn_reference_123",
      "type": "PAYMENT",
      "status": "COMPLETED",
      "amount": 50000,
      "createdAt": "2026-04-03T10:00:00Z"
    }
  ]
  ```
- **Response Codes:**
  - `200 OK`: Transactions retrieved successfully

---

### 6.9 Get Successful Payment Count
- **HTTP Method:** `GET`
- **Full Path:** `/v1/transactions/analytics/successful-count`
- **Endpoint Name:** Get Successful Payment Count
- **Authentication Required:** No
- **Path Parameters:** None
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "metric": "successful_transactions",
    "value": 1250
  }
  ```
- **Response Codes:**
  - `200 OK`: Count retrieved successfully

---

## 7. Health Check Module

**Base Path:** `/health`  
**Controller:** `HealthController`  
**Authentication:** Not required

### 7.1 Health Check
- **HTTP Method:** `GET`
- **Full Path:** `/health`
- **Endpoint Name:** Health Check
- **Authentication Required:** No
- **Path Parameters:** None
- **Request Body:** None
- **Response Format:**
  ```json
  {
    "status": "UP",
    "timestamp": "2026-04-03T10:00:00Z",
    "database": "CONNECTED"
  }
  ```
- **Response Codes:**
  - `200 OK`: Service is healthy
  - `503 Service Unavailable`: Service or database is down

---

### 7.2 Ping
- **HTTP Method:** `GET`
- **Full Path:** `/health/ping`
- **Endpoint Name:** Ping
- **Authentication Required:** No
- **Path Parameters:** None
- **Request Body:** None
- **Response Format:**
  ```text
  pong
  ```
- **Response Codes:**
  - `200 OK`: Service is responding

---

## Module Summary

| Module | Base Path | Controller | Total Endpoints | Auth Required |
|--------|-----------|------------|-----------------|----------------|
| Authentication | `/v1/auth` | AuthController | 4 | No |
| Payment | `/v1/payments` | PaymentController | 5 | Optional |
| Webhook | `/v1/webhooks` | WebhookController | 1 | Signature Verification |
| Enrollment | `/api/v1/enrollments` | EnrollmentController | 20 | No |
| Notification | `/v1/notifications` | NotificationController | 4 | No |
| Transaction | `/v1/transactions` | TransactionController | 9 | No |
| Health | `/health` | HealthController | 2 | No |
| **TOTAL** | - | **7 Controllers** | **45 Endpoints** | - |

---

## Missing Modules

The following requested modules were **NOT found** in the codebase:
- ❌ Customers Module (No CustomerController)
- ❌ Courses Module (No CourseController)
- ❌ Refunds Module (No RefundController)

---

## Common Response Wrapper

All endpoints (except webhooks and health) use a common response wrapper:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "id": "uuid",
    "field": "value"
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## Authentication Methods

1. **JWT Bearer Token** (Optional for Payment endpoints)
   - Header: `Authorization: Bearer <jwt_token>`
   - Obtained from: `/v1/auth/generate-token*`

2. **Paystack Webhook Signature Verification** (Webhook endpoint)
   - Header: `x-paystack-signature`
   - Verified against request body HMAC

---

## Common Query Parameters

- **Pagination:** `page` (0-indexed), `size` (default: 20)
- **Sorting:** `sort` (format: `field,asc|desc`)
- **Date Filtering:** `startDate`, `endDate` (ISO 8601 format)

---

## Status Codes Reference

| Code | Meaning |
|------|---------|
| `200` | OK - Request successful |
| `201` | Created - Resource created successfully |
| `400` | Bad Request - Invalid request data |
| `401` | Unauthorized - Invalid signature or token |
| `403` | Forbidden - Access denied |
| `404` | Not Found - Resource not found |
| `429` | Too Many Requests - Rate limit exceeded |
| `500` | Internal Server Error |
| `503` | Service Unavailable - Database down |

---

**Document Version:** 1.0  
**Last Updated:** April 3, 2026  
**Total Endpoints Documented:** 45
