#!/bin/bash

# DevSync Database Setup Script
# This script sets up the PostgreSQL database for DevSync

set -e

# Configuration
DB_NAME="devsync"
DB_USER="devsync"
DB_PASSWORD="password"
DB_HOST="localhost"
DB_PORT="5432"

echo "🚀 Setting up DevSync PostgreSQL database..."

# Check if PostgreSQL is running
if ! pg_isready -h $DB_HOST -p $DB_PORT > /dev/null 2>&1; then
    echo "❌ PostgreSQL is not running on $DB_HOST:$DB_PORT"
    echo "Please start PostgreSQL and try again."
    exit 1
fi

echo "✅ PostgreSQL is running"

# Create database and user (requires superuser privileges)
echo "📝 Creating database and user..."
psql -h $DB_HOST -p $DB_PORT -U postgres -c "CREATE DATABASE $DB_NAME;" 2>/dev/null || echo "Database $DB_NAME already exists"
psql -h $DB_HOST -p $DB_PORT -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" 2>/dev/null || echo "User $DB_USER already exists"
psql -h $DB_HOST -p $DB_PORT -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

echo "✅ Database and user created"

# Run schema creation
echo "📋 Creating database schema..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f schema.sql

echo "✅ Schema created successfully"

# Insert sample data
echo "📊 Inserting sample data..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f sample_data.sql

echo "✅ Sample data inserted"

# Verify setup
echo "🔍 Verifying database setup..."
USER_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM users;")
CHANNEL_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM channels;")
MESSAGE_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM messages;")

echo "📊 Database Statistics:"
echo "   Users: $USER_COUNT"
echo "   Channels: $CHANNEL_COUNT"
echo "   Messages: $MESSAGE_COUNT"

echo ""
echo "🎉 DevSync database setup complete!"
echo ""
echo "📋 Connection Details:"
echo "   Database: $DB_NAME"
echo "   User: $DB_USER"
echo "   Host: $DB_HOST"
echo "   Port: $DB_PORT"
echo ""
echo "🔑 Sample Login Credentials:"
echo "   Username: admin"
echo "   Password: password123"
echo ""
echo "   Username: caleb_adams"
echo "   Password: password123"
echo ""
echo "🚀 You can now start your Spring Boot backend!"