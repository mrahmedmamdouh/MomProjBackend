---
applyTo: '**'
---
# 🔧 Mom Care Platform - Configuration Guide

## 📚 Related Documentation

- **[main.instructions.md](./main.instructions.md)** - Complete backend API documentation and endpoints
- **[setup.instructions.md](./setup.instructions.md)** - Backend setup and deployment instructions
- **[ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md)** - Mobile app integration guide
- **[current-tasks.instructions.md](./current-tasks.instructions.md)** - Current development tasks and priorities

## Overview

This guide covers all configuration aspects of the Mom Care Platform backend, including authentication, database, server settings, and environment-specific configurations.

## 🔐 Authentication Configuration

### **Token Authentication Settings**

The authentication system supports configurable token expiration and security parameters via `application.yaml`:

```yaml
auth:
  accessTokenExpiryMinutes: 30    # Access token expiry in minutes
  refreshTokenExpiryDays: 30      # Refresh token expiry in days  
  idleTimeoutHours: 24           # Idle timeout in hours
```

### **Configuration Parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `accessTokenExpiryMinutes` | Long | 30 | Access token expiration in minutes |
| `refreshTokenExpiryDays` | Long | 30 | Refresh token expiration in days |
| `idleTimeoutHours` | Long | 24 | Idle timeout for refresh tokens in hours |

### **Environment-Specific Examples**

#### **Development Environment**
```yaml
auth:
  accessTokenExpiryMinutes: 60    # Longer tokens for development
  refreshTokenExpiryDays: 7       # Shorter refresh for security
  idleTimeoutHours: 8            # Business hours only
```

#### **Production Environment**
```yaml
auth:
  accessTokenExpiryMinutes: 15    # Stricter security
  refreshTokenExpiryDays: 30      # Standard refresh period
  idleTimeoutHours: 24           # Full day access
```

#### **High Security Environment**
```yaml
auth:
  accessTokenExpiryMinutes: 5     # Very short-lived tokens
  refreshTokenExpiryDays: 1       # Daily refresh required
  idleTimeoutHours: 2            # Short idle timeout
```

#### **Testing Environment**
```yaml
auth:
  accessTokenExpiryMinutes: 1     # Quick expiry for testing
  refreshTokenExpiryDays: 1       # Short refresh for testing
  idleTimeoutHours: 1            # Quick idle timeout
```

## 📋 Complete Environment Configuration Templates

### 🔧 **Development Environment (application.yaml)**

```yaml
# Development Configuration - Optimized for development workflow
ktor:
  application:
    modules:
      - com.evelolvetech.ApplicationKt.module
  deployment:
    port: 8080
    host: 0.0.0.0
    watch:
      - classes
      - resources

# Authentication Configuration - Developer-friendly settings
auth:
  accessTokenExpiryMinutes: 60    # 1 hour - longer for development
  refreshTokenExpiryDays: 7       # 1 week - convenient for development
  idleTimeoutHours: 8            # 8 hours - business day

# JWT Configuration - Development keys (change in production!)
jwt:
  domain: "https://jwt-provider-domain/"
  audience: "jwt-audience"
  realm: "ktor sample app"
  secret: "dev-jwt-secret-key-change-in-production"

# Database Configuration - Local development
database:
  mongodb:
    uri: "mongodb://localhost:27017/?maxPoolSize=10&w=majority"
    name: "momproject_dev"

# File Upload Configuration - Development paths
uploads:
  baseDir: "./uploads"
  maxFileSize: 10485760  # 10MB
  allowedExtensions: ["jpg", "jpeg", "png", "gif"]

# Logging Configuration - Verbose for development
logging:
  level: DEBUG
  loggers:
    com.evelolvetech: DEBUG
    org.mongodb: INFO
    io.ktor: INFO
```

### 🚀 **Production Environment (application.yaml)**

```yaml
# Production Configuration - Optimized for performance and security
ktor:
  application:
    modules:
      - com.evelolvetech.ApplicationKt.module
  deployment:
    port: 8080
    host: 0.0.0.0
    # No watch in production for better performance

# Authentication Configuration - Production security settings
auth:
  accessTokenExpiryMinutes: 15    # 15 minutes - stricter security
  refreshTokenExpiryDays: 30      # 30 days - standard refresh period
  idleTimeoutHours: 24           # 24 hours - full day access

# JWT Configuration - Use environment variables in production
jwt:
  domain: "${JWT_DOMAIN:https://api.momcare.com}"
  audience: "${JWT_AUDIENCE:momcare-api}"
  realm: "${JWT_REALM:Mom Care Platform}"
  secret: "${JWT_SECRET}"  # MUST be set via environment variable

# Database Configuration - Production MongoDB
database:
  mongodb:
    uri: "${MONGODB_URI:mongodb://localhost:27017/?maxPoolSize=20&w=majority&readPreference=primary}"
    name: "${MONGODB_DATABASE:momproject}"

# File Upload Configuration - Production paths
uploads:
  baseDir: "${UPLOAD_DIR:/app/uploads}"
  maxFileSize: 10485760  # 10MB
  allowedExtensions: ["jpg", "jpeg", "png", "gif"]

# Logging Configuration - Production logging
logging:
  level: INFO
  loggers:
    com.evelolvetech: INFO
    org.mongodb: WARN
    io.ktor: WARN
    root: WARN

# Security Configuration
security:
  cors:
    allowedHosts: ["https://app.momcare.com", "https://admin.momcare.com"]
    allowedMethods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allowedHeaders: ["Authorization", "Content-Type", "Accept"]
```

### 🔒 **High Security Environment (application.yaml)**

```yaml
# High Security Configuration - Maximum security settings
ktor:
  application:
    modules:
      - com.evelolvetech.ApplicationKt.module
  deployment:
    port: 8080
    host: 0.0.0.0
    # SSL termination handled by reverse proxy

# Authentication Configuration - High security settings
auth:
  accessTokenExpiryMinutes: 5     # 5 minutes - very short-lived tokens
  refreshTokenExpiryDays: 1       # 1 day - daily refresh required
  idleTimeoutHours: 2            # 2 hours - short idle timeout

# JWT Configuration - High security JWT settings
jwt:
  domain: "${JWT_DOMAIN}"         # MUST be set via environment
  audience: "${JWT_AUDIENCE}"     # MUST be set via environment
  realm: "${JWT_REALM}"           # MUST be set via environment
  secret: "${JWT_SECRET}"         # MUST be complex and rotated regularly

# Database Configuration - Secured MongoDB with authentication
database:
  mongodb:
    uri: "${MONGODB_URI}"  # Must include authentication and SSL
    name: "${MONGODB_DATABASE}"
    # Example: mongodb://username:password@host:port/database?ssl=true&authSource=admin

# File Upload Configuration - Restricted upload settings
uploads:
  baseDir: "${UPLOAD_DIR}"
  maxFileSize: 5242880   # 5MB - reduced for security
  allowedExtensions: ["jpg", "jpeg", "png"]  # Reduced allowed types

# Logging Configuration - Security-focused logging
logging:
  level: WARN
  loggers:
    com.evelolvetech: INFO
    org.mongodb: ERROR
    io.ktor: ERROR
    root: ERROR
    security: DEBUG  # Log security events

# Security Configuration - Strict security settings
security:
  cors:
    allowedHosts: ["${ALLOWED_ORIGIN}"]  # Single trusted origin
    allowedMethods: ["GET", "POST", "PUT", "DELETE"]  # No OPTIONS
    allowedHeaders: ["Authorization", "Content-Type"]
  rateLimit:
    enabled: true
    maxRequests: 100
    windowMinutes: 15
  ssl:
    required: true
    hstsMaxAge: 31536000  # 1 year

# Environment Variables Required for High Security:
# - JWT_DOMAIN: Your JWT issuer domain
# - JWT_AUDIENCE: Your JWT audience
# - JWT_REALM: Your JWT realm
# - JWT_SECRET: Strong JWT secret (min 256 bits)
# - MONGODB_URI: Authenticated MongoDB connection string
# - MONGODB_DATABASE: Database name
# - UPLOAD_DIR: Secure upload directory path
# - ALLOWED_ORIGIN: Single trusted frontend origin
```

### 🧪 **Testing Environment (application.yaml)**

```yaml
# Testing Configuration - Optimized for automated testing
ktor:
  application:
    modules:
      - com.evelolvetech.ApplicationKt.module
  deployment:
    port: 8081  # Different port for testing
    host: 127.0.0.1  # Localhost only

# Authentication Configuration - Fast expiry for testing
auth:
  accessTokenExpiryMinutes: 1     # 1 minute - quick expiry for testing
  refreshTokenExpiryDays: 1       # 1 day - short refresh for testing
  idleTimeoutHours: 1            # 1 hour - quick idle timeout

# JWT Configuration - Testing keys
jwt:
  domain: "https://test-jwt-domain/"
  audience: "test-audience"
  realm: "test app"
  secret: "test-jwt-secret-key-for-testing-only"

# Database Configuration - Test database
database:
  mongodb:
    uri: "mongodb://localhost:27017/?maxPoolSize=5&w=majority"
    name: "momproject_test"

# File Upload Configuration - Test paths
uploads:
  baseDir: "./test-uploads"
  maxFileSize: 1048576  # 1MB for faster tests
  allowedExtensions: ["jpg", "png"]

# Logging Configuration - Minimal logging for tests
logging:
  level: ERROR
  loggers:
    com.evelolvetech: INFO
    org.mongodb: ERROR
    io.ktor: ERROR
    root: ERROR
```

## 🗄️ Database Configuration

### **MongoDB Settings**

```yaml
# Default connection (embedded in code)
mongodb:
  uri: "mongodb://localhost:27017/?maxPoolSize=20&w=majority"
  database: "momproject"
```

### **Connection Pool Configuration**

The MongoDB connection is configured with:
- **Max Pool Size**: 20 connections
- **Write Concern**: Majority acknowledgment
- **Read Preference**: Primary

### **Database Collections**

| Collection | Purpose | Indexes |
|------------|---------|---------|
| `users` | User authentication data | email (unique) |
| `moms` | Mom profile data | userId (unique) |
| `doctors` | Doctor profile data | userId (unique) |
| `refreshTokens` | Refresh token storage | token (unique), userId |
| `categories` | Product categories | name |
| `products` | Product catalog | categoryId, name |
| `carts` | Shopping carts | userId (unique) |
| `orders` | Order history | userId, createdAt |

## 🌐 Server Configuration

### **Ktor Server Settings**

```yaml
ktor:
  application:
    modules:
      - com.evelolvetech.ApplicationKt.module
  deployment:
    port: 8080                    # Server port
    host: 0.0.0.0                # Bind to all interfaces
```

### **JWT Configuration**

```yaml
jwt:
  domain: "https://jwt-provider-domain/"
  audience: "jwt-audience"
  realm: "ktor sample app"
  secret: "your-jwt-secret-key"  # Change in production!
```

## 📁 File Upload Configuration

### **Upload Directory Structure**

```
uploads/
├── profiles/          # User profile photos
├── nids/             # NID document images
└── products/         # Product images (future)
```

### **File Upload Settings**

- **Max File Size**: 10MB per file
- **Allowed Extensions**: jpg, jpeg, png, gif
- **Storage**: Local filesystem with UUID naming
- **Access**: Static file serving via `/uploads/*` endpoints

### **Security Features**

- File type validation
- Size limit enforcement
- UUID-based naming to prevent conflicts
- Secure file disposal on validation failures

## 🔒 Security Configuration

### **Authentication Flow**

1. **Registration/Login**: Returns access token + refresh token
2. **API Requests**: Use access token (expires in 30 minutes default)
3. **Token Refresh**: Use refresh token to get new access token
4. **Idle Timeout**: Refresh token expires if unused for 24 hours default
5. **Logout**: Revokes refresh token

### **Security Headers**

The application includes:
- CORS configuration
- Content-Type validation
- File upload security
- JWT token validation

### **Password Security**

- **Hashing**: PBKDF2 with SHA-256
- **Salt**: 32-byte random salt per password
- **Storage**: Hash:Salt format in database

## 🧪 Testing Configuration

### **Test Environment Setup**

For testing, you can use shorter token expiration:

```yaml
auth:
  accessTokenExpiryMinutes: 2     # 2 minutes for testing
  refreshTokenExpiryDays: 1       # 1 day for testing
  idleTimeoutHours: 1            # 1 hour for testing
```

### **Test Accounts**

Pre-seeded test accounts:
- **Mom**: alice@example.com (password: password123)
- **Mom**: beth@example.com (password: password123)  
- **Doctor**: dr.smith@example.com (password: password123)

### **Testing Token Flow**

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@example.com", "password": "password123"}'

# 2. Use access token for API calls
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer <access_token>"

# 3. Refresh token when expired
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refresh_token>"}'

# 4. Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refresh_token>"}'
```

## 🚀 Production Deployment Configuration

### **Environment Variables**

For production, consider using environment variables:

```yaml
jwt:
  secret: ${JWT_SECRET:default-secret}  # Use environment variable

auth:
  accessTokenExpiryMinutes: ${ACCESS_TOKEN_EXPIRY:15}
  refreshTokenExpiryDays: ${REFRESH_TOKEN_EXPIRY:30}
  idleTimeoutHours: ${IDLE_TIMEOUT:24}
```

### **Production Checklist**

- [ ] Change JWT secret from default
- [ ] Configure appropriate token expiration times
- [ ] Set up MongoDB with authentication
- [ ] Configure HTTPS/TLS
- [ ] Set up file upload directory with proper permissions
- [ ] Configure logging levels
- [ ] Set up monitoring and health checks

### **Docker Compose Configurations**

#### **Development Docker Compose**

```yaml
# docker-compose.dev.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - ENVIRONMENT=development
    volumes:
      - ./uploads:/app/uploads
      - ./src:/app/src  # Hot reload for development
    depends_on:
      - mongodb
    
  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_dev_data:/data/db
    environment:
      - MONGO_INITDB_DATABASE=momproject_dev

volumes:
  mongodb_dev_data:
```

#### **Production Docker Compose**

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - JWT_DOMAIN=${JWT_DOMAIN}
      - JWT_AUDIENCE=${JWT_AUDIENCE}
      - JWT_REALM=${JWT_REALM}
      - MONGODB_URI=${MONGODB_URI}
      - MONGODB_DATABASE=${MONGODB_DATABASE}
      - UPLOAD_DIR=/app/uploads
    volumes:
      - uploads_data:/app/uploads
    restart: unless-stopped
    depends_on:
      - mongodb
    
  mongodb:
    image: mongo:7.0
    environment:
      - MONGO_INITDB_ROOT_USERNAME=${MONGO_ROOT_USERNAME}
      - MONGO_INITDB_ROOT_PASSWORD=${MONGO_ROOT_PASSWORD}
      - MONGO_INITDB_DATABASE=${MONGODB_DATABASE}
    volumes:
      - mongodb_prod_data:/data/db
    restart: unless-stopped
    command: mongod --auth

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    restart: unless-stopped

volumes:
  mongodb_prod_data:
  uploads_data:
```

#### **High Security Docker Compose**

```yaml
# docker-compose.security.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "127.0.0.1:8080:8080"  # Bind to localhost only
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - JWT_DOMAIN=${JWT_DOMAIN}
      - JWT_AUDIENCE=${JWT_AUDIENCE}
      - JWT_REALM=${JWT_REALM}
      - MONGODB_URI=${MONGODB_URI}
      - MONGODB_DATABASE=${MONGODB_DATABASE}
      - UPLOAD_DIR=/app/uploads
      - ALLOWED_ORIGIN=${ALLOWED_ORIGIN}
    volumes:
      - uploads_data:/app/uploads:ro  # Read-only uploads
    restart: unless-stopped
    depends_on:
      - mongodb
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp:noexec,nosuid,size=100m
    
  mongodb:
    image: mongo:7.0
    environment:
      - MONGO_INITDB_ROOT_USERNAME=${MONGO_ROOT_USERNAME}
      - MONGO_INITDB_ROOT_PASSWORD=${MONGO_ROOT_PASSWORD}
      - MONGO_INITDB_DATABASE=${MONGODB_DATABASE}
    volumes:
      - mongodb_security_data:/data/db
    restart: unless-stopped
    command: mongod --auth --tlsMode requireTLS --tlsCertificateKeyFile /etc/ssl/mongodb.pem
    security_opt:
      - no-new-privileges:true

  nginx:
    image: nginx:alpine
    ports:
      - "443:443"  # HTTPS only
    volumes:
      - ./nginx-security.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    restart: unless-stopped
    security_opt:
      - no-new-privileges:true

volumes:
  mongodb_security_data:
  uploads_data:
```

### **Dockerfile Configuration**

Example Dockerfile configuration:

```dockerfile
# Environment variables
ENV JWT_SECRET=your-production-secret
ENV ACCESS_TOKEN_EXPIRY=15
ENV REFRESH_TOKEN_EXPIRY=30
ENV IDLE_TIMEOUT=24

# Create uploads directory
RUN mkdir -p /app/uploads/profiles /app/uploads/nids
RUN chmod 755 /app/uploads
```

## 🔄 Configuration Updates

### **Runtime Configuration Changes**

Most configuration changes require application restart, except:
- Database connection pool adjustments
- Logging level changes

### **Hot Reload Support**

For development, use:
```bash
./gradlew run --continuous
```

This enables automatic restart on code changes but not configuration changes.

## 🛠️ Configuration Validation

### **Startup Validation**

The application validates configuration on startup:
- JWT secret presence
- Database connectivity
- Upload directory permissions
- Token expiration values (must be positive)

### **Configuration Testing**

Test configuration changes:

```bash
# Test with custom configuration
./gradlew run -Dauth.accessTokenExpiryMinutes=5

# Verify configuration loading
curl http://localhost:8080/api/auth/login # Check token expiry in response
```

## 📋 Configuration Reference

### **Complete application.yaml Template**

```yaml
# Server Configuration
ktor:
  application:
    modules:
      - com.evelolvetech.ApplicationKt.module
  deployment:
    port: 8080
    host: 0.0.0.0

# JWT Configuration
jwt:
  domain: "https://jwt-provider-domain/"
  audience: "jwt-audience"
  realm: "ktor sample app"
  secret: "your-jwt-secret-key"

# Authentication Configuration
auth:
  accessTokenExpiryMinutes: 30    # Access token expiry (minutes)
  refreshTokenExpiryDays: 30      # Refresh token expiry (days)
  idleTimeoutHours: 24           # Idle timeout (hours)

# Database Configuration (embedded in code)
# mongodb://localhost:27017/?maxPoolSize=20&w=majority

# File Upload Configuration (embedded in code)
# uploads/ directory with profiles/ and nids/ subdirectories
# Max file size: 10MB
# Allowed types: jpg, jpeg, png, gif
```

## 🔍 Troubleshooting Configuration

### **Common Issues**

1. **Token Expiry Too Short**: Users complain about frequent logouts
   - Solution: Increase `accessTokenExpiryMinutes` or `idleTimeoutHours`

2. **Refresh Token Expired**: Users forced to login frequently
   - Solution: Increase `refreshTokenExpiryDays`

3. **Security Too Loose**: Tokens last too long
   - Solution: Decrease expiration times for stricter security

4. **Database Connection Issues**: Check MongoDB connectivity
   - Solution: Verify MongoDB is running and accessible

5. **File Upload Failures**: Check upload directory permissions
   - Solution: Ensure uploads/ directory exists and is writable

### **Configuration Debugging**

Enable debug logging to see configuration loading:

```yaml
# Add to logback.xml
<logger name="com.evelolvetech.util.AuthConfig" level="DEBUG"/>
```

---

**🎯 This configuration guide provides comprehensive control over the Mom Care Platform's behavior for any deployment scenario!**
