@echo off
REM Manual DevSync Database Setup Script
REM This script sets up the PostgreSQL database for DevSync

setlocal enabledelayedexpansion

REM Configuration
set DB_NAME=devsync
set DB_USER=devsync
set DB_PASSWORD=password
set DB_HOST=localhost
set DB_PORT=5432

echo 🚀 Setting up DevSync PostgreSQL database manually...

REM Check if PostgreSQL is running
"C:\Program Files\PostgreSQL\17\bin\pg_isready.exe" -h %DB_HOST% -p %DB_PORT% >nul 2>&1
if errorlevel 1 (
    echo ❌ PostgreSQL is not running on %DB_HOST%:%DB_PORT%
    echo Please start PostgreSQL and try again.
    pause
    exit /b 1
)

echo ✅ PostgreSQL is running

REM Create database and user (requires superuser privileges)
echo 📝 Creating database and user...
echo Please enter the postgres password when prompted:
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -c "CREATE DATABASE %DB_NAME%;" 2>nul || echo Database %DB_NAME% already exists
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -c "CREATE USER %DB_USER% WITH PASSWORD '%DB_PASSWORD%';" 2>nul || echo User %DB_USER% already exists
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE %DB_NAME% TO %DB_USER%;"

echo ✅ Database and user created

REM Run schema creation
echo 📋 Creating database schema...
echo Please enter the postgres password when prompted:
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -d %DB_NAME% -f schema.sql

echo ✅ Schema created successfully

REM Insert sample data
echo 📊 Inserting sample data...
echo Please enter the postgres password when prompted:
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -d %DB_NAME% -f sample_data.sql

echo ✅ Sample data inserted

REM Verify setup
echo 🔍 Verifying database setup...
for /f %%i in ('"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -d %DB_NAME% -t -c "SELECT COUNT(*) FROM users;"') do set USER_COUNT=%%i
for /f %%i in ('"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -d %DB_NAME% -t -c "SELECT COUNT(*) FROM channels;"') do set CHANNEL_COUNT=%%i
for /f %%i in ('"C:\Program Files\PostgreSQL\17\bin\psql.exe" -h %DB_HOST% -p %DB_PORT% -U postgres -d %DB_NAME% -t -c "SELECT COUNT(*) FROM messages;"') do set MESSAGE_COUNT=%%i

echo 📊 Database Statistics:
echo    Users: %USER_COUNT%
echo    Channels: %CHANNEL_COUNT%
echo    Messages: %MESSAGE_COUNT%

echo.
echo 🎉 DevSync database setup complete!
echo.
echo 📋 Connection Details:
echo    Database: %DB_NAME%
echo    User: %DB_USER%
echo    Host: %DB_HOST%
echo    Port: %DB_PORT%
echo.
echo 🔑 Sample Login Credentials:
echo    Username: admin
echo    Password: password123
echo.
echo    Username: caleb_adams
echo    Password: password123
echo.
echo 🚀 You can now start your Spring Boot backend!

pause 