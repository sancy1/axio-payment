# 📋 DEPLOYMENT QUICK CHECKLIST

Copy these exact values from your `.env.local` to the platforms indicated.

---

## ✅ STEP 1: GitHub Secrets Setup (DO THIS FIRST!)

**Location:** https://github.com/sancy1/axio-payment/settings/secrets/actions

Add exactly 8 secrets. Click "New repository secret" for each:

### Secret #1
```
Name:  PAYSTACK_SECRET_KEY
Value: sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
```

### Secret #2
```
Name:  PAYSTACK_PUBLIC_KEY
Value: pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
```

### Secret #3
```
Name:  DB_USERNAME
Value: neondb_owner
```

### Secret #4
```
Name:  DB_PASSWORD
Value: npg_U8q4DIVoXdpL
```

### Secret #5
```
Name:  SMTP_USER
Value: diolux.inc@gmail.com
```

### Secret #6
```
Name:  SMTP_PASSWORD
Value: iueezyshzmptfhjt
```

### Secret #7
```
Name:  WEBHOOK_SECRET
Value: AxioQuan_Secure_2026_!_99
```

### Secret #8 (Add LATER after Render setup)
```
Name:  RENDER_DEPLOY_HOOK_URL
Value: <Get from Render Dashboard>
```

---

## ✅ STEP 2: Render Service Setup

1. Go to https://dashboard.render.com
2. Click **"New"** → **"Web Service"**
3. Connect GitHub repo: `sancy1/axio-payment`
4. Name: `payment-service`
5. Environment: **Docker**
6. Branch: `main`
7. Dockerfile Path: `./Dockerfile` (default)

### Environment Variables in Render

Click "Add Environment Variable" for each:

```
DATABASE_URL = jdbc:postgresql://ep-jolly-cell-ai33uxot-pooler.c-4.us-east-1.aws.neon.tech:5432/axio_prod?sslmode=require
DB_USERNAME = neondb_owner
DB_PASSWORD = npg_U8q4DIVoXdpL
PAYSTACK_SECRET_KEY = sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
PAYSTACK_PUBLIC_KEY = pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae
PAYSTACK_CALLBACK_URL = https://payment-service-xxx.onrender.com/api/v1/webhooks/paystack
PAYSTACK_WEBHOOK_URL = https://payment-service-xxx.onrender.com/api/v1/webhooks/paystack
SMTP_USER = diolux.inc@gmail.com
SMTP_PASSWORD = iueezyshzmptfhjt
SMTP_FROM = AxioQuan <diolux.inc@gmail.com>
WEBHOOK_SECRET = AxioQuan_Secure_2026_!_99
SERVER_PORT = 8080
SERVER_CONTEXT_PATH = /api
LOG_LEVEL = INFO
```

### Get Deploy Hook URL

1. In Render Dashboard → Your Service → **Settings** tab
2. Scroll to **"Deploy Hook"**
3. Copy the URL (looks like: `https://api.render.com/deploy/srv-abc123...`)
4. Add to GitHub Secrets as `RENDER_DEPLOY_HOOK_URL`

---

## ✅ STEP 3: Git Push

```bash
# From project directory
cd c:\Users\NEC\Desktop\payment-service

# Initialize if needed
git init
git remote add origin https://github.com/sancy1/axio-payment.git
git config user.email "your@email.com"
git config user.name "Your Name"

# Add and commit
git add .
git commit -m "Initial: Payment Service with Docker & CI/CD"

# Push
git branch -M main
git push -u origin main
```

---

## ✅ STEP 4: Monitor Deployment

### GitHub Actions
- URL: https://github.com/sancy1/axio-payment/actions
- Watch for:
  - ✅ **ci-cd.yml** → builds and tests code
  - ✅ **deploy-render.yml** → triggers Render webhook

### Render Dashboard
- URL: https://dashboard.render.com
- Watch for: **"Deploy in progress..."** → **"Live"**
- Takes 2-3 minutes

---

## ✅ STEP 5: Verify Application

```bash
# Get your Render URL (https://dashboard.render.com)
# Should look like: https://payment-service-xxxx.onrender.com

# Test health
curl https://payment-service-xxxx.onrender.com/api/actuator/health

# Should return:
# {"status":"UP"}
```

---

## 📝 Important Notes

### Files Automatically Ignored (Safe!)
- ❌ `.env.local` - NOT pushed to GitHub
- ❌ `*.sql` files - NOT pushed to GitHub
- ❌ `*.txt` logs - NOT pushed to GitHub
- ✅ .gitignore already configured

### Sensitive Values
- **NEVER** hardcode secrets in code
- Use GitHub Secrets for CI/CD
- Use Render Env Vars for runtime
- All are encrypted and hidden

### After First Successful Deployment
1. Update Paystack webhook URL in dashboard
2. Test payment flow end-to-end
3. Monitor logs in Render dashboard

---

## 🆘 Troubleshooting

| Issue | Check |
|-------|-------|
| Build fails | All 8 GitHub Secrets added? |
| Deploy fails | All env vars in Render? |
| App won't start | DATABASE_URL correct? |
| No database | DB credentials match? |
| Webhook fails | Paystack webhook URL updated? |

---

## 🎯 You are ready! 

**Deployment Flow:**
1. ✅ GitHub Secrets (8 items)
2. ✅ Render Service created
3. ✅ Environment variables in Render
4. ✅ Deploy hook URL added to GitHub Secrets
5. ✅ Push code to GitHub
6. ✅ GitHub Actions triggers automatically
7. ✅ Render deploys automatically
8. ✅ Application live!

