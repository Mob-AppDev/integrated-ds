# DevSync Complete Setup Guide

This guide will walk you through setting up the complete DevSync application with backend, database, and Firebase integration.

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Firebase Account
- Expo CLI

## Backend Setup

### 1. Database Setup

Install PostgreSQL and create the database:

```sql
-- Connect to PostgreSQL as superuser
psql -U postgres

-- Create database and user
CREATE DATABASE devsync;
CREATE USER devsync WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE devsync TO devsync;
```

### 2. Firebase Project Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project called "DevSync"
3. Enable Authentication:
   - Go to Authentication > Sign-in method
   - Enable Email/Password
4. Enable Cloud Messaging:
   - Go to Project Settings > Cloud Messaging
   - Note down the Server Key
5. Generate Service Account:
   - Go to Project Settings > Service Accounts
   - Click "Generate new private key"
   - Download the JSON file
   - Rename it to `firebase-service-account.json`
   - Place it in `backend/src/main/resources/`

### 3. Backend Configuration

Update `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/devsync
    username: devsync
    password: password

jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000

firebase:
  config-path: firebase-service-account.json
```

### 4. Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend will be available at `http://localhost:8080`

## Frontend Setup

### 1. Environment Configuration

Create `.env` file in the root directory:

```env
# Backend API
EXPO_PUBLIC_API_URL=http://localhost:8080/api

# Firebase (get these from Firebase Console > Project Settings)
EXPO_PUBLIC_FIREBASE_API_KEY=your_api_key
EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
EXPO_PUBLIC_FIREBASE_PROJECT_ID=your_project_id
EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
EXPO_PUBLIC_FIREBASE_APP_ID=your_app_id

# Daily.co for video calls
EXPO_PUBLIC_DAILY_API_KEY=your_daily_api_key
EXPO_PUBLIC_DAILY_ROOM_URL=https://your_domain.daily.co/
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Run Frontend

```bash
npm run dev
```

## Testing the Setup

### 1. Test Backend API

```bash
# Test signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Test signin
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### 2. Test Frontend

1. Open the Expo app
2. Try signing up with a new account
3. Login with the account
4. Test messaging features

## Database Schema

The following tables will be automatically created:

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `email` (Unique)
- `password` (Encrypted)
- `first_name`
- `last_name`
- `profile_picture`
- `status` (ACTIVE, AWAY, DO_NOT_DISTURB, OFFLINE)
- `last_seen`
- `firebase_token`
- `is_online`
- `created_at`
- `updated_at`

### Roles Table
- `id` (Primary Key)
- `name` (ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN)

### Channels Table
- `id` (Primary Key)
- `name`
- `description`
- `is_private`
- `created_by` (Foreign Key to Users)
- `created_at`
- `updated_at`

### Messages Table
- `id` (Primary Key)
- `content`
- `sender_id` (Foreign Key to Users)
- `channel_id` (Foreign Key to Channels, nullable for DMs)
- `recipient_id` (Foreign Key to Users, nullable for channels)
- `parent_message_id` (Foreign Key to Messages, for threads)
- `type` (TEXT, IMAGE, FILE, AUDIO, VIDEO)
- `is_edited`
- `edited_at`
- `created_at`
- `updated_at`

### Junction Tables
- `user_roles` (Many-to-Many: Users ↔ Roles)
- `channel_members` (Many-to-Many: Channels ↔ Users)

## Push Notifications Setup

### 1. Firebase Cloud Messaging

The backend is configured to send push notifications for:
- Direct messages
- Channel messages
- Mentions

### 2. Frontend Integration

The app will automatically request notification permissions and register the device token with the backend.

## Production Deployment

### Backend Deployment

1. Build the application:
```bash
./mvnw clean package
```

2. Deploy to your preferred platform (AWS, Google Cloud, Heroku, etc.)

3. Set environment variables:
```bash
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
FIREBASE_CONFIG_PATH=firebase-service-account.json
```

### Frontend Deployment

1. Build for production:
```bash
npm run build:web
```

2. Deploy to Netlify, Vercel, or your preferred platform

### Mobile App Deployment

1. Build for app stores:
```bash
expo build:android
expo build:ios
```

2. Submit to Google Play Store and Apple App Store

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Ensure PostgreSQL is running
   - Check database credentials
   - Verify database exists

2. **Firebase Initialization Error**
   - Check if `firebase-service-account.json` exists
   - Verify Firebase project configuration
   - Ensure service account has proper permissions

3. **JWT Token Issues**
   - Verify JWT secret is set
   - Check token expiration settings
   - Ensure proper Authorization header format

4. **Push Notification Issues**
   - Verify Firebase project setup
   - Check device token registration
   - Ensure proper Firebase permissions

### Logs

Check application logs for detailed error information:

```bash
# Backend logs
tail -f logs/spring.log

# Frontend logs
expo logs
```

## Next Steps

1. **Real-time Features**: Implement WebSocket for real-time messaging
2. **File Upload**: Add file upload functionality for images/documents
3. **Voice Messages**: Integrate audio recording and playback
4. **Video Calls**: Complete Daily.co integration
5. **Search**: Implement full-text search for messages
6. **Analytics**: Add user analytics and monitoring
7. **Testing**: Add comprehensive unit and integration tests

## Support

For issues and questions:
1. Check the logs for error details
2. Verify all configuration steps
3. Test individual components separately
4. Check Firebase and database connectivity