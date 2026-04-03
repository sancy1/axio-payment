# 🚀 TESTING & IMPLEMENTATION GUIDE - Phase 1-4

**Date:** April 3, 2026  
**Status:** Production Ready  

---

## ✅ VERIFICATION: What's Already Done

| Item | Status | Details |
|------|--------|---------|
| **Webhook Handler** | ✅ DONE | ProcessWebhook + handleSuccess + handleFailure |
| **Payment Notifications** | ✅ DONE | Auto-creates on payment success |
| **Transaction Logging** | ✅ DONE | Audit trail created automatically |
| **Error Handling** | ✅ DONE | Webhook logging for debugging |
| **JWT Security** | ✅ DONE | Enforced on all protected endpoints |

---

## 🔑 PHASE 1: TEST ENDPOINTS WITH JWT

### Step 1: Generate JWT Token

```bash
curl -X POST https://axio-payment.onrender.com/api/v1/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 604800000
}
```

Copy the `token` value - you'll use this for all protected endpoints.

---

### Step 2: Test Endpoints WITH JWT Token

**Replace `YOUR_JWT_TOKEN` with the token from Step 1**

#### ✅ Initialize Payment
```bash
curl -X POST https://axio-payment.onrender.com/api/v1/payments/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "17c5e646-112c-45b0-bdd8-8d6bccd705c1",
    "courseId": "846e3596-d727-42be-b2ff-d44aa4869a82",
    "amount": 50000,
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Payment initialized successfully",
  "data": {
    "reference": "PAY_550e8400-e29b-41d4-a716-446655440000_1712145893",
    "authorizationUrl": "https://checkout.paystack.com/abc123xyz",
    "accessCode": "abc123xyz"
  }
}
```

#### ✅ Get Notifications
```bash
curl -X GET "https://axio-payment.onrender.com/api/v1/notifications/user/17c5e646-112c-45b0-bdd8-8d6bccd705c1?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ✅ Get Unread Count
```bash
curl -X GET https://axio-payment.onrender.com/api/v1/notifications/user/17c5e646-112c-45b0-bdd8-8d6bccd705c1/unread/count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ✅ Mark Notification as Read
```bash
curl -X PUT https://axio-payment.onrender.com/api/v1/notifications/{notificationId}/read \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### Step 3: WITHOUT JWT (Should Fail)

```bash
curl -X POST https://axio-payment.onrender.com/api/v1/payments/initialize \
  -H "Content-Type: application/json" \
  -d '{...}'
```

**Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "JWT token required"
}
```

✅ This means JWT is working!

---

## 🔄 PHASE 3: WEBHOOK HANDLER (ALREADY IMPLEMENTED)

### How It Works

```
1. User completes payment on Paystack
   ↓
2. Paystack sends POST to:
   https://axio-payment.onrender.com/api/v1/webhooks/paystack
   ↓
3. WebhookController receives request
   - Verifies Paystack signature ✅
   - Parses webhook payload ✅
   ↓
4. PaymentServiceImpl.processWebhook()
   - Logs webhook entry ✅
   - Checks event type (charge.success, charge.failed) ✅
   ↓
5. handleSuccess() OR handleFailure()
   - Updates payment status ✅
   - Creates notification ✅
   - Logs transaction ✅
   ↓
6. Returns 200 OK to Paystack ✅
```

### Test Webhook Locally (Before Full Payment)

```bash
# Paystack test webhook (copy from dashboard)
curl -X POST https://api.paystack.co/webhooks/test \
  -H "Authorization: Bearer sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58" \
  -H "Content-Type: application/json" \
  -d '{
    "event": "charge.success",
    "data": {
      "reference": "PAY_550e8400-e29b-41d4-a716-446655440000_1712145893",
      "status": "success"
    }
  }'
```

### Check Webhook Logs in Render

```
Render Dashboard → Logs
Look for:
- "Received Paystack webhook"
- "Webhook signature verified"
- "Processing webhook event: charge.success"
- "Payment marked as SUCCESS"
```

---

## 📧 PHASE 4: EMAIL ON PAYMENT SUCCESS

### Current Implementation

Email service is configured but **NOT YET integrated** with payment success.

### What We'll Do

Add independent email sending (won't break anything if fails):

```java
// In PaymentServiceImpl.handleSuccess()
private void handleSuccess(WebhookPayload payload) {
    // ... existing code ...
    
    // Send email INDEPENDENTLY (async, non-blocking)
    try {
        String userEmail = getEmailFromUser(payment.getUserId());
        String courseName = getCourseTitle(payment.getCourseId());
        
        emailService.sendHtmlAsync(
            userEmail,
            "Payment Received! ✅",
            buildPaymentConfirmationEmail(courseName, payment.getAmountCents())
        );
    } catch (Exception e) {
        // DO NOT FAIL payment - just log error
        log.warn("Email sending failed (non-critical)", e);
    }
}
```

### Key Points
- ✅ Email sent AFTER payment is already marked successful
- ✅ If email fails, payment is NOT affected
- ✅ Errors are logged but not thrown
- ✅ Uses async execution (non-blocking)

---

## 🚦 PHASE 5: RATE LIMITING

### What We'll Add

Protect payment endpoint from abuse:

```java
@PostMapping("/initialize")
@RateLimited(
    requests = 5,           // 5 requests
    window = "1 minute",    // per 1 minute
    key = "user_id"         // per user
)
public ResponseEntity<?> initializePayment(...) {
    // ...
}
```

### Configuration
```yaml
# application.yml
app:
  ratelimit:
    payment-init: "5/minute"    # 5 per minute per user
    webhook: "100/minute"        # 100 per minute (all sources)
    default: "100/minute"        # Apply to all others
```

---

## 📋 IMPLEMENTATION ORDER

### ✅ DONE (Don't Touch)
1. Webhook handler
2. Payment notifications
3. JWT security
4. Transaction logging

### 🔄 TODO (Next)
1. Add email sending on payment success (async, independent)
2. Add rate limiting on payment endpoints
3. Test full payment flow

---

## 🎯 COMPLETE END-TO-END TEST FLOW

```
1. GET JWT TOKEN
   curl -X POST https://axio-payment.onrender.com/api/v1/auth/generate-token

2. INITIALIZE PAYMENT (with JWT)
   curl -X POST https://axio-payment.onrender.com/api/v1/payments/initialize \
   -H "Authorization: Bearer TOKEN"
   → Get: authorizationUrl

3. USER COMPLETES PAYMENT
   Open authorizationUrl in browser
   Complete payment on Paystack checkout
   
4. PAYSTACK SENDS WEBHOOK (automatic)
   Paystack → https://axio-payment.onrender.com/api/v1/webhooks/paystack
   
5. PAYMENT MARKED SUCCESS + NOTIFICATION CREATED
   Check Render logs: "Payment marked as SUCCESS"
   
6. CHECK NOTIFICATION (with JWT)
   curl -X GET https://axio-payment.onrender.com/api/v1/notifications/user/{userId}/unread/count \
   -H "Authorization: Bearer TOKEN"
   → Should return: {"unreadCount": 1}
   
7. ✅ TEST COMPLETE
```

---

## 🆘 TROUBLESHOOTING

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Missing JWT | Generate token with `/auth/generate-token` |
| Webhook not received | Paystack URL not configured | Update https://dashboard.paystack.com/settings/webhooks |
| Notification not created | Invalid payment reference | Check Render logs for "Payment marked as SUCCESS" |
| Email not sent | SMTP not configured | Check application.yml email properties |
| Rate limit error | Too many requests | Wait 1 minute or use different user_id |

---

## ✅ Ready for Implementation?

Should I now:
1. **Add email sending** on payment success (async, safe)
2. **Add rate limiting** on payment endpoints
3. **Both** simultaneously

Which order do you prefer?

