# Payment Service - Next.js Integration Guide

**Purpose:** Complete step-by-step guide for consuming Payment Service API endpoints from Next.js  
**Created:** April 3, 2026  
**Target Version:** Next.js 14+  

---

## 📋 Table of Contents

1. [Setup & Configuration](#setup--configuration)
2. [Endpoint Consumption Order](#endpoint-consumption-order)
3. [Step-by-Step Integration](#step-by-step-integration)
4. [Code Examples](#code-examples)
5. [Error Handling](#error-handling)
6. [Best Practices](#best-practices)

---

## Setup & Configuration

### 1. Environment Variables (.env.local)

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_PAYSTACK_PUBLIC_KEY=your_paystack_public_key

# Or production
# NEXT_PUBLIC_API_BASE_URL=https://axio-payment.onrender.com
```

### 2. Create API Client (lib/api.ts)

```typescript
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL;

export const apiClient = {
  async request(endpoint: string, options: RequestInit = {}) {
    const url = `${API_BASE}${endpoint}`;
    const token = localStorage.getItem('authToken');

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API Error');
    }

    return response.json();
  },

  get(endpoint: string) {
    return this.request(endpoint, { method: 'GET' });
  },

  post(endpoint: string, body: any) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  },

  put(endpoint: string, body: any) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body),
    });
  },
};
```

---

## Endpoint Consumption Order

### **The Complete Payment Flow**

```
START
  ↓
1. Generate JWT Token (Auth)
  ↓
2. Check Purchase Status (Avoid duplicate purchases)
  ↓
3. Initialize Payment (Create payment record)
  ↓
4. Redirect to Paystack Checkout
  ↓
5. User Completes Payment on Paystack
  ↓
6. Verify Payment (Database updates, enrollment created)
  ↓
7. Get Enrollment Details (Confirm enrollment)
  ↓
8. Check Course Access (Verify user has access)
  ↓
9. Get User's Enrollments (Dashboard - show all courses)
  ↓
10. Update Progress (As user learns)
  ↓
11. Get Transaction History (Show user their payments)
  ↓
END
```

---

## Step-by-Step Integration

### **Step 1: Generate JWT Token on User Login**

**When:** User logs in to your Next.js app  
**Why:** Needed for accessing protected endpoints  
**Endpoint:** `GET /v1/auth/generate-token-by-email?email={email}`  

```typescript
// hooks/usePaymentAuth.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function usePaymentAuth(email: string | null) {
  const [token, setToken] = useState<string>('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!email) return;

    const generateToken = async () => {
      setLoading(true);
      try {
        const response = await apiClient.get(
          `/v1/auth/generate-token-by-email?email=${email}`
        );
        
        // Store token in localStorage
        localStorage.setItem('authToken', response.data.token);
        localStorage.setItem('userId', response.data.userId);
        setToken(response.data.token);
      } catch (error) {
        console.error('Failed to generate token:', error);
      } finally {
        setLoading(false);
      }
    };

    generateToken();
  }, [email]);

  return { token, loading };
}
```

**Usage in Component:**

```typescript
// pages/dashboard.tsx
import { usePaymentAuth } from '@/hooks/usePaymentAuth';

export default function Dashboard() {
  const { user } = useAuth(); // Your existing auth hook
  const { token, loading } = usePaymentAuth(user?.email);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>Welcome {user?.name}</h1>
      {/* Token is now stored in localStorage */}
    </div>
  );
}
```

---

### **Step 2: Check If User Already Purchased Course**

**When:** Before showing "Enroll" button  
**Why:** Prevent duplicate purchases & show correct button ("Enroll" vs "Already Purchased")  
**Endpoint:** `GET /api/v1/enrollments/user/{userId}/course/{courseId}/status`  

```typescript
// hooks/usePurchaseStatus.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function usePurchaseStatus(userId: string, courseId: string) {
  const [isPurchased, setIsPurchased] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId || !courseId) return;

    const checkStatus = async () => {
      try {
        // This endpoint returns a boolean (true/false)
        const response = await apiClient.get(
          `/v1/payments/user/${userId}/course/${courseId}/status`
        );
        setIsPurchased(response.data);
      } catch (error) {
        console.error('Failed to check purchase status:', error);
      } finally {
        setLoading(false);
      }
    };

    checkStatus();
  }, [userId, courseId]);

  return { isPurchased, loading };
}
```

**Usage in Component:**

```typescript
// components/CourseCard.tsx
import { usePurchaseStatus } from '@/hooks/usePurchaseStatus';
import { useRouter } from 'next/navigation';

export function CourseCard({ course, userId }: Props) {
  const router = useRouter();
  const { isPurchased, loading } = usePurchaseStatus(userId, course.id);

  if (loading) return <div>Checking access...</div>;

  return (
    <div className="course-card">
      <h3>{course.title}</h3>
      <p>{course.description}</p>

      {isPurchased ? (
        <button onClick={() => router.push(`/course/${course.id}`)}>
          Continue Learning
        </button>
      ) : (
        <button onClick={() => router.push(`/checkout/${course.id}`)}>
          Enroll Now - ₦{course.price}
        </button>
      )}
    </div>
  );
}
```

---

### **Step 3: Initialize Payment**

**When:** User clicks "Enroll Now" button  
**Why:** Create payment record and get Paystack checkout URL  
**Endpoint:** `POST /v1/payments/initialize`  

```typescript
// hooks/useInitializePayment.ts
import { useState } from 'react';
import { apiClient } from '@/lib/api';

interface InitializePaymentParams {
  userId: string;
  courseId: string;
  email: string;
}

export function useInitializePayment() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const initialize = async (params: InitializePaymentParams) => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.post('/v1/payments/initialize', {
        userId: params.userId,
        courseId: params.courseId,
        email: params.email,
      });

      return {
        reference: response.data.reference,
        checkoutUrl: response.data.checkoutUrl,
        amountCents: response.data.amountCents,
        paymentId: response.data.paymentId,
      };
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { initialize, loading, error };
}
```

**Usage in Component:**

```typescript
// pages/checkout/[courseId].tsx
import { useInitializePayment } from '@/hooks/useInitializePayment';
import { useRouter } from 'next/navigation';

export default function CheckoutPage({ params }: { params: { courseId: string } }) {
  const router = useRouter();
  const { user } = useAuth();
  const { initialize, loading } = useInitializePayment();
  const [processing, setProcessing] = useState(false);

  const handlePayment = async () => {
    setProcessing(true);
    try {
      const payment = await initialize({
        userId: user?.id || '',
        courseId: params.courseId,
        email: user?.email || '',
      });

      // Store payment reference for later verification
      sessionStorage.setItem('paymentReference', payment.reference);

      // Redirect to Paystack checkout
      window.location.href = payment.checkoutUrl;
    } catch (error) {
      console.error('Payment initialization failed:', error);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="checkout">
      <h1>Complete Your Purchase</h1>
      <button onClick={handlePayment} disabled={loading || processing}>
        {processing ? 'Processing...' : 'Pay Now'}
      </button>
    </div>
  );
}
```

---

### **Step 4: Verify Payment (After User Completes Paystack)**

**When:** User redirected back from Paystack  
**Why:** Confirm payment success and trigger enrollment creation  
**Endpoint:** `GET /v1/payments/verify/{reference}`  

```typescript
// hooks/useVerifyPayment.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function useVerifyPayment(reference: string | null) {
  const [payment, setPayment] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<'pending' | 'success' | 'failed'>('pending');

  useEffect(() => {
    if (!reference) return;

    const verify = async () => {
      setLoading(true);
      try {
        const response = await apiClient.get(
          `/v1/payments/verify/${reference}`
        );

        setPayment(response.data);
        setStatus(response.data.status === 'SUCCESS' ? 'success' : 'failed');
      } catch (error) {
        console.error('Payment verification failed:', error);
        setStatus('failed');
      } finally {
        setLoading(false);
      }
    };

    verify();
  }, [reference]);

  return { payment, loading, status };
}
```

**Usage in Component:**

```typescript
// pages/payment-success.tsx
import { useVerifyPayment } from '@/hooks/useVerifyPayment';
import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

export default function PaymentSuccessPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const reference = searchParams.get('reference');
  
  const { payment, loading, status } = useVerifyPayment(reference);

  useEffect(() => {
    // Redirect to course after 3 seconds if payment successful
    if (status === 'success' && payment?.enrollmentId) {
      const timer = setTimeout(() => {
        router.push(`/course/${payment.courseId}`);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [status, payment, router]);

  if (loading) return <div>Verifying your payment...</div>;

  if (status === 'success') {
    return (
      <div className="success-page">
        <h1>✅ Payment Successful!</h1>
        <p>Amount: ₦{payment?.amountCents / 100}</p>
        <p>Enrollment ID: {payment?.enrollmentId}</p>
        <p>Redirecting to course...</p>
      </div>
    );
  }

  return (
    <div className="error-page">
      <h1>❌ Payment Failed</h1>
      <button onClick={() => router.push('/courses')}>
        Back to Courses
      </button>
    </div>
  );
}
```

---

### **Step 5: Get Enrollment Details**

**When:** When user views a course (verify they're enrolled)  
**Why:** Get enrollment status, progress, paid status  
**Endpoint:** `GET /api/v1/enrollments/user/{userId}/course/{courseId}`  

```typescript
// hooks/useEnrollment.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function useEnrollment(userId: string, courseId: string) {
  const [enrollment, setEnrollment] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId || !courseId) return;

    const fetchEnrollment = async () => {
      try {
        const response = await apiClient.get(
          `/api/v1/enrollments/user/${userId}/course/${courseId}`
        );
        setEnrollment(response.data);
      } catch (error) {
        console.error('Failed to fetch enrollment:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchEnrollment();
  }, [userId, courseId]);

  return { enrollment, loading };
}
```

**Usage in Component:**

```typescript
// pages/course/[courseId].tsx
import { useEnrollment } from '@/hooks/useEnrollment';
import { useRouter } from 'next/navigation';

export default function CoursePage({ params }: { params: { courseId: string } }) {
  const router = useRouter();
  const { user } = useAuth();
  const { enrollment, loading } = useEnrollment(user?.id || '', params.courseId);

  if (loading) return <div>Loading course...</div>;

  if (!enrollment) {
    return (
      <div>
        <p>Not enrolled in this course</p>
        <button onClick={() => router.push(`/checkout/${params.courseId}`)}>
          Enroll Now
        </button>
      </div>
    );
  }

  return (
    <div className="course-content">
      <h1>Course Name</h1>
      <div className="enrollment-info">
        <p>Status: {enrollment.status}</p>
        <p>Progress: {enrollment.progressPercentage}%</p>
        {enrollment.isPaid && <span className="badge">Lifetime Access</span>}
      </div>

      <ProgressBar value={enrollment.progressPercentage} />
      {/* Course content here */}
    </div>
  );
}
```

---

### **Step 6: Check Course Access (Gate Protection)**

**When:** Before rendering course content  
**Why:** Ensure user can access the course  
**Endpoint:** `GET /api/v1/enrollments/check-access/{userId}/{courseId}`  

```typescript
// hooks/useCourseAccess.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function useCourseAccess(userId: string, courseId: string) {
  const [hasAccess, setHasAccess] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId || !courseId) return;

    const checkAccess = async () => {
      try {
        const response = await apiClient.get(
          `/api/v1/enrollments/check-access/${userId}/${courseId}`
        );
        setHasAccess(response.data); // Returns true/false
      } catch (error) {
        console.error('Access check failed:', error);
      } finally {
        setLoading(false);
      }
    };

    checkAccess();
  }, [userId, courseId]);

  return { hasAccess, loading };
}
```

**Usage in Middleware/Route Guard:**

```typescript
// components/CourseProtectedPage.tsx
import { useCourseAccess } from '@/hooks/useCourseAccess';
import { useRouter } from 'next/navigation';

export function CourseProtectedPage({ userId, courseId, children }: Props) {
  const router = useRouter();
  const { hasAccess, loading } = useCourseAccess(userId, courseId);

  if (loading) return <div>Checking access...</div>;

  if (!hasAccess) {
    return (
      <div className="access-denied">
        <h1>Access Denied</h1>
        <p>You need to purchase this course to access it.</p>
        <button onClick={() => router.push(`/checkout/${courseId}`)}>
          Buy Now
        </button>
      </div>
    );
  }

  return children;
}
```

---

### **Step 7: Get All User Enrollments (Dashboard)**

**When:** User views their dashboard/my courses  
**Why:** Show all courses user is enrolled in with progress  
**Endpoint:** `GET /api/v1/enrollments/user/{userId}/with-progress`  

```typescript
// hooks/useUserEnrollments.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function useUserEnrollments(userId: string) {
  const [enrollments, setEnrollments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId) return;

    const fetchEnrollments = async () => {
      try {
        const response = await apiClient.get(
          `/api/v1/enrollments/user/${userId}/with-progress`
        );
        setEnrollments(response.data || []);
      } catch (error) {
        console.error('Failed to fetch enrollments:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchEnrollments();
  }, [userId]);

  return { enrollments, loading };
}
```

**Usage in Component:**

```typescript
// pages/my-courses.tsx
import { useUserEnrollments } from '@/hooks/useUserEnrollments';
import { useRouter } from 'next/navigation';

export default function MyCoursesPage() {
  const { user } = useAuth();
  const { enrollments, loading } = useUserEnrollments(user?.id || '');
  const router = useRouter();

  if (loading) return <div>Loading your courses...</div>;

  return (
    <div className="my-courses">
      <h1>My Courses ({enrollments.length})</h1>

      {enrollments.length === 0 ? (
        <div>
          <p>You haven't enrolled in any courses yet</p>
          <button onClick={() => router.push('/browse-courses')}>
            Browse Courses
          </button>
        </div>
      ) : (
        <div className="courses-grid">
          {enrollments.map((enrollment) => (
            <div key={enrollment.enrollmentId} className="course-card">
              <h3>{enrollment.courseTitle}</h3>

              {/* Progress Bar */}
              <div className="progress-wrapper">
                <div className="progress-bar">
                  <div 
                    className="progress-fill" 
                    style={{ width: `${enrollment.progressPercentage}%` }}
                  />
                </div>
                <p>{enrollment.progressPercentage.toFixed(1)}%</p>
              </div>

              {/* Course Details */}
              <p>{enrollment.completedLessons}/{enrollment.totalLessons} lessons</p>
              <p>Time spent: {enrollment.timeSpentMinutes} mins</p>

              {enrollment.isPaid && (
                <span className="badge premium">Lifetime Access ∞</span>
              )}

              <button 
                onClick={() => router.push(`/course/${enrollment.courseId}`)}
                className="btn-primary"
              >
                Continue Learning
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

---

### **Step 8: Update Progress (As User Learns)**

**When:** After user completes lesson, quiz, or assignment  
**Why:** Track learning journey  
**Endpoint:** `PUT /api/v1/enrollments/{enrollmentId}/progress`  

```typescript
// hooks/useUpdateProgress.ts
import { useState } from 'react';
import { apiClient } from '@/lib/api';

export function useUpdateProgress() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateProgress = async (
    enrollmentId: string,
    progressData: {
      progressPercentage: number;
      completedLessons: number;
      totalLessons: number;
      timeSpentMinutes: number;
      averageQuizScore?: number;
      assignmentAverage?: number;
      overallGrade?: number;
      markCompleted?: boolean;
    }
  ) => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.put(
        `/api/v1/enrollments/${enrollmentId}/progress`,
        progressData
      );
      return response;
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { updateProgress, loading, error };
}
```

**Usage in Component:**

```typescript
// components/LessonViewer.tsx
import { useUpdateProgress } from '@/hooks/useUpdateProgress';

export function LessonViewer({ enrollmentId, lesson, onComplete }: Props) {
  const { updateProgress } = useUpdateProgress();
  const [currentProgress, setCurrentProgress] = useState(0);

  const handleLessonComplete = async () => {
    const newProgress = currentProgress + 10; // 10% per lesson
    
    try {
      await updateProgress(enrollmentId, {
        progressPercentage: newProgress,
        completedLessons: Math.floor(newProgress / 10),
        totalLessons: 10,
        timeSpentMinutes: Math.floor(Date.now() - startTime) / 60000,
      });

      setCurrentProgress(newProgress);
      onComplete?.();
    } catch (error) {
      console.error('Failed to update progress:', error);
    }
  };

  return (
    <div className="lesson">
      <h2>{lesson.title}</h2>
      {/* Lesson content */}
      <button onClick={handleLessonComplete}>
        Mark as Complete
      </button>
    </div>
  );
}
```

---

### **Step 9: Get Transaction History (Payment Records)**

**When:** User views payment history page  
**Why:** Show all user's transactions  
**Endpoint:** `GET /v1/transactions/user/{userId}`  

```typescript
// hooks/useTransactionHistory.ts
import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';

export function useTransactionHistory(userId: string) {
  const [transactions, setTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<any>(null);

  useEffect(() => {
    if (!userId) return;

    const fetchTransactions = async () => {
      try {
        // Fetch transactions list
        const txnResponse = await apiClient.get(
          `/v1/transactions/user/${userId}`
        );
        setTransactions(txnResponse.data || []);

        // Fetch summary
        const summaryResponse = await apiClient.get(
          `/v1/transactions/user/${userId}/summary`
        );
        setSummary(summaryResponse.data);
      } catch (error) {
        console.error('Failed to fetch transactions:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [userId]);

  return { transactions, summary, loading };
}
```

**Usage in Component:**

```typescript
// pages/payment-history.tsx
import { useTransactionHistory } from '@/hooks/useTransactionHistory';

export default function PaymentHistoryPage() {
  const { user } = useAuth();
  const { transactions, summary, loading } = useTransactionHistory(user?.id || '');

  if (loading) return <div>Loading transactions...</div>;

  return (
    <div className="payment-history">
      <h1>Payment History</h1>

      {/* Summary Stats */}
      {summary && (
        <div className="summary-cards">
          <div className="card">
            <p>Total Purchases</p>
            <h2>{summary.totalTransactions}</h2>
          </div>
          <div className="card">
            <p>Total Spent</p>
            <h2>₦{(summary.totalAmountCents / 100).toLocaleString()}</h2>
          </div>
          <div className="card">
            <p>Successful</p>
            <h2>{summary.successfulCount}</h2>
          </div>
          <div className="card">
            <p>Failed</p>
            <h2>{summary.failedCount}</h2>
          </div>
        </div>
      )}

      {/* Transaction List */}
      <table className="transactions-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Reference</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Type</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((txn) => (
            <tr key={txn.id}>
              <td>{new Date(txn.createdAt).toLocaleDateString()}</td>
              <td>{txn.reference}</td>
              <td>₦{(txn.amountCents / 100).toFixed(2)}</td>
              <td>
                <span className={`status-${txn.status.toLowerCase()}`}>
                  {txn.status}
                </span>
              </td>
              <td>{txn.type}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## Code Examples

### Complete Integration Example (All Steps Combined)

```typescript
// pages/course-purchase-flow.tsx
import { useAuth } from '@/hooks/useAuth';
import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

// Import all hooks
import { usePaymentAuth } from '@/hooks/usePaymentAuth';
import { usePurchaseStatus } from '@/hooks/usePurchaseStatus';
import { useInitializePayment } from '@/hooks/useInitializePayment';
import { useVerifyPayment } from '@/hooks/useVerifyPayment';
import { useCourseAccess } from '@/hooks/useCourseAccess';
import { useEnrollment } from '@/hooks/useEnrollment';

export default function CoursePurchaseFlow({ params }: { params: { courseId: string } }) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user } = useAuth();

  // Step 1: Get JWT Token
  const { token } = usePaymentAuth(user?.email || null);

  // Step 2: Check if already purchased
  const { isPurchased } = usePurchaseStatus(user?.id || '', params.courseId);

  // Step 3: Initialize payment
  const { initialize } = useInitializePayment();

  // Step 4: Verify payment (if returning from Paystack)
  const paymentRef = searchParams.get('reference');
  const { payment, status } = useVerifyPayment(paymentRef);

  // Step 5: Get enrollment
  const { enrollment } = useEnrollment(user?.id || '', params.courseId);

  // Step 6: Check access
  const { hasAccess } = useCourseAccess(user?.id || '', params.courseId);

  // Payment complete - redirect to course
  useEffect(() => {
    if (status === 'success' && hasAccess) {
      router.push(`/course/${params.courseId}`);
    }
  }, [status, hasAccess]);

  // Handle purchase button click
  const handlePurchase = async () => {
    try {
      const payment = await initialize({
        userId: user?.id || '',
        courseId: params.courseId,
        email: user?.email || '',
      });
      window.location.href = payment.checkoutUrl;
    } catch (error) {
      console.error('Purchase failed:', error);
    }
  };

  if (!token) return <div>Authenticating...</div>;

  if (isPurchased && hasAccess) {
    return (
      <div>
        <h1>Course Purchased! ✅</h1>
        <button onClick={() => router.push(`/course/${params.courseId}`)}>
          Access Course
        </button>
      </div>
    );
  }

  if (status === 'success') {
    return <div>Payment verified! Redirecting...</div>;
  }

  return (
    <div className="checkout-page">
      <h1>Complete Your Purchase</h1>
      <button onClick={handlePurchase}>
        Pay Now
      </button>
    </div>
  );
}
```

---

## Error Handling

### Global Error Handler

```typescript
// lib/errorHandler.ts
export class PaymentError extends Error {
  constructor(
    public message: string,
    public statusCode?: number,
    public details?: any
  ) {
    super(message);
  }
}

export const handlePaymentError = (error: any) => {
  if (error.message === 'Failed to fetch') {
    return new PaymentError('Network error. Please check your connection.');
  }

  if (error.response?.status === 404) {
    return new PaymentError('Resource not found.');
  }

  if (error.response?.status === 401) {
    // Clear token and redirect to login
    localStorage.removeItem('authToken');
    window.location.href = '/login';
    return new PaymentError('Session expired. Please login again.');
  }

  if (error.response?.status === 403) {
    return new PaymentError('Access denied. You cannot perform this action.');
  }

  if (error.response?.status === 429) {
    return new PaymentError('Too many requests. Please wait a moment and try again.');
  }

  return new PaymentError(
    error.message || 'An unexpected error occurred. Please try again.'
  );
};
```

### Usage in Components

```typescript
try {
  await initialize(paymentData);
} catch (error) {
  const paymentError = handlePaymentError(error);
  toast.error(paymentError.message);
}
```

---

## Best Practices

### 1. **Store Token Securely**

```typescript
// Instead of localStorage, consider using cookies with httpOnly
// Using next-auth or similar solution is recommended

// For now, at least store securely:
const storeToken = (token: string) => {
  // Avoid storing sensitive data in localStorage
  // Better: use httpOnly cookies
  document.cookie = `authToken=${token}; path=/; HttpOnly; Secure; SameSite=Strict`;
};
```

### 2. **Implement Loading States**

```typescript
// Always show loading state to prevent duplicate requests
const [isSubmitting, setIsSubmitting] = useState(false);

const handlePay = async () => {
  if (isSubmitting) return; // Prevent double-click
  setIsSubmitting(true);
  try {
    await initialize(data);
  } finally {
    setIsSubmitting(false);
  }
};
```

### 3. **Validate Input**

```typescript
const validatePaymentData = (data: any) => {
  if (!data.userId || !data.courseId || !data.email) {
    throw new Error('Missing required fields');
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(data.email)) {
    throw new Error('Invalid email format');
  }

  return true;
};
```

### 4. **Handle Offline Mode**

```typescript
export function useOnline() {
  const [isOnline, setIsOnline] = useState(true);

  useEffect(() => {
    setIsOnline(navigator.onLine);

    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
}
```

### 5. **Use SWR for Data Fetching (Recommended)**

```typescript
import useSWR from 'swr';

export function useUserEnrollmentsSWR(userId: string) {
  const { data, error, isLoading } = useSWR(
    userId ? `/api/v1/enrollments/user/${userId}/with-progress` : null,
    apiClient.get,
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: true,
    }
  );

  return {
    enrollments: data?.data || [],
    loading: isLoading,
    error,
  };
}
```

### 6. **Implement Retry Logic**

```typescript
const retryAsync = async (fn: () => Promise<any>, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1))); // exponential backoff
    }
  }
};

// Usage
const payment = await retryAsync(() =>
  initialize(paymentData)
);
```

---

## Summary Flowchart

```
┌─────────────────────────────────────────────────────────────────┐
│                    NEXT.JS PAYMENT FLOW                         │
└─────────────────────────────────────────────────────────────────┘

User Login
   │
   ├─→ 1. Generate JWT Token
   │      (usePaymentAuth)
   │
User Views Course
   │
   ├─→ 2. Check Purchase Status
   │      (usePurchaseStatus) → Already Purchased? → Show "Continue"
   │
User Clicks "Enroll"
   │
   ├─→ 3. Initialize Payment
   │      (useInitializePayment) → Get checkout URL
   │
   ├─→ 4. Redirect to Paystack
   │      (window.location.href = checkoutUrl)
   │
User Completes Payment on Paystack
   │
   ├─→ Paystack redirects with ?reference=...
   │
   ├─→ 5. Verify Payment
   │      (useVerifyPayment) → Check status
   │
User Access Course
   │
   ├─→ 6. Check Access
   │      (useCourseAccess) → Gate protection
   │
   ├─→ 7. Get Enrollment
   │      (useEnrollment) → Show enrollment details
   │
User Dashboard
   │
   ├─→ 8. Get All Enrollments
   │      (useUserEnrollments) → Show all courses
   │
User Taking Course
   │
   ├─→ 9. Update Progress
   │      (useUpdateProgress) → Track lessons
   │
Payment History
   │
   └─→ 10. Fetch Transactions
          (useTransactionHistory) → Show payment records
```

---

## Next Steps

1. **Copy all hook files** from code examples above
2. **Update environment variables** in `.env.local`
3. **Test locally** with `http://localhost:8080`
4. **Implement error toast notifications** (use `react-hot-toast` or similar)
5. **Add loading skeletons** for better UX
6. **Test complete flow** in Paystack sandbox mode
7. **Deploy** to production when API is on Render

---

**Document Version:** 1.0  
**Created:** April 3, 2026  
**For:** Next.js Developers  
**Status:** Ready to implement
