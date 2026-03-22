---
applyTo: '**'
---
# 🚀 Ktor Backend Setup Instructions

## Quick Start Guide for KMP Developers

This guide provides step-by-step instructions to set up the Ktor backend for KMP development.

## 📚 Related Documentation

- **[configuration.instructions.md](./configuration.instructions.md)** - Comprehensive configuration guide for authentication, tokens, and environment setup
- **[ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md)** - Mobile app integration guide
- **[main.instructions.md](./main.instructions.md)** - Complete API documentation

## 📋 Prerequisites

### **Required Software**
- **Java 21+** (OpenJDK or Oracle JDK)
- **MongoDB** (running on localhost:27017)
- **Git** (for cloning the repository)

### **System Requirements**
- **RAM**: Minimum 4GB, Recommended 8GB+
- **Storage**: 2GB free space
- **Network**: Internet connection for dependencies

## 🛠️ Installation Steps

### **Step 1: Clone Repository**
```bash
# Clone the repository
git clone https://github.com/yehia2030/MomProjBackend.git
cd MomProjBackend

# Make gradlew executable
chmod +x gradlew
```

### **Step 2: Install MongoDB**

#### **macOS (Homebrew)**
```bash
# Install MongoDB
brew tap mongodb/brew
brew install mongodb-community

# Start MongoDB service
brew services start mongodb-community
```

#### **Ubuntu/Debian**
```bash
# Import MongoDB public key
wget -qO - https://www.mongodb.org/static/pgp/server-7.0.asc | sudo apt-key add -

# Add MongoDB repository
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list

# Install MongoDB
sudo apt-get update
sudo apt-get install -y mongodb-org

# Start MongoDB service
sudo systemctl start mongod
sudo systemctl enable mongod
```

#### **Windows**
1. Download MongoDB Community Server from [mongodb.com](https://www.mongodb.com/try/download/community)
2. Run the installer with default settings
3. Start MongoDB service from Services.msc

### **Step 3: Build Project**
```bash
# Build the project (skip tests for faster build)
./gradlew build -x test
```

### **Step 4: Seed Database (Recommended)**
```bash
# Populate database with test data
./gradlew run --args="--seed"
```

### **Step 5: Start Server**
```bash
# Start the Ktor server
./gradlew run
```

### **Step 6: Verify Setup**
```bash
# Test server is running
curl http://localhost:8080/

# Expected response: "Hello World!"
```

## 🔧 Configuration

### **Environment Variables**
```bash
# Optional: Set custom port (default: 8080)
export SERVER_PORT=8080

# Optional: Set custom MongoDB connection
export MONGODB_URI=mongodb://localhost:27017

# Optional: Set database name
export DATABASE_NAME=momcare_platform
```

### **Database Configuration**
- **Database Name**: `momcare_platform`
- **Collections**: Automatically created on first run
- **Seeding**: Run with `--seed` flag to populate test data

## 🧪 Test Accounts

### **Mom Test Accounts**
| Email | Password | Name | Phone | Status |
|-------|----------|------|-------|--------|
| jane.doe@example.com | password123 | Jane Doe | +1-555-0101 | MARRIED |
| sarah.smith@example.com | password123 | Sarah Smith | +1-555-0102 | SINGLE |
| maria.garcia@example.com | password123 | Maria Garcia | +1-555-0103 | MARRIED |

### **Doctor Test Accounts**
| Email | Password | Name | Phone | Specialization |
|-------|----------|------|-------|----------------|
| dr.john.smith@example.com | password123 | Dr. John Smith | +1-555-0201 | GENERAL_MEDICINE |
| dr.emily.johnson@example.com | password123 | Dr. Emily Johnson | +1-555-0202 | PEDIATRICS |
| dr.michael.brown@example.com | password123 | Dr. Michael Brown | +1-555-0203 | OBSTETRICS_GYNECOLOGY |

## 🔑 Quick Login Commands

### **Login as Mom**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@example.com",
    "password": "password123"
  }'
```

### **Login as Doctor**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dr.john.smith@example.com",
    "password": "password123"
  }'
```

## 📊 Available Test Data

### **Categories (3)**
- **Prenatal Care** - Products for expecting mothers
- **Postnatal Care** - Products for new mothers
- **Baby Care** - Products for infants

### **Products (15)**
- Prenatal Vitamins, Maternity Clothes, Baby Formula
- Diapers, Baby Bottles, Nursing Bras
- And 10 more products with full details

### **SKU Offers (25)**
- Multiple offers per product with different prices
- Various sellers with competitive pricing
- Active and inactive offers for testing

### **Doctors (4)**
- 3 Authorized doctors (available to moms)
- 1 Pending authorization doctor

## 🔍 API Testing Tools

### **Swagger UI**
- **URL**: http://localhost:8080/swagger
- **Features**: Interactive API documentation and testing

### **OpenAPI Documentation**
- **URL**: http://localhost:8080/openapi
- **Features**: Raw OpenAPI specification

### **Postman Collection**
- **File**: `postman_collection.json` in project root
- **Features**: Pre-configured requests for all endpoints

## 🚨 Troubleshooting

### **Common Issues**

#### **Port 8080 Already in Use**
```bash
# Kill process using port 8080
lsof -ti:8080 | xargs kill -9

# Or use different port
export SERVER_PORT=8081
./gradlew run
```

#### **MongoDB Connection Failed**
```bash
# Check if MongoDB is running
brew services list | grep mongodb  # macOS
sudo systemctl status mongod       # Linux

# Start MongoDB if not running
brew services start mongodb-community  # macOS
sudo systemctl start mongod            # Linux
```

#### **Build Failures**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build -x test
```

#### **Permission Denied (gradlew)**
```bash
# Make gradlew executable
chmod +x gradlew
```

### **Logs and Debugging**
```bash
# Run with debug logging
./gradlew run --debug

# Check application logs
tail -f logs/application.log
```

## 📱 KMP Integration

### **Base URL**
```
http://localhost:8080
```

### **Authentication**
All protected endpoints require JWT authentication:
```http
Authorization: Bearer <jwt_token>
```

### **Response Format**
All API responses follow this format:
```json
{
  "successful": true,
  "data": { ... },
  "message": "Success message"
}
```

## 🎯 Next Steps

1. **Start the server** using the steps above
2. **Test authentication** with the provided test accounts
3. **Explore the API** using Swagger UI
4. **Begin KMP development** using the API endpoints
5. **Refer to** [ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md) for detailed API specifications

## 📞 Support

If you encounter any issues:
1. Check the troubleshooting section above
2. Review the logs for error messages
3. Ensure all prerequisites are installed correctly
4. Verify MongoDB is running and accessible

---

**Happy coding! 🚀**
