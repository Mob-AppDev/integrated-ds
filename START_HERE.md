# 🚀 DevSync - Quick Start Guide

Welcome to DevSync! Follow these steps to get your chat application running.

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Firebase Account
- Expo CLI (`npm install -g @expo/cli`)

## 🏃‍♂️ Quick Start (5 minutes)

### 1. Database Setup
```bash
cd database
chmod +x setup.sh
./setup.sh
```

### 2. Environment Configuration
```bash
cp .env.example .env
# Edit .env with your Firebase credentials
```

### 3. Start Backend
```bash
cd backend
./mvnw spring-boot:run
```

### 4. Start Frontend
```bash
npm run dev
```

### 5. Test the App
- Open Expo app on your phone
- Scan the QR code
- Create account and start chatting!

## 🧪 Test Credentials

Username: `admin` | Password: `password123`
Username: `caleb_adams` | Password: `password123`

## 🔥 Firebase Setup (Required for Push Notifications)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create project "DevSync"
3. Enable Authentication → Email/Password
4. Enable Cloud Messaging
5. Download service account JSON → place in `backend/src/main/resources/firebase-service-account.json`
6. Update `.env` with your Firebase config

## ✅ You're Done When...

- Backend starts without errors
- Frontend loads on your device
- You can create account and login
- Messages send and receive
- Push notifications work

## 📚 Full Documentation

- [Complete Setup Guide](SETUP_GUIDE.md)
- [Deployment Checklist](DEPLOYMENT_CHECKLIST.md)
- [Database Documentation](database/README.md)
- [Backend API Documentation](backend/README.md)

Happy coding! 🎉