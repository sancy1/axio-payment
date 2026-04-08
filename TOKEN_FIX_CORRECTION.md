# ⚠️ IMPORTANT CORRECTION - Token Generation Endpoints

## The Issue

My previous guide had **INCORRECT endpoints**. Your actual working endpoint is different.

---

## ✅ CORRECT Endpoints (Use These!)

### **1. Generate Token by User ID** ⭐ PRIMARY METHOD

```http
POST /api/v1/auth/generate-token?userId={uuid}
Content-Type: application/json
```

**Example (This is what works):**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/generate-token?userId=30f9e5f3-27f0-4351-ac63-8381ada6e6ce"
```

**Success Response:**
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

---

### **2. Generate Token by Email** (Alternative)

```http
POST /api/v1/auth/generate-token-by-email?email={email}
Content-Type: application/json
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/generate-token-by-email?email=ellux.developer@gmail.com"
```

---

### **3. Validate Token**

```http
POST /api/v1/auth/validate-token?token={jwt_token}
Content-Type: application/json
```

---

### **4. Generate Token with Role**

```http
POST /api/v1/auth/generate-token-with-role?email={email}&role={role}
Content-Type: application/json
```

---

## ❌ WRONG (Don't Use These!)

```http
# WRONG - Missing /api
POST /v1/auth/generate-token-by-email?email=...

# WRONG - Wrong path prefix
POST /v1/payments/initialize

# WRONG - Wrong structure
POST /auth/generate-token?userId=...
```

---

## 🎯 The Key Difference

| What I told you | What actually works |
|-----------------|-------------------|
| `/v1/auth/generate-token-by-email` | `/api/v1/auth/generate-token` |
| Parameter: `email` | Parameter: `userId` |

---

## 📱 Updated Next.js Implementation

### **Step 1: Update API Client**

Create/update `lib/api-client.ts`:

```typescript
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('authToken');
}

function getAuthHeader(): Record<string, string> {
  const token = getAuthToken();
  if (!token) return {};
  return { 'Authorization': `Bearer ${token}` };
}

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
    throw new Error(data.message || 'API request failed');
  }

  return data;
}

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
};

export default apiRequest;
```

### **Step 2: Update usePaymentAuth Hook**

Create/update `hooks/usePaymentAuth.ts`:

```typescript
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

  // ✅ CORRECT: Generate token using userId
  const generateToken = useCallback(async (userIdToUse: string) => {
    if (!userIdToUse) {
      setError('Invalid user ID');
      return false;
    }

    setLoading(true);
    setError('');

    try {
      // ✅ Use /api/v1/ path with userId parameter
      const response = await api.post(
        `/api/v1/auth/generate-token?userId=${userIdToUse}`,
        {}
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to generate token');
      }

      const data = response.data as TokenData;

      // Save to localStorage
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);

      setToken(data.token);
      setUserId(data.userId);
      setEmail(data.email);
      setName(data.name);

      console.log('✅ Token generated:', {
        userId: data.userId,
        email: data.email,
        expiresIn: data.expiresIn
      });

      return true;
    } catch (err: any) {
      const errorMsg = err.message || 'Token generation failed';
      setError(errorMsg);
      console.error('❌ Error:', errorMsg);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  // Load token from localStorage on mount
  useEffect(() => {
    const savedToken = localStorage.getItem('authToken');
    const savedUserId = localStorage.getItem('userId');

    if (savedToken && savedUserId) {
      setToken(savedToken);
      setUserId(savedUserId);
      setEmail(localStorage.getItem('userEmail') || '');
      setName(localStorage.getItem('userName') || '');
    }
  }, []);

  const clearToken = useCallback(() => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');

    setToken('');
    setUserId('');
    setEmail('');
    setName('');
  }, []);

  return {
    token,
    userId,
    email,
    name,
    loading,
    error,
    hasToken: !!token,
    generateToken,
    clearToken,
  };
}
```

---

## 🧪 Postman Quick Test

### **Generate Token**
```
Method: POST
URL: http://localhost:8080/api/v1/auth/generate-token?userId=30f9e5f3-27f0-4351-ac63-8381ada6e6ce
Headers:
  Content-Type: application/json
Body: (empty)
```

**Expected:** Token in response ✅

### **Initialize Payment**
```
Method: POST
URL: http://localhost:8080/api/v1/payments/initialize
Headers:
  Content-Type: application/json
  Authorization: Bearer {your_token_here}
Body: {
  "userId": "30f9e5f3-27f0-4351-ac63-8381ada6e6ce",
  "courseId": "some-uuid",
  "email": "ellux.developer@gmail.com"
}
```

**Expected:** Payment reference and checkout URL ✅

---

## 📋 Quick Reference

| Endpoint | Method | Path | Parameter | Works? |
|----------|--------|------|-----------|--------|
| Generate Token | POST | `/api/v1/auth/generate-token` | `userId` | ✅ YES |
| Generate Token | POST | `/api/v1/auth/generate-token-by-email` | `email` | ✅ YES |
| Validate Token | POST | `/api/v1/auth/validate-token` | `token` | ✅ YES |
| Initialize Payment | POST | `/api/v1/payments/initialize` | body | ✅ YES |

**Important:** All paths start with `/api/v1/` not `/v1/`

---

## Summary

✅ **Use:** `POST /api/v1/auth/generate-token?userId={uuid}`  
❌ **Don't use:** `POST /v1/auth/generate-token-by-email?email=...`

You already found this working in Postman! Now make sure your Next.js code uses these correct endpoints.

---

**Updated:** April 6, 2026
