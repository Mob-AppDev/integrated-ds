-- DevSync PostgreSQL Database Schema
-- This script creates all the necessary tables for the DevSync chat application

-- Create database (run this separately as superuser)
-- CREATE DATABASE devsync;
-- CREATE USER devsync WITH PASSWORD 'password';
-- GRANT ALL PRIVILEGES ON DATABASE devsync TO devsync;

-- Connect to devsync database before running the rest

-- Enable UUID extension for generating UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create ENUM types
CREATE TYPE user_status AS ENUM ('ACTIVE', 'AWAY', 'DO_NOT_DISTURB', 'OFFLINE');
CREATE TYPE role_name AS ENUM ('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN');
CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'FILE', 'AUDIO', 'VIDEO');

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name role_name NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    profile_picture VARCHAR(255),
    status user_status DEFAULT 'ACTIVE',
    last_seen TIMESTAMP WITH TIME ZONE,
    firebase_token TEXT,
    is_online BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create channels table
CREATE TABLE IF NOT EXISTS channels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_private BOOLEAN DEFAULT FALSE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Create channel_members junction table
CREATE TABLE IF NOT EXISTS channel_members (
    channel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (channel_id, user_id),
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    sender_id BIGINT NOT NULL,
    channel_id BIGINT,
    recipient_id BIGINT,
    parent_message_id BIGINT,
    type message_type DEFAULT 'TEXT',
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_message_id) REFERENCES messages(id) ON DELETE CASCADE,
    -- Ensure either channel_id or recipient_id is set (not both)
    CONSTRAINT check_message_target CHECK (
        (channel_id IS NOT NULL AND recipient_id IS NULL) OR 
        (channel_id IS NULL AND recipient_id IS NOT NULL)
    )
);

-- Create message_reactions table (for future emoji reactions)
CREATE TABLE IF NOT EXISTS message_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    emoji VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(message_id, user_id, emoji)
);

-- Create message_attachments table (for file uploads)
CREATE TABLE IF NOT EXISTS message_attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);

-- Create user_sessions table (for tracking active sessions)
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    device_info TEXT,
    ip_address INET,
    last_activity TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_online ON users(is_online);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

CREATE INDEX IF NOT EXISTS idx_channels_name ON channels(name);
CREATE INDEX IF NOT EXISTS idx_channels_is_private ON channels(is_private);
CREATE INDEX IF NOT EXISTS idx_channels_created_by ON channels(created_by);

CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_channel_id ON messages(channel_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_messages_parent_message_id ON messages(parent_message_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_messages_type ON messages(type);

CREATE INDEX IF NOT EXISTS idx_message_reactions_message_id ON message_reactions(message_id);
CREATE INDEX IF NOT EXISTS idx_message_reactions_user_id ON message_reactions(user_id);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_channels_updated_at BEFORE UPDATE ON channels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_messages_updated_at BEFORE UPDATE ON messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default roles
INSERT INTO roles (name) VALUES 
    ('ROLE_USER'),
    ('ROLE_MODERATOR'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Create default channels
INSERT INTO channels (name, description, is_private, created_by) VALUES 
    ('general', 'General discussion for everyone', FALSE, 1),
    ('random', 'Random conversations and fun stuff', FALSE, 1),
    ('announcements', 'Important announcements', FALSE, 1)
ON CONFLICT DO NOTHING;

-- Create views for common queries
CREATE OR REPLACE VIEW user_profiles AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.profile_picture,
    u.status,
    u.is_online,
    u.last_seen,
    u.created_at,
    ARRAY_AGG(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.email, u.first_name, u.last_name, 
         u.profile_picture, u.status, u.is_online, u.last_seen, u.created_at;

CREATE OR REPLACE VIEW channel_with_members AS
SELECT 
    c.id,
    c.name,
    c.description,
    c.is_private,
    c.created_by,
    c.created_at,
    c.updated_at,
    COUNT(cm.user_id) as member_count,
    ARRAY_AGG(cm.user_id) as member_ids
FROM channels c
LEFT JOIN channel_members cm ON c.id = cm.channel_id
GROUP BY c.id, c.name, c.description, c.is_private, c.created_by, c.created_at, c.updated_at;

CREATE OR REPLACE VIEW message_with_details AS
SELECT 
    m.id,
    m.content,
    m.type,
    m.is_edited,
    m.edited_at,
    m.created_at,
    m.updated_at,
    m.parent_message_id,
    u.username as sender_username,
    u.profile_picture as sender_avatar,
    c.name as channel_name,
    r.username as recipient_username,
    COUNT(replies.id) as reply_count
FROM messages m
JOIN users u ON m.sender_id = u.id
LEFT JOIN channels c ON m.channel_id = c.id
LEFT JOIN users r ON m.recipient_id = r.id
LEFT JOIN messages replies ON m.id = replies.parent_message_id
GROUP BY m.id, m.content, m.type, m.is_edited, m.edited_at, m.created_at, 
         m.updated_at, m.parent_message_id, u.username, u.profile_picture, 
         c.name, r.username;

-- Create function to clean up expired sessions
CREATE OR REPLACE FUNCTION cleanup_expired_sessions()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM user_sessions WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create function to get user's unread message count
CREATE OR REPLACE FUNCTION get_unread_count(user_id_param BIGINT)
RETURNS TABLE(channel_id BIGINT, channel_name VARCHAR, unread_count BIGINT) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id as channel_id,
        c.name as channel_name,
        COUNT(m.id) as unread_count
    FROM channels c
    JOIN channel_members cm ON c.id = cm.channel_id
    LEFT JOIN messages m ON c.id = m.channel_id 
        AND m.created_at > cm.joined_at
        AND m.sender_id != user_id_param
    WHERE cm.user_id = user_id_param
    GROUP BY c.id, c.name;
END;
$$ LANGUAGE plpgsql;

-- Grant permissions to devsync user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO devsync;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO devsync;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO devsync;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO devsync;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO devsync;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO devsync;

COMMIT;