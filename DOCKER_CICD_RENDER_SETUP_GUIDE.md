# 🚀 Docker & CI/CD Setup Guide - Complete Implementation

**Date:** April 1, 2026  
**Application:** Payment Service (Spring Boot)  
**Deployment:** Render (via Docker)  
**CI/CD:** GitHub Actions

---

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Local Development with Docker](#local-development-with-docker)
3. [GitHub Actions CI/CD Setup](#github-actions-cicd-setup)
4. [GitHub Secrets Configuration](#github-secrets-configuration)
5. [Render Deployment Setup](#render-deployment-setup)
6. [Environment Variables Mapping](#environment-variables-mapping)
7. [Step-by-Step Deployment](#step-by-step-deployment)
8. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

```
Developer Pushes Code
         ↓
   GitHub Actions CI/CD
    (Build, Test, Security Scan)
         ↓
  Docker Image Build & Push
   (to ghcr.io registry)
         ↓
  Trigger Render Deployment
   (via webhook)
         ↓
   Production Environment
    (Running on Render)
```

---

## Local Development with Docker

### **1. Run Entire Stack Locally**

```bash
# Start PostgreSQL + Payment Service
docker-compose -f docker-compose.dev.yml up -d

# Verify services are running
docker-compose -f docker-compose.dev.yml ps

# Check logs
docker-compose -f docker-compose.dev.yml logs -f payment-service

# Stop services
docker-compose -f docker-compose.dev.yml down
```

### **2. Files Explained**

- **`docker-compose.dev.yml`** - For local development
  - PostgreSQL 15 (local development database)
  - No application service (you run locally with `mvn spring-boot:run`)

- **`docker-compose.yml`** - For production/deployed environment
  - PostgreSQL service
  - Payment Service application
  - Both configured with environment variables
  - Health checks included
  - Proper networking and volumes

- **`Dockerfile`** - Multi-stage build
  - Stage 1: Maven build (compiles JAR)
  - Stage 2: Runtime (minimal JRE, non-root user)
  - Result: Smaller, secure image

---

## GitHub Actions CI/CD Setup

### **Workflow 1: CI/CD Pipeline** (`ci-cd.yml`)

**Triggers:**
- Push to `main` or `develop` branch
- Pull requests to `main` or `develop`

**What it does:**
1. ✅ Checkout code
2. ✅ Setup JDK 17
3. ✅ Run Maven compilation
4. ✅ Run unit tests
5. ✅ Build JAR package
6. ✅ Build Docker image
7. ✅ Push to GitHub Container Registry (ghcr.io)
8. ✅ Run security vulnerability scan

### **Workflow 2: Deploy to Render** (`deploy-render.yml`)

**Triggers:**
- Push to `main` branch
- Successful CI/CD workflow completion

**What it does:**
1. ✅ Triggers Render webhook for deployment
2. ✅ Notifies deployment status

---

## GitHub Secrets Configuration

### **CRITICAL: These must be added to GitHub before first push!**

**Location:** GitHub Repository → Settings → Secrets and Variables → Actions

### **Secrets to Add:**

| Secret Name | Value from .env.local | Purpose |
|-------------|----------------------|---------|
| `PAYSTACK_SECRET_KEY` | `sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58` | Paystack API secret key |
| `PAYSTACK_PUBLIC_KEY` | `pk_test_f85f8a8fcc639dd55436ebf69401e4bd9eef2dae` | Paystack public key |
| `DB_USERNAME` | `neondb_owner` | Database username |
| `DB_PASSWORD` | `npg_U8q4DIVoXdpL` | Database password |
| `SMTP_USER` | `diolux.inc@gmail.com` | Gmail SMTP username |
| `SMTP_PASSWORD` | `iueezyshzmptfhjt` | Gmail app-specific password |
| `WEBHOOK_SECRET` | `AxioQuan_Secure_2026_!_99` | Webhook signature verification |
| `RENDER_DEPLOY_HOOK_URL` | From Render dashboard | Webhook URL for deployment trigger |

### **Step-by-Step: Add GitHub Secrets**

1. **Go to Repository Settings**
   ```
   https://github.com/sancy1/axio-payment/settings/secrets/actions
   ```

2. **Click "New repository secret"** for each key:

   ```
   Secret name: PAYSTACK_SECRET_KEY
   Secret value: sk_test_e7ffba8d840e040ded09907ec868eab1a43fce58
   [Add secret]
   ```

3. **Repeat for all secrets** (see table above)

4. **Example for Database:**
   ```
   Secret name: DB_PASSWORD
   Secret value: npg_U8q4DIVoXdpL
   [Add secret]
   ```

5. **Verify** - All 8 secrets should appear in the list

---

## Render Deployment Setup

### **Step 1: Create New Web Service on Render**

1. Go to https://dashboard.render.com
2. Click **"New"** → **"Web Service"**
3. Select **"Build and deploy from a Git repository"**
4. **Connect GitHub account** if not already done
5. **Select repository:** `sancy1/axio-payment`
6. Click **"Connect"**

### **Step 2: Configure Service**

**Name:** `payment-service` (or any name you prefer)

**Environment:** `Docker`

**Branch:** `main`

**Dockerfile Path:** `./Dockerfile` (default, leave as is)

### **Step 3: Add Environment Variables to Render**

In the Render dashboard, scroll down to **"Environment"** section:

Click **"Add Environment Variable"** for each:

```
DATABASE_URL = jdbc:postgresql://your-neon-db-url:5432/axio_prod?sslmode=require
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

### **Step 4: Get Deploy Webhook URL**

1. In Render dashboard, go to your service
2. Click **"Settings"** tab
3. Scroll down to **"Deploy Hook"**
4. Copy the URL
5. Add as GitHub Secret `RENDER_DEPLOY_HOOK_URL`

**Example:**
```
https://api.render.com/deploy/srv-abc123xyz789?key=rnd_abc123xyz789
```

---

## Environment Variables Mapping

### **Direct Mapping: .env.local → GitHub Secrets → Render**

```
┌─────────────────────┐
│   .env.local        │  Your local development file
│ (DO NOT PUSH)       │  Contains all sensitive values
└──────────┬──────────┘
           │ Copy values from
           ↓
┌─────────────────────────────────────────┐
│      GitHub Secrets                     │  Added in Settings/Secrets/Actions
│  (Protected, encrypted, auto-masked)    │
│                                         │
│  PAYSTACK_SECRET_KEY = (hidden)        │
│  DB_PASSWORD = (hidden)                │
│  SMTP_PASSWORD = (hidden)              │
│  ... 8 total secrets                   │
└──────────┬──────────────────────────────┘
           │ Injected during CI/CD build
           ↓
┌─────────────────────────────────────────┐
│    Docker Build Process                │
│  (GitHub Actions)                       │
│                                         │
│  maven build → docker image             │
│  All env vars injected at runtime       │
└──────────┬──────────────────────────────┘
           │ Pushed to ghcr.io
           │ Webhook triggers Render
           ↓
┌─────────────────────────────────────────┐
│    Render Environment Variables        │
│  (Stored securely in Render)            │
│                                         │
│  DATABASE_URL = (your-neon-db)         │
│  DB_PASSWORD = (hidden)                │
│  PAYSTACK_SECRET_KEY = (hidden)       │
│  SMTP_PASSWORD = (hidden)              │
│  ... mirrored from GitHub Secrets      │
└──────────┬──────────────────────────────┘
           │
           ↓
    ✅ Running Spring Boot App
```

### **Key Differences:**

| File | Scope | Access | Purpose |
|------|-------|--------|---------|
| `.env.local` | Local machine | You only | Development & testing |
| GitHub Secrets | Repository | CI/CD workflows | Build-time configuration |
| Render Env Vars | Service | Running app | Runtime configuration |

---

## Step-by-Step Deployment

### **PHASE 1: Prepare GitHub Secrets (DO THIS FIRST)**

```bash
# 1. Go to your GitHub repo settings
https://github.com/sancy1/axio-payment/settings/secrets/actions

# 2. Add 8 secrets (see table above):
PAYSTACK_SECRET_KEY
PAYSTACK_PUBLIC_KEY
DB_USERNAME
DB_PASSWORD
SMTP_USER
SMTP_PASSWORD
WEBHOOK_SECRET
RENDER_DEPLOY_HOOK_URL (get this later from Render)
```

### **PHASE 2: Prepare Render Service**

```
1. Create Web Service on Render
2. Connect GitHub repo (sancy1/axio-payment)
3. Add all environment variables
4. Get Deploy Hook URL
5. Add RENDER_DEPLOY_HOOK_URL to GitHub Secrets
```

### **PHASE 3: Push Code to GitHub**

```bash
# From your local machine
cd c:\Users\NEC\Desktop\payment-service

# Initialize git (if not already done)
git init
git remote add origin https://github.com/sancy1/axio-payment.git

# Configure git
git config user.email "your-email@example.com"
git config user.name "Your Name"

# Add all files EXCEPT sensitive ones (already in .gitignore)
git add .
git status  # Review - should NOT show .env.local, *.sql, *.txt files

# Commit
git commit -m "Initial commit: Payment Service with Docker & CI/CD"

# Push to GitHub
git branch -M main
git push -u origin main
```

### **PHASE 4: Monitor CI/CD**

```
✅ GitHub Actions automatically triggers
   → Checkout code
   → Build & test
   → Build Docker image
   → Push to registry
   → Trigger Render deployment

📊 Monitor at:
   - GitHub Actions: https://github.com/sancy1/axio-payment/actions
   - Render Dashboard: https://dashboard.render.com
```

### **PHASE 5: Verify Deployment**

```bash
# Wait 2-3 minutes for deployment

# Check app health
curl https://payment-service-xxx.onrender.com/api/actuator/health

# If successful, response:
{
  "status": "UP"
}

# Test an endpoint
curl -X POST https://payment-service-xxx.onrender.com/api/v1/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

---

## Troubleshooting

### **Issue: GitHub Actions Build Fails**

**Symptoms:** Red ❌ on commit in GitHub Actions

**Solutions:**
1. Check GitHub Actions logs
2. Verify all secrets are added
3. Check `.gitignore` - sensitive files should not be committed
4. Run locally first: `mvn clean package -DskipTests`

### **Issue: Docker Image Not Built**

**Check:**
```bash
# Verify Dockerfile exists and is valid
ls -la Dockerfile

# Test build locally
docker build -t payment-service:test .

# If build fails, check logs
docker build -t payment-service:test . -->> error.log
```

### **Issue: Render Deployment Fails**

**Check:**
1. All environment variables added to Render
2. Database credentials correct
3. RENDER_DEPLOY_HOOK_URL is valid
4. GitHub secret matches Render webhook URL

**Render Logs:**
```
https://dashboard.render.com → Your Service → Logs tab
```

### **Issue: Application Starts But No Database**

**Check:**
1. `DATABASE_URL` is correct (use cloud database, not localhost)
2. `DB_USERNAME` and `DB_PASSWORD` match
3. Database is accessible from Render (check firewall rules on Neon)

**Test connection:**
```bash
# From Render logs, look for:
"Database connection established successfully"
```

### **Issue: Paystack Webhook Not Working**

**Check:**
1. `PAYSTACK_WEBHOOK_URL` points to correct Render URL
2. In Paystack dashboard, webhook URL is set to:
   ```
   https://payment-service-xxx.onrender.com/api/v1/webhooks/paystack
   ```
3. `WEBHOOK_SECRET` matches

---

## Security Checklist

- ✅ `.env.local` is in `.gitignore` (won't be pushed)
- ✅ GitHub Secrets are encrypted and masked
- ✅ Render environment variables are private
- ✅ Database credentials not hardcoded anywhere
- ✅ Dockerfile uses non-root user
- ✅ Production use HTTPS only (Render provides SSL)
- ✅ JWT secret is strong (non-guessable)

---

## Quick Reference: Commands

```bash
# Local Development
docker-compose -f docker-compose.dev.yml up -d
mvn spring-boot:run -DskipTests

# Build Docker locally
docker build -t payment-service:latest .

# Run built image
docker run -e DATABASE_URL=jdbc:postgresql://host:5432/db -p 8080:8080 payment-service:latest

# Git workflow
git add .
git commit -m "Your message"
git push origin main
```

---

## Summary: What Happens When You Push Code

1. **You:** `git push origin main`
2. **GitHub:** Detects push, triggers `.github/workflows/ci-cd.yml`
3. **CI/CD:**
   - ✅ Compiles code with `mvn compile`
   - ✅ Runs tests with `mvn test`
   - ✅ Builds JAR with `mvn package`
   - ✅ Builds Docker image from Dockerfile
   - ✅ Pushes image to ghcr.io
4. **Deploy Workflow:** Calls Render webhook
5. **Render:** 
   - ✅ Pulls Docker image
   - ✅ Loads environment variables
   - ✅ Starts container
   - ✅ Runs database migrations
6. **Result:** ✅ Application live at `https://payment-service-xxx.onrender.com`

---

**You are now ready to deploy! 🚀**

