# DevSync Backend

A comprehensive Spring Boot backend application for the DevSync Slack-style messaging application with real-time WebSocket communication, OAuth2 authentication, and Firebase Cloud Messaging.

## Features

### Core Features
- **Real-time Messaging**: WebSocket with STOMP protocol for instant messaging
- **JWT Authentication**: Secure token-based authentication with refresh tokens
- **OAuth2 Integration**: Login with Google, GitHub, and Facebook
- **Push Notifications**: Firebase Cloud Messaging for offline users
- **Typing Indicators**: Real-time typing status via WebSocket
- **Presence Management**: Online/offline user status tracking
- **Direct Messages**: Private one-on-one messaging
- **Channel Messaging**: Group chat functionality
- **Thread Support**: Message replies and threading
- **Cross-Origin Support**: CORS enabled for React Native frontend

### Technical Features
- **Layered Architecture**: Controller, Service, Repository pattern
- **DTO Pattern**: Data Transfer Objects for API responses
- **PostgreSQL Database**: Robust data persistence
- **Spring Security**: Comprehensive security configuration
- **Production Ready**: Error handling, validation, logging
- **Scalable Design**: Modular and maintainable codebase

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE devsync;
CREATE USER devsync WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE devsync TO devsync;
```

### 2. Environment Variables

Set the following environment variables or update `application.yml`:

```bash
export DB_USERNAME=devsync
export DB_PASSWORD=password
export JWT_SECRET=mySecretKey123456789012345678901234567890
export FIREBASE_CONFIG_PATH=firebase-service-account.json
```

### 3. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing project
3. Go to Project Settings > Service Accounts
4. Generate a new private key
5. Download the JSON file and rename it to `firebase-service-account.json`
6. Place it in `src/main/resources/`

### 4. Run the Application

```bash
# Development mode
./mvnw spring-boot:run

# Production build
./mvnw clean package
java -jar target/devsync-backend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### WebSocket Endpoints
- `WS /ws` - WebSocket connection with STOMP
- `SEND /app/chat.send` - Send messages
- `SEND /app/chat.typing` - Typing indicators
- `SUBSCRIBE /topic/channel.{channelId}` - Channel messages
- `SUBSCRIBE /user/queue/messages` - Direct messages
- `SUBSCRIBE /topic/presence` - User presence updates

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/signin` - Login user
- `POST /api/auth/signout` - Logout user
- `GET /oauth2/authorization/{provider}` - OAuth2 login (google, github, facebook)

### FCM Token Management
- `POST /api/fcm/token` - Update FCM token
- `DELETE /api/fcm/token` - Remove FCM token
- `GET /api/fcm/preferences` - Get notification preferences

### Users
- `GET /api/users/profile` - Get current user profile
- `GET /api/users/online` - Get online users
- `PUT /api/users/status` - Update user status
- `PUT /api/users/firebase-token` - Update Firebase token

### Channels
- `GET /api/channels` - Get user's channels
- `POST /api/channels` - Create new channel
- `POST /api/channels/{id}/join` - Join channel

### Messages
- `GET /api/chat/channels/{channelId}/messages` - Get channel message history
- `GET /api/chat/direct/{userId}/messages` - Get direct message history
- `POST /api/chat/messages` - Send message (fallback REST endpoint)
- `GET /api/chat/messages/{messageId}/replies` - Get thread replies

## WebSocket Communication

### Connection
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer ' + jwtToken
}, onConnected, onError);
```

### Sending Messages
```javascript
stompClient.send('/app/chat.send', {}, JSON.stringify({
    content: 'Hello World!',
    type: 'CHANNEL',
    channelId: 1,
    messageType: 'TEXT'
}));
```

### Receiving Messages
```javascript
// Channel messages
stompClient.subscribe('/topic/channel.1', onMessageReceived);

// Direct messages
stompClient.subscribe('/user/queue/messages', onDirectMessage);

// Presence updates
stompClient.subscribe('/topic/presence', onPresenceUpdate);
```

## OAuth2 Configuration

Add these environment variables for OAuth2 providers:

```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# GitHub OAuth2
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# Facebook OAuth2
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
```

## Database Schema

The application automatically creates the following tables:
- `users` - User accounts and profiles
- `roles` - User roles (USER, MODERATOR, ADMIN)
- `user_roles` - User-role relationships
- `channels` - Chat channels
- `channel_members` - Channel membership
- `messages` - Chat messages and threads
- `message_reactions` - Emoji reactions (future feature)
- `message_attachments` - File attachments (future feature)

## Security

- JWT tokens for authentication with configurable expiration
- Role-based access control
- OAuth2 integration with major providers
- CORS configuration for frontend integration
- Password encryption with BCrypt
- WebSocket authentication via JWT
- Secure API endpoints

## Push Notifications

Firebase Cloud Messaging automatically sends notifications when users are offline:
- Direct message notifications
- Channel message notifications
- Mention notifications
- Rich notifications with sender avatar and message preview
- Smart delivery (only when user is not connected via WebSocket)

## Real-time Features

### Typing Indicators
```javascript
stompClient.send('/app/chat.typing', {}, JSON.stringify({
    type: 'CHANNEL',
    targetId: channelId,
    isTyping: true
}));
```

### Presence Management
- Automatic online/offline detection via WebSocket connections
- Real-time presence broadcasts to all connected users
- Last seen timestamps
- User status management (Active, Away, Do Not Disturb, Offline)

## Development

### Project Structure
```
src/main/java/com/devsync/
├── config/          # Configuration classes
├── controller/      # REST and WebSocket controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── listener/       # Event listeners
├── payload/        # Request/Response payloads
├── repository/     # Data access layer
├── security/       # Security configuration
├── service/        # Business logic layer
└── utils/          # Utility classes
```

### Testing the API

#### REST API Testing
```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

#### WebSocket Testing
Use tools like:
- **Postman**: WebSocket request feature
- **wscat**: Command line WebSocket client
- **Browser DevTools**: WebSocket frame inspection

### Adding New Features

1. **New Message Types**: Extend `MessageType` enum and update DTOs
2. **New Notification Types**: Add methods to `NotificationService`
3. **New OAuth Providers**: Add configuration in `application.yml`
4. **New WebSocket Events**: Add `@MessageMapping` methods in controllers

### Database Migrations

The application uses Hibernate's `ddl-auto: update` for development. For production, consider using Flyway or Liquibase for proper database migrations.

## Production Deployment

### Environment Variables
```bash
# Database
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
DB_HOST=your_db_host

# JWT
JWT_SECRET=your_jwt_secret_key_minimum_256_bits
JWT_EXPIRATION=86400000

# Firebase
FIREBASE_CONFIG_PATH=firebase-service-account.json

# OAuth2 (optional)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
FACEBOOK_CLIENT_ID=your_facebook_client_id
FACEBOOK_CLIENT_SECRET=your_facebook_client_secret
```

### Docker Deployment
```bash
# Build image
docker build -t devsync-backend .

# Run with docker-compose
docker-compose up -d
```

### Performance Considerations
- **Connection Pooling**: Configure HikariCP for database connections
- **WebSocket Scaling**: Use Redis for session storage in multi-instance deployments
- **Caching**: Implement Redis caching for frequently accessed data
- **Load Balancing**: Use sticky sessions for WebSocket connections

## Monitoring and Logging

### Application Metrics
- WebSocket connection counts
- Message throughput
- Authentication success/failure rates
- Push notification delivery rates

### Logging Configuration
```yaml
logging:
  level:
    com.devsync: INFO
    org.springframework.messaging: DEBUG
    org.springframework.security: INFO
```

## API Documentation

### Message DTO Structure
```json
{
  "id": 1,
  "content": "Hello World!",
  "type": "CHANNEL",
  "messageType": "TEXT",
  "senderId": 1,
  "senderUsername": "john_doe",
  "channelId": 1,
  "channelName": "general",
  "timestamp": "2024-01-15T10:30:00"
}
```

### WebSocket Message Format
```json
{
  "content": "Hello World!",
  "type": "CHANNEL",
  "channelId": 1,
  "messageType": "TEXT"
}
```

This backend provides a complete, production-ready foundation for a Slack-style messaging application with modern real-time features and robust security.