# DevSync Database Setup

This directory contains the PostgreSQL database schema and setup scripts for the DevSync application.

## Files

- `schema.sql` - Complete database schema with tables, indexes, and functions
- `sample_data.sql` - Sample data for testing and development
- `setup.sh` - Automated setup script for Unix/Linux/macOS
- `setup.bat` - Automated setup script for Windows

## Quick Setup

### Option 1: Automated Setup (Recommended)

**Unix/Linux/macOS:**
```bash
cd database
chmod +x setup.sh
./setup.sh
```

**Windows:**
```cmd
cd database
setup.bat
```

### Option 2: Manual Setup

1. **Create database and user:**
```sql
-- Connect as postgres superuser
psql -U postgres

-- Create database and user
CREATE DATABASE devsync;
CREATE USER devsync WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE devsync TO devsync;
\q
```

2. **Run schema:**
```bash
psql -U devsync -d devsync -f schema.sql
```

3. **Insert sample data:**
```bash
psql -U devsync -d devsync -f sample_data.sql
```

## Database Schema Overview

### Core Tables

- **users** - User accounts and profiles
- **roles** - User roles (USER, MODERATOR, ADMIN)
- **user_roles** - Many-to-many relationship between users and roles
- **channels** - Chat channels
- **channel_members** - Channel membership
- **messages** - Chat messages and threads

### Additional Tables

- **message_reactions** - Emoji reactions to messages
- **message_attachments** - File attachments
- **user_sessions** - Active user sessions

### Views

- **user_profiles** - Users with their roles
- **channel_with_members** - Channels with member counts
- **message_with_details** - Messages with sender and channel info

### Functions

- **cleanup_expired_sessions()** - Remove expired user sessions
- **get_unread_count(user_id)** - Get unread message counts per channel

## Sample Data

The sample data includes:

- **8 test users** with different roles and statuses
- **9 channels** including public and private channels
- **Sample messages** in channels and direct messages
- **Thread replies** and emoji reactions

### Test Accounts

All test accounts use the password: `password123`

- `admin` - Administrator with all roles
- `caleb_adams` - Regular user
- `michael_oti` - Regular user
- `hakeem_adam` - Regular user
- `frank_iokko` - Moderator
- `alvin` - Regular user
- `selormfidel` - Regular user
- `roger_osafo` - Regular user

## Configuration

Update your Spring Boot `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/devsync
    username: devsync
    password: password
```

## Maintenance

### Clean up expired sessions
```sql
SELECT cleanup_expired_sessions();
```

### Check unread counts for a user
```sql
SELECT * FROM get_unread_count(1);
```

### View user profiles with roles
```sql
SELECT * FROM user_profiles;
```

### View channels with member counts
```sql
SELECT * FROM channel_with_members;
```

## Production Notes

1. **Change default passwords** before deploying to production
2. **Set up proper backup strategy** for your database
3. **Configure SSL/TLS** for database connections
4. **Set up monitoring** for database performance
5. **Review and adjust indexes** based on query patterns
6. **Set up connection pooling** in your application

## Troubleshooting

### Connection Issues
- Ensure PostgreSQL is running: `pg_isready`
- Check connection settings in `application.yml`
- Verify user permissions: `GRANT ALL PRIVILEGES ON DATABASE devsync TO devsync;`

### Permission Issues
- Ensure the devsync user has proper permissions
- Check if tables are owned by the correct user
- Run the grant statements from `schema.sql`

### Performance Issues
- Check if indexes are being used: `EXPLAIN ANALYZE SELECT ...`
- Monitor slow queries: Enable `log_slow_statements`
- Consider adding more indexes for frequently queried columns