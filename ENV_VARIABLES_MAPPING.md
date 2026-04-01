# 🔑 Environment Variables: .env.local → GitHub → Render Mapping

**⚠️ CRITICAL: All values below come from your `.env.local` file**

---

## Your .env.local Values

```
# Database Configuration
DATABASE_URL=jdbc:postgresql://ep-jolly-cell-ai33uxot-pooler.c-4.us-east-1.aws.neon.tech:5432/axio_prod?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_U8q4DIVoXdpL

# Paystack Configuration
PAYSTACK_SECRET_KEY=sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
PAYSTACK_PUBLIC_KEY=pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
PAYSTACK_BASE_URL=https://api.paystack.co
PAYSTACK_CALLBACK_URL=https://axio-prod-dev.onrender.com/payment-success
PAYSTACK_WEBHOOK_URL=http://localhost:8080/api/v1/payments/webhook
WEBHOOK_SECRET=AxioQuan_Secure_2026_!_99

# Email Configuration (Gmail SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=diolux.inc@gmail.com
SMTP_PASSWORD=iueezyshzmptfhjt
SMTP_FROM=AxioQuan <diolux.inc@gmail.com>

# Application Configuration
SERVER_PORT=8080
SERVER_CONTEXT_PATH=/api
FRONTEND_URL=http://localhost:3000
LOG_LEVEL=DEBUG
```

---

## Mapping: Where Each Value Goes

### 1️⃣ DATABASE CONFIGURATION

**Source (from .env.local):**
```
DATABASE_URL=jdbc:postgresql://ep-jolly-cell-ai33uxot-pooler.c-4.us-east-1.aws.neon.tech:5432/axio_prod?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_U8q4DIVoXdpL
```

**→ GitHub Secrets (for CI/CD):**
```
Secret Name: DB_PASSWORD
Secret Value: npg_U8q4DIVoXdpL

(USERNAME can be in code, ONLY password as secret)
```

**→ Render Environment Variables:**
```
DATABASE_URL = jdbc:postgresql://ep-jolly-cell-ai33uxot-pooler.c-4.us-east-1.aws.neon.tech:5432/axio_prod?sslmode=require
DB_USERNAME = neondb_owner
DB_PASSWORD = npg_U8q4DIVoXdpL
```

**Why?**
- Neon (your cloud database) requires full connection string
- Password must be secret on GitHub
- Both needed at runtime on Render

---

### 2️⃣ PAYSTACK CONFIGURATION

**Source (from .env.local):**
```
PAYSTACK_SECRET_KEY=sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
PAYSTACK_PUBLIC_KEY=pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
PAYSTACK_BASE_URL=https://api.paystack.co
PAYSTACK_CALLBACK_URL=https://axio-prod-dev.onrender.com/payment-success
PAYSTACK_WEBHOOK_URL=http://localhost:8080/api/v1/payments/webhook
WEBHOOK_SECRET=AxioQuan_Secure_2026_!_99
```

**→ GitHub Secrets:**
```
Secret Name: PAYSTACK_SECRET_KEY
Secret Value: sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58

Secret Name: PAYSTACK_PUBLIC_KEY
Secret Value: pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae

Secret Name: WEBHOOK_SECRET
Secret Value: AxioQuan_Secure_2026_!_99
```

**→ Render Environment Variables:**
```
PAYSTACK_SECRET_KEY = sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
PAYSTACK_PUBLIC_KEY = pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
PAYSTACK_BASE_URL = https://api.paystack.co
PAYSTACK_CALLBACK_URL = https://payment-service-xxxx.onrender.com/api/v1/webhooks/paystack
PAYSTACK_WEBHOOK_URL = https://payment-service-xxxx.onrender.com/api/v1/webhooks/paystack
WEBHOOK_SECRET = AxioQuan_Secure_2026_!_99
```

**IMPORTANT:** Update these after Render creates your service:
```
# BEFORE: (local dev)
PAYSTACK_CALLBACK_URL=https://axio-prod-dev.onrender.com/payment-success
PAYSTACK_WEBHOOK_URL=http://localhost:8080/api/v1/payments/webhook

# AFTER: (Render production)
PAYSTACK_CALLBACK_URL=https://payment-service-ac123xyz.onrender.com/api/v1/webhooks/paystack
PAYSTACK_WEBHOOK_URL=https://payment-service-ac123xyz.onrender.com/api/v1/webhooks/paystack
```

**Why?**
- Keys are sensitive (credentials to Paystack)
- Callbacks must point to your actual Render URL
- You update in Render directly (we'll show how)

---

### 3️⃣ EMAIL CONFIGURATION (GMAIL SMTP)

**Source (from .env.local):**
```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=diolux.inc@gmail.com
SMTP_PASSWORD=iueezyshzmptfhjt
SMTP_FROM=AxioQuan <diolux.inc@gmail.com>
```

**→ GitHub Secrets:**
```
Secret Name: SMTP_USER
Secret Value: diolux.inc@gmail.com

Secret Name: SMTP_PASSWORD
Secret Value: iueezyshzmptfhjt
```

**→ Render Environment Variables:**
```
SMTP_HOST = smtp.gmail.com
SMTP_PORT = 587
SMTP_USER = diolux.inc@gmail.com
SMTP_PASSWORD = iueezyshzmptfhjt
SMTP_FROM = AxioQuan <diolux.inc@gmail.com>
```

**Why?**
- Email credentials are sensitive
- Password required at runtime
- Host and port are constants (Gmail SMTP)

---

### 4️⃣ APPLICATION CONFIGURATION

**Source (from .env.local):**
```
SERVER_PORT=8080
SERVER_CONTEXT_PATH=/api
FRONTEND_URL=http://localhost:3000
LOG_LEVEL=DEBUG
```

**→ GitHub Secrets:**
```
(NONE - these are not sensitive)
```

**→ Render Environment Variables:**
```
SERVER_PORT = 8080
SERVER_CONTEXT_PATH = /api
FRONTEND_URL = https://your-frontend-url.com  (update if needed)
LOG_LEVEL = INFO  (use INFO for production, DEBUG for dev)
```

**Why?**
- Not sensitive, just configuration
- Log level: DEBUG for dev, INFO for production
- Frontend URL: Update based on your frontend deployment

---

## Quick Copy-Paste Tables

### For GitHub Secrets (8 total)

Copy these EXACT names and values from .env.local:

```
PAYSTACK_SECRET_KEY = sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
PAYSTACK_PUBLIC_KEY = pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
DB_USERNAME = neondb_owner
DB_PASSWORD = npg_U8q4DIVoXdpL
SMTP_USER = diolux.inc@gmail.com
SMTP_PASSWORD = iueezyshzmptfhjt
WEBHOOK_SECRET = AxioQuan_Secure_2026_!_99
RENDER_DEPLOY_HOOK_URL = (get from Render later)
```

### For Render Environment Variables (13 total)

```
DATABASE_URL = jdbc:postgresql://ep-jolly-cell-ai33uxot-pooler.c-4.us-east-1.aws.neon.tech:5432/axio_prod?sslmode=require
DB_USERNAME = neondb_owner
DB_PASSWORD = npg_U8q4DIVoXdpL
PAYSTACK_SECRET_KEY = sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
PAYSTACK_PUBLIC_KEY = pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
PAYSTACK_BASE_URL = https://api.paystack.co
PAYSTACK_CALLBACK_URL = https://payment-service-xxx.onrender.com/api/v1/webhooks/paystack
PAYSTACK_WEBHOOK_URL = https://payment-service-xxx.onrender.com/api/v1/webhooks/paystack
SMTP_HOST = smtp.gmail.com
SMTP_PORT = 587
SMTP_USER = diolux.inc@gmail.com
SMTP_PASSWORD = iueezyshzmptfhjt
SMTP_FROM = AxioQuan <diolux.inc@gmail.com>
WEBHOOK_SECRET = AxioQuan_Secure_2026_!_99
SERVER_PORT = 8080
SERVER_CONTEXT_PATH = /api
FRONTEND_URL = http://localhost:3000
LOG_LEVEL = INFO
```

---

## Sensitive vs Non-Sensitive

### 🔒 SENSITIVE (Must be GitHub Secrets)
- `PAYSTACK_SECRET_KEY` - API access
- `PAYSTACK_PUBLIC_KEY` - API access
- `DB_PASSWORD` - Database access
- `SMTP_PASSWORD` - Email access
- `WEBHOOK_SECRET` - Signature verification

### 📄 NON-SENSITIVE (Can be public or in Render only)
- `DATABASE_URL` - Connection string
- `DB_USERNAME` - Username only
- `PAYSTACK_BASE_URL` - Public API endpoint
- `SMTP_HOST`, `SMTP_PORT` - Gmail config
- `SMTP_USER` - Email address
- `SMTP_FROM` - Sender display
- `SERVER_PORT`, `SERVER_CONTEXT_PATH` - App config
- `LOG_LEVEL` - Logging config

---

## After Render Deployment

Once your service is deployed on Render:

1. **Get your Render URL:**
   ```
   https://dashboard.render.com → Your Service → URL
   Example: https://payment-service-abc123xyz.onrender.com
   ```

2. **Update Paystack Callbacks in Render:**
   - Go to Render → Environment → Edit
   - Update these values:
   ```
   PAYSTACK_CALLBACK_URL = https://payment-service-abc123xyz.onrender.com/api/v1/webhooks/paystack
   PAYSTACK_WEBHOOK_URL = https://payment-service-abc123xyz.onrender.com/api/v1/webhooks/paystack
   ```

3. **Update Paystack Dashboard:**
   - Log in to https://dashboard.paystack.com
   - Settings → Webhooks
   - URL: `https://payment-service-abc123xyz.onrender.com/api/v1/webhooks/paystack`

---

## Testing Values Match

### Command to verify:
```bash
# After deployment, SSH into Render (or check logs)
# Look for these lines to confirm vars are loaded:

"Database connection established to: ep-jolly-cell-ai33uxot-pooler.c-4.us-east-1.aws.neon.tech"
"Email service configured with SMTP: smtp.gmail.com:587"
"Paystack API client initialized with keys starting with: sk_test"
```

---

## Summary

| Platform | Type | Count | Includes Sensitive |
|----------|------|-------|-------------------|
| `.env.local` | Local file | All | ✅ Yes (not pushed) |
| **GitHub Secrets** | Repository secrets | 8 | ✅ Yes (encrypted) |
| **Render Env Vars** | Service config | 13 | ✅ Yes (encrypted) |

All values come from your `.env.local` - just copy them to the right place!

