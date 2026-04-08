# 🔐 Token Generation & Exchange Guide - Complete Troubleshooting

> A comprehensive guide to understanding and fixing JWT token generation and exchange issues between Next.js Frontend and Spring Boot Backend

---

## 📖 Table of Contents

1. [How Token Generation Works](#how-token-generation-works)
2. [Token Flow Architecture](#token-flow-architecture)
3. [Backend Token Endpoints](#backend-token-endpoints)
4. [Common Issues & Solutions](#common-issues--solutions)
5. [Step-by-Step Next.js Implementation](#step-by-step-nextjs-implementation)
6. [Postman Testing Guide](#postman-testing-guide)
7. [Final Checklist](#final-checklist)

---

## 🎯 How Token Generation Works

### **The Complete Process**

```
┌─────────────────────────────────────────────────────────────────┐
│                    STEP 1: USER LOGIN                           │
│                                                                 │
│  User enters email → Frontend captures it                       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                 STEP 2: GENERATE TOKEN                          │
│                                                                 │
│  Frontend calls: POST /api/v1/auth/generate-token              │
│  Params: userId={user_uuid}                                    │
│  Backend: Finds user with that UUID in database               │
│  Backend: Creates JWT token signed with secret key            │
│  Returns: { token, userId, email, name, expiresIn }           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                 STEP 3: STORE TOKEN                             │
│                                                                 │
│  Frontend saves to: localStorage OR httpOnly cookie            │
│  Token stays valid for: 7 days                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│            STEP 4: SEND TOKEN WITH REQUESTS                     │
│                                                                 │
│  All API requests include:                                      │
│  Headers: {                                                     │
│    "Authorization": "Bearer {token}",                           │
│    "Content-Type": "application/json"                           │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

### **Key Points**

✅ **Token is NOT sent by default** - You MUST send it in the Authorization header  
✅ **Token contains user info** - userId, email, name, role (if set)  
✅ **Token is signed** - Backend can verify it hasn't been tampered with  
✅ **Token expires** - After 7 days, you need to generate a new one  
✅ **Token is for authenticated requests** - Optional for some endpoints (like initialize payment)  

---

## 🔄 Token Flow Architecture

### **Complete Request/Response Flow**

```
FRONTEND (Next.js)                          BACKEND (Spring Boot)
═════════════════════════════════════════════════════════════════════

User in App
    │
    ├─→ User Logs In (enters email)
    │
    ├─→ generateToken(email)
    │       │
    │       ├─→ POST /api/v1/auth/generate-token?userId=30f9e5f3-27f0-4351-ac63-8381ada6e6ce
    │       │   (NO Authorization header needed yet)
    │       │
    │       ├──────────────────────────────────────────────────────────→ 
    │                                                          Check email in DB
    │                                                          ↓
    │                                                     User exists?
    │                                                     YES → Generate JWT
    │                                                     NO → Return 404
    │       ●─────────────────────────────────────────────────────────── 
    │       │
    ├─→ Receive Response:
    │   {
    │     "success": true,
    │     "message": "JWT token generated successfully",
    │     "data": {
    │       "token": "eyJhbGciOiJIUzI1NiJ9.eyJuYW1l...",
    │       "userId": "5bed31bb-959c-4a24-8f76-...",
    │       "email": "user@gmail.com",
    │       "name": "John Williams",
    │       "expiresIn": "7 days"
    │     }
    │   }
    │
    ├─→ localStorage.setItem('authToken', token)
    │   localStorage.setItem('userId', userId)
    │
    ├─→ User clicks "Buy Course"
    │
    ├─→ initializePayment(userId, courseId, email)
    │       │
    │       ├─→ POST /v1/payments/initialize
    │       │   Headers: {
    │       │     "Authorization": "Bearer eyJhbGciOi...",
    │       │     "Content-Type": "application/json"
    │       │   }
    │       │   Body: {
    │       │     "userId": "5bed31bb...",
    │       │     "courseId": "xyz789...",
    │       │     "email": "user@gmail.com"
    │       │   }
    │       │
    │       ├──────────────────────────────────────────────────────────→
    │                                                   Extract Authorization header
    │                                                   ↓
    │                                                   Validate JWT token
    │                                                   ↓
    │                                                   Create payment record
    │                                                   ↓
    │                                                   Return checkout URL
    │       ├───────────────────────────────────────────────────────────
    │       │
    ├─→ Receive Response:
    │   {
    │     "success": true,
    │     "data": {
    │       "paymentId": "uuid",
    │       "reference": "unique_ref",
    │       "checkoutUrl": "https://checkout.paystack.com/...",
    │       "amount": 50000,
    │       "currency": "NGN"
    │     }
    │   }
    │
    ├─→ Redirect user: window.location.href = checkoutUrl
    │
    └─→ User completes payment on Paystack
```

---

## 📡 Backend Token Endpoints

### **Endpoint 1: Generate Token by User ID** ⭐ MOST USED

This is the recommended and WORKING way to generate tokens in your app.

```http
POST /api/v1/auth/generate-token?userId={uuid}
Content-Type: application/json
Authorization: (NOT required)

No body needed
```

**When to use:** 
- After user logs in to your app (you have their userId)
- When initializing payment
- When accessing protected endpoints

**Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/generate-token?userId=30f9e5f3-27f0-4351-ac63-8381ada6e6ce"
```

**Response (Success 200):**
```json
{
  "success": true,
  "message": "JWT token generated successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJu...",
    "userId": "30f9e5f3-27f0-4351-ac63-8381ada6e6ce",
    "email": "ellux.developer@gmail.com",
    "name": "John Williams",
    "expiresIn": "7 days"
  }
}
```

**Response (Error - User not found 404):**
```json
{
  "success": false,
  "message": "User with ID '00000000-0000-0000-0000-000000000000' not found",
  "data": null
}
```

---

### **Endpoint 2: Generate Token by Email** (Alternative)

For testing or if you have the email address.

```http
POST /api/v1/auth/generate-token-by-email?email={email}
Content-Type: application/json
Authorization: (NOT required)

No body needed
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/generate-token-by-email?email=ellux.developer@gmail.com"
```

---

### **Endpoint 3: Validate Token**

Check if a token is still valid.

```http
POST /api/v1/auth/validate-token?token={jwt_token}
Content-Type: application/json
Authorization: (NOT required)

No body needed
```

**Response (Valid):**
```json
{
  "success": true,
  "message": "Token validated",
  "data": {
    "valid": true,
    "expired": false,
    "userId": "5bed31bb-959c-4a24-8f76-30ba4c80fe87",
    "email": "ellux.developer@gmail.com",
    "role": null
  }
}
```

**Response (Expired or Invalid):**
```json
{
  "success": true,
  "message": "Token validated",
  "data": {
    "valid": false,
    "expired": true,
    "userId": null,
    "email": null,
    "role": null
  }
}
```

---

### **Endpoint 4: Generate Token with Custom Role**

For testing different user roles (admin, instructor, student).

```http
POST /api/v1/auth/generate-token-with-role?email={email}&role={role}
Content-Type: application/json
Authorization: (NOT required)

No body needed
```

**Supported Roles:** `student`, `instructor`, `admin`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/generate-token-with-role?email=ellux.developer@gmail.com&role=admin"
```

---

## 🚨 Common Issues & Solutions

### **Issue 1: "User not found" Error**

**Problem:**
```json
{
  "success": false,
  "message": "User with email 'test@gmail.com' not found",
  "data": null
}
```

**Causes:**
- ❌ Email doesn't exist in database
- ❌ Typo in email address
- ❌ User not created yet
- ❌ Case sensitivity issue (test@Gmail.com vs test@gmail.com)

**Solutions:**
1. **Verify user exists in backend database:**
   ```sql
   SELECT * FROM users WHERE id = '30f9e5f3-27f0-4351-ac63-8381ada6e6ce';
   ```

2. **If user doesn't exist, create one:**
   - Ask your backend team to create test users
   - Or create via user registration endpoint

3. **Check UUID spelling:**
   - Postman: Verify userId parameter is correct
   - Make sure it's a valid UUID format
   - Check for spaces or typos

4. **Try the working endpoint:**
   ```bash
   # Use userId that you know exists
   curl -X POST "http://localhost:8080/api/v1/auth/generate-token?userId=30f9e5f3-27f0-4351-ac63-8381ada6e6ce"
   ```

---

### **Issue 2: "401 Unauthorized" on Payment Endpoint**

**Problem:**
```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

**Causes:**
- ❌ Token not sent in Authorization header
- ❌ Token is expired
- ❌ Token is malformed
- ❌ Wrong format: `Token xyz` instead of `Bearer xyz`

**Solutions:**

✅ **CORRECT Header Format:**
```javascript
headers: {
  'Authorization': 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJu...',
  'Content-Type': 'application/json'
}
```

❌ **WRONG Header Formats:**
```javascript
// Wrong 1: Missing "Bearer"
'Authorization': 'eyJhbGciOiJIUzI1NiJ9.eyJu...'

// Wrong 2: Wrong keyword
'Authorization': 'Token eyJhbGciOiJIUzI1NiJ9.eyJu...'

// Wrong 3: Extra space
'Authorization': '  Bearer eyJhbGciOiJIUzI1NiJ9.eyJu...'

// Wrong 4: Missing header entirely
// (No Authorization header sent)
```

✅ **How to verify token in header (use Postman):**
1. Copy your token
2. Go to Postman
3. Click "Authorization" tab
4. Select "Bearer Token"
5. Paste token in "Token" field
6. See it appears in Headers as: `Authorization: Bearer {token}`

---

### **Issue 3: Token Expired Error**

**Problem:**
```
Token has expired (generated 7+ days ago)
```

**Solution:**
Generate a new token:
```bash
curl -X POST "http://localhost:8080/v1/auth/generate-token-by-email?email=user@gmail.com"
```

Save it to localStorage again and use the new one.

---

### **Issue 4: "Cannot find token in request"**

**Problem:**
Payment endpoint says it can't find token (but it's optional)

**Cause:**
- Token format is wrong
- Extra characters in token
- Token has spaces

**Solution:**
```javascript
// DON'T include quotes
const token = localStorage.getItem('authToken');
// token = "eyJhbGciOiJIUzI1NiJ9..." (good)
// token = '"eyJhbGciOiJIUzI1NiJ9..."' (bad - has quotes)

// When sending, just the token:
const authHeader = `Bearer ${token}`;
// NOT: `Bearer "${token}"` 
// This would give: Bearer "eyJ..." (extra quotes)
```

---

### **Issue 5: CORS Error when calling token endpoint**

**Problem:**
```
Access to XMLHttpRequest at 'http://localhost:8080/v1/auth/...' has been blocked by CORS policy
```

**Cause:**
- Frontend and backend on different ports
- CORS not configured on backend

**Solutions:**

**Option 1: Check backend CORS config (application.yml):**
```yaml
app:
  cors:
    allowed-origins: localhost:3000,http://localhost:3000,http://localhost:5173
```

**Option 2: Configure in Next.js (for local testing):**
```javascript
// next.config.js
module.exports = {
  async rewrites() {
    return {
      fallback: [
        {
          source: '/api/:path*',
          destination: 'http://localhost:8080/v1/:path*'
        }
      ]
    }
  }
}
```

Then use `/api/auth/generate-token-by-email` instead of full URL.

---

## 📱 Step-by-Step Next.js Implementation

### **Step 1: Create Environment Variables**

Create `.env.local` in your Next.js project root:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_BASE_URL_PROD=https://axio-payment.onrender.com
```

**Important:** 
- `NEXT_PUBLIC_` prefix = exposed to browser
- Use this for non-sensitive config like API endpoint
- Never put secrets here!

---

### **Step 2: Create API Client**

Create `lib/api-client.ts`:

```typescript
/**
 * API Client for payment service
 * Handles token transmission and request formatting
 */

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export class PaymentApiError extends Error {
  constructor(
    public status: number,
    public message: string
  ) {
    super(message);
  }
}

/**
 * Extract token from localStorage
 */
function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('authToken');
}

/**
 * Build Authorization header
 */
function getAuthHeader(): Record<string, string> {
  const token = getAuthToken();
  if (!token) return {};
  
  return {
    'Authorization': `Bearer ${token}`
  };
}

/**
 * Main API request function
 */
async function apiRequest<T = any>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...getAuthHeader(),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new PaymentApiError(
      response.status,
      data.message || 'API request failed'
    );
  }

  return data;
}

/**
 * Export convenience methods
 */
export const api = {
  get: <T = any>(endpoint: string) =>
    apiRequest<T>(endpoint, { method: 'GET' }),

  post: <T = any>(endpoint: string, body: any) =>
    apiRequest<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  put: <T = any>(endpoint: string, body: any) =>
    apiRequest<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),

  delete: <T = any>(endpoint: string) =>
    apiRequest<T>(endpoint, { method: 'DELETE' }),
};

export default apiRequest;
```

---

### **Step 3: Create the usePaymentAuth Hook**

Create `hooks/usePaymentAuth.ts`:

```typescript
/**
 * Hook for generating and managing JWT token
 * 
 * Usage in component:
 * const { token, userId, email, loading, error, generateToken } = usePaymentAuth();
 * 
 * await generateToken('user@gmail.com');
 */

'use client';

import { useState, useCallback, useEffect } from 'react';
import { api } from '@/lib/api-client';

interface TokenData {
  token: string;
  userId: string;
  email: string;
  name: string;
  expiresIn: string;
}

export function usePaymentAuth() {
  const [token, setToken] = useState<string>('');
  const [userId, setUserId] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [name, setName] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  // ===========================
  // Generate Token
  // ===========================
  const generateToken = useCallback(async (userEmail: string) => {
    if (!userEmail || !userEmail.includes('@')) {
      setError('Invalid email address');
      return false;
    }

    setLoading(true);
    setError('');

    try {
      // Call backend to generate token
      const response = await api.post('/v1/auth/generate-token-by-email?email=' + userEmail, {});

      if (!response.success) {
        throw new Error(response.message || 'Failed to generate token');
      }

      const data = response.data as TokenData;

      // Save to localStorage
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);
      localStorage.setItem('tokenExpires', new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString());

      // Update state
      setToken(data.token);
      setUserId(data.userId);
      setEmail(data.email);
      setName(data.name);

      console.log('✅ Token generated successfully:', {
        email: data.email,
        userId: data.userId,
        expiresIn: data.expiresIn
      });

      return true;
    } catch (err: any) {
      const errorMsg = err.message || 'Failed to generate token';
      setError(errorMsg);
      console.error('❌ Token generation failed:', errorMsg);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  // ===========================
  // Load token from localStorage on mount
  // ===========================
  useEffect(() => {
    const savedToken = localStorage.getItem('authToken');
    const savedUserId = localStorage.getItem('userId');
    const savedEmail = localStorage.getItem('userEmail');
    const savedName = localStorage.getItem('userName');

    if (savedToken && savedUserId) {
      setToken(savedToken);
      setUserId(savedUserId);
      setEmail(savedEmail || '');
      setName(savedName || '');
    }
  }, []);

  // ===========================
  // Clear token
  // ===========================
  const clearToken = useCallback(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
    localStorage.removeItem('tokenExpires');

    setToken('');
    setUserId('');
    setEmail('');
    setName('');
  }, []);

  // ===========================
  // Check if token exists
  // ===========================
  const hasToken = !!token;

  return {
    token,
    userId,
    email,
    name,
    loading,
    error,
    hasToken,
    generateToken,
    clearToken,
  };
}
```

---

### **Step 4: Use in Login Component**

Create `components/LoginForm.tsx`:

```typescript
'use client';

import { useState } from 'react';
import { usePaymentAuth } from '@/hooks/usePaymentAuth';
import { useRouter } from 'next/navigation';

export function LoginForm() {
  const [email, setEmail] = useState('');
  const { generateToken, loading, error } = usePaymentAuth();
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    console.log('🔐 Attempting to generate token for:', email);

    const success = await generateToken(email);

    if (success) {
      console.log('✅ Login successful! Redirecting to dashboard...');
      setTimeout(() => {
        router.push('/dashboard');
      }, 1000);
    } else {
      console.log('❌ Login failed');
    }
  };

  return (
    <form onSubmit={handleLogin} className="space-y-4 max-w-md">
      <div>
        <label className="block text-sm font-medium">Email</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="user@example.com"
          required
          className="w-full px-4 py-2 border rounded-lg"
        />
      </div>

      {error && (
        <div className="p-3 bg-red-100 text-red-700 rounded-lg">
          ❌ {error}
        </div>
      )}

      <button
        type="submit"
        disabled={loading}
        className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg disabled:opacity-50"
      >
        {loading ? 'Generating Token...' : 'Login'}
      </button>

      <div className="text-xs text-gray-600 space-y-1">
        <p>📝 Valid emails:</p>
        <ul className="list-disc list-inside">
          <li>ellux.developer@gmail.com</li>
          <li>test@example.com (must exist in DB)</li>
        </ul>
      </div>
    </form>
  );
}
```

---

### **Step 5: Use in Payment Component**

Create `components/CourseCard.tsx`:

```typescript
'use client';

import { useState } from 'react';
import { usePaymentAuth } from '@/hooks/usePaymentAuth';
import { api } from '@/lib/api-client';

interface CourseCardProps {
  courseId: string;
  courseName: string;
  coursePrice: number; // in cents
  courseImage: string;
}

export function CourseCard({
  courseId,
  courseName,
  coursePrice,
  courseImage,
}: CourseCardProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { token, userId, email, hasToken } = usePaymentAuth();

  const handlePurchase = async () => {
    // ❌ ERROR: User doesn't have token
    if (!hasToken) {
      setError('❌ Please login first to purchase courses');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // ✅ Initialize payment with token in Authorization header
      const response = await api.post('/v1/payments/initialize', {
        userId,
        courseId,
        email,
      });

      if (!response.success) {
        throw new Error(response.message || 'Payment initialization failed');
      }

      const { checkoutUrl } = response.data;

      // Redirect to Paystack
      console.log('💳 Redirecting to Paystack checkout...');
      window.location.href = checkoutUrl;

    } catch (err: any) {
      const errorMsg = err.message || 'Payment initialization failed';
      setError(`❌ ${errorMsg}`);
      console.error('Payment error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white shadow-lg rounded-xl overflow-hidden">
      <img
        src={courseImage}
        alt={courseName}
        className="w-full h-48 object-cover"
      />

      <div className="p-4 space-y-4">
        <h3 className="text-lg font-bold">{courseName}</h3>

        <p className="text-2xl font-bold text-green-600">
          ₦{(coursePrice / 100).toLocaleString()}
        </p>

        {error && (
          <div className="p-2 bg-red-100 text-red-700 rounded text-sm">
            {error}
          </div>
        )}

        <button
          onClick={handlePurchase}
          disabled={!hasToken || loading}
          className={`w-full py-2 rounded-lg font-semibold transition
            ${!hasToken
              ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
              : loading
              ? 'bg-blue-400 text-white'
              : 'bg-blue-600 hover:bg-blue-700 text-white'
            }`}
        >
          {!hasToken
            ? '🔒 Login to Purchase'
            : loading
            ? '⏳ Processing...'
            : '💳 Buy Now'
          }
        </button>
      </div>
    </div>
  );
}
```

---

## 🧪 Postman Testing Guide

⚠️ **IMPORTANT: Actual Endpoints Use `/api/v1/` NOT `/v1/`**

Based on your working Postman request, the correct base path is `/api/v1/` for all endpoints.

### **Test 1: Generate Token by User ID** (WORKING METHOD)

1. **Open Postman**
2. **Create new request:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/auth/generate-token?userId=30f9e5f3-27f0-4351-ac63-8381ada6e6ce`
3. **Headers:**
   ```
   Content-Type: application/json
   ```
4. **Body:** (leave empty, just send POST)
5. **Send** → You should get token in response ✅

### **Test 2: Copy Token**

```
From response:
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJu...",
    ...
  }
}

Copy the entire token value (including eyJ... and everything after)
```

### **Test 3: Initialize Payment with Token**

1. **Create new request:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/payments/initialize`

2. **Headers:**
   ```
   Content-Type: application/json
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJu...
   ```
   
   (Paste your token after "Bearer ")

3. **Body:**
   ```json
   {
     "userId": "5bed31bb-959c-4a24-8f76-30ba4c80fe87",
     "courseId": "some-course-id-uuid",
     "email": "ellux.developer@gmail.com"
   }
   ```

4. **Send** → Should succeed (Status 201)

### **Test 4: Verify Token is Valid**

1. **Create new request:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/auth/validate-token?token=eyJhbGciOiJIUzI1NiJ9.eyJu...`

2. **Headers:**
   ```
   Content-Type: application/json
   ```

3. **Send** → Should return `"valid": true`

---

## ✅ Final Checklist

Use this checklist to verify your token setup is correct:

### **Backend Checks**

- [ ] Spring Boot running on `http://localhost:8080`
- [ ] PostgreSQL database has users table with test data
- [ ] JWT_SECRET environment variable is set and >32 characters
- [ ] CORS is configured to allow your Next.js frontend
- [ ] Test user exists: `ellux.developer@gmail.com` or similar

### **Next.js Setup**

- [ ] `.env.local` has `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`
- [ ] `lib/api-client.ts` file created (handles Authorization header)
- [ ] `hooks/usePaymentAuth.ts` file created (generates token)
- [ ] `components/LoginForm.tsx` created (calls generateToken)
- [ ] localStorage is being used to store token

### **Token Generation Flow**

- [ ] Can call `POST /api/v1/auth/generate-token?userId={uuid}` in Postman ✅
- [ ] Get token in response with userId and email
- [ ] Token is stored in localStorage after login
- [ ] Token appears in localStorage: `localStorage.getItem('authToken')`

### **Token Usage in Requests**

- [ ] All API requests include Authorization header: `Bearer {token}`
- [ ] Header format is correct (no extra spaces or quotes)
- [ ] Payment initialization works with token

### **Error Scenarios**

- [ ] User not found → Get clear error message
- [ ] Invalid token → Can generate new one
- [ ] Token expired (7 days) → Can regenerate
- [ ] Missing Authorization header → Error message

### **Testing Steps**

1. [ ] Start backend: `mvn spring-boot:run`
2. [ ] Start frontend: `npm run dev`
3. [ ] Login with email: `ellux.developer@gmail.com`
4. [ ] Verify token in browser console: `console.log(localStorage.getItem('authToken'))`
5. [ ] Click "Buy Course" button
6. [ ] Verify Authorization header is sent in Network tab
7. [ ] See payment initialization succeeds
8. [ ] Redirected to Paystack checkout

---

## 🆘 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| "User not found" | Verify email exists in database, check spelling |
| "401 Unauthorized" | Make sure token is in Authorization header as "Bearer {token}" |
| CORS error | Add frontend URL to ALLOWED_ORIGINS in backend config |
| Token not saving | Check localStorage is not disabled, check browser console |
| Payment fails | Verify token is still valid (not expired), generate new one |
| Cannot see Authorization header in Postman | Sometimes you need to scroll to see all headers |

---

## 📞 Quick Contact Info

**If token generation still not working:**

1. Check console logs in browser (press F12)
2. Check Postman request/response
3. Verify email exists in database:
   ```sql
   SELECT * FROM users WHERE email = 'your-email@gmail.com';
   ```
4. Verify backend running: `http://localhost:8080/health`
5. Make sure CORS allowed: Check `application.yml` for allowed-origins

---

**Now your Next.js app CAN generate tokens and send them with API requests! 🎉**

Last updated: April 6, 2026
