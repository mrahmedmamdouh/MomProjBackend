
# Mom Care Platform Backend

## Project Status: ✅ Core Features + Enhanced Security Access Control + API Response Pattern Optimization Implemented & Production-Ready

The backend now has comprehensive authentication, profile management, categories, and file upload systems fully implemented and tested with enhanced security access control and optimized API response patterns. The system supports unified authentication, multipart registration, JWT-based profile management, session tracking, secure file handling, strict role-based access control, and clean API response structures.

✅ **Current Implementation**: 40+ working endpoints (Authentication + Profile Management + Categories + Product Catalog + SKU Offers + Shopping Cart + File Serving + Enhanced Security + Admin Operations + Mom Session Privacy + Optimized Response Patterns). Additional features planned for future development.

### 🎯 **Latest Updates (Completed)**
- ✅ **API Response Optimization**: Removed `isAuthorized` from profile endpoints for cleaner responses
- ✅ **Response Pattern Fixes**: Resolved double-nesting issues across all API endpoints
- ✅ **Documentation Updates**: Updated Swagger/OpenAPI and Postman collection
- ✅ **Test Coverage**: All 59 unit tests and 6 integration test suites passing
- ✅ **Pull Request**: PR #10 created and ready for review

## Quick Start

### Prerequisites
```bash
brew services start mongodb-community
```

### Build & Seed Database
```bash
cd /Users/yehia/KtorBackend/momproj
./gradlew build -x test
./gradlew runSeed
```

### Start Server
```bash
./gradlew run
# Server at: http://localhost:8080
```

### Test API

#### 🤖 Automated Testing (Recommended)
Run comprehensive test suite covering all endpoints:
```bash
./tests/main/test-api-comprehensive.sh
```
**Features**: 
- **Comprehensive Coverage**: Authentication, Registration, Authorization, CRUD operations, Error handling, Performance testing
- **Automated Scripts**: Individual test suites for different API categories (auth, public, mom, doctor, admin, security)
- **Real-time Results**: Color-coded output with detailed pass/fail reporting
- **Dependency Checking**: Automatic validation of required tools (curl, jq)
- **Server Health Checks**: Pre-flight validation of server availability
- **Modular Testing**: Run specific test suites with `--auth-only`, `--public-only`, `--admin-only` flags

#### 📮 Manual Testing with Postman
Import `postman_collection.json` in Postman (40+ working endpoints). Use seeded accounts:
- Alice: alice@example.com / password123 (3 sessions - Unauthorized for e-commerce)
- Beth: beth@example.com / password123 (8 sessions - Authorized for e-commerce)  
- Dr. Smith: dr.smith@example.com / password123

📖 **Detailed Testing Guide**: See `.github/instructions/testing-automation.instructions.md`
📊 **API Coverage Analysis**: See `.github/instructions/api-coverage-analysis.md` - Complete endpoint inventory and test coverage status

## ✅ Implemented Features

- **🔐 Authentication System**: Unified login for Moms and Doctors with JWT tokens
- **👩 Profile Management**: Complete CRUD operations for Mom and Doctor profiles
- **📁 File Upload**: Multipart registration and profile updates with photo uploads
- **🔒 Security**: JWT token-based authentication with PBKDF2 password hashing
- **⚙️ Admin Operations**: Doctor authorization management with real-time access control
- **📦 Category Management**: Full CRUD operations for product categories
- **🛍️ Product Catalog**: Complete product management with search and filtering
- **💰 SKU Offers**: Product pricing and offer management system
- **🛒 Shopping Cart**: Full cart operations with add, update, remove, and clear
- **🗄️ Database**: MongoDB integration with realistic seeded test data
- **📝 Documentation**: Comprehensive API docs and KMP integration guide
- **📊 Session Tracking**: Mom session management for purchase validation
- **🧪 Unit Testing**: Comprehensive test coverage for all services
- **🤖 Automated API Testing**: Complete cURL-based test automation with comprehensive coverage analysis (100% coverage - see api-coverage-analysis.md)
- **✅ Enhanced Validation**: Fixed phone validation bug - consistent digit count and format validation
- **🔧 Automation Scripts**: Comprehensive test orchestration with modular test suites and real-time reporting

## 🔒 Enhanced Security Access Control (Recently Implemented)

The platform now features comprehensive role-based access control with strict user type validation:

### **MOM Users (E-commerce Access)**
- ✅ **E-commerce Access**: Products, categories, SKU offers, shopping cart (requires 4+ sessions)
- ✅ **Profile Management**: Manage own profile and session data (always available)
- ✅ **Session Tracking**: Update session count to gain e-commerce access
- ✅ **Authorization Check**: Check current authorization status and session count
- ❌ **Unauthorized Moms**: Moms with <4 sessions cannot access e-commerce features (403 Forbidden)

### **DOCTOR Users (Healthcare Only)**
- ✅ **Profile Management**: Manage own profile and authorization status
- ✅ **Authorization Check**: Can check their own authorization status in real-time
- ❌ **E-commerce Restricted**: Cannot access products, categories, SKU offers, or shopping cart
- ❌ **No Order Access**: Cannot place orders or access e-commerce features

### **ADMIN Users (Administrative Access)**
- ✅ **Doctor Authorization Management**: Update doctor authorization status (authorize/revoke)
- ✅ **Authorization Status Check**: View any doctor's current authorization status
- ✅ **Real-time Access Control**: Changes take effect immediately

### **Security Features**
- **JWT Token Validation**: All protected endpoints require valid JWT tokens
- **User Type Verification**: Endpoints verify `userType` claim in JWT tokens
- **Role-Based Access**: Strict separation between MOM, DOCTOR, and ADMIN access levels
- **Real-time Authorization**: Doctor access checked against database on every request
- **Consistent Error Handling**: Standardized unauthorized access responses

## 📋 Planned Features (Not Yet Implemented)

- Order management and processing
- Payment processing and tracking
- Review and rating system
- Inventory management
- Advanced analytics and reporting

## Documentation

See `.github/instructions/` for full docs:
- [API Documentation](./.github/instructions/main.instructions.md)
- [Project Summary](./.github/instructions/project-summary.instructions.md)
- [Tasks & Roadmap](./.github/instructions/tasks.instructions.md)
- [KMP Integration Guide](./.github/instructions/kmp-integration.instructions.md)

## Success Criteria ✅

**Currently Working**:
- ✅ Registration and login for Moms and Doctors with consistent response format
- ✅ File uploads for photos and NIDs with smart validation
- ✅ JWT token generation and authentication
- ✅ Complete profile management with photo updates
- ✅ Session tracking and updates for Moms
- ✅ Doctor directory with authorization filtering
- ✅ Category management (full CRUD)
- ✅ Product catalog with search and filtering
- ✅ SKU offers and pricing management
- ✅ Shopping cart with full CRUD operations
- ✅ Database seeded with realistic test data
- ✅ API tested via Postman collection (40+ endpoints)
- ✅ Static file serving for uploaded images
- ✅ Comprehensive unit testing coverage (59 tests)
- ✅ **API Response Pattern Optimization**: Clean profile responses without `isAuthorized`
- ✅ **Response Structure Fixes**: Resolved double-nesting issues across all endpoints
- ✅ **Documentation Updates**: Swagger/OpenAPI and Postman collection updated
- ✅ **Pull Request Ready**: PR #10 created and ready for review

**Next Implementation Phases**:
- 🔄 Order management and processing
- 🔄 Payment processing and tracking
- 🔄 Review and rating functionality
- 🔄 Advanced analytics and reporting

## API Overview

### 🔐 Authentication (3 endpoints)
- `POST /api/auth/register/mom` - Multipart mom registration (returns JWT)
- `POST /api/auth/register/doctor` - Multipart doctor registration (returns JWT)
- `POST /api/auth/login` - Universal login (returns JWT)

### 👩 Mom Profile Management (4 endpoints)
- `GET /api/moms/check-authorization` - Check mom authorization status (MOM auth required) - **Returns `isAuthorized` field**
- `GET /api/moms/profile` - Get mom profile (auth required, available to all authenticated moms) - **Clean response without `isAuthorized`**
- `PUT /api/moms/profile` - Update profile with optional photo (auth required) - **Clean response without `isAuthorized`**
- `PUT /api/moms/sessions` - Update session count (auth required, affects e-commerce access)

### 👨‍⚕️ Doctor Profile Management (3 endpoints)
- `GET /api/doctors/check-authorization` - Check doctor authorization status (DOCTOR auth required) - **Returns `isAuthorized` field**
- `GET /api/doctors/profile` - Get doctor profile (DOCTOR auth required) - **Clean response without `isAuthorized`**
- `PUT /api/doctors/profile` - Update profile with optional photo (DOCTOR auth required) - **Clean response without `isAuthorized`**

### ⚙️ Admin Operations (3 endpoints)
- `PUT /api/admin/doctors/{doctorId}/authorize` - Update doctor authorization (ADMIN auth required)
- `GET /api/admin/doctors/{doctorId}/status` - Get doctor authorization status (ADMIN auth required) - **Returns `isAuthorized` field**
- `GET /api/admin/moms/{momId}/sessions` - Get mom session data for administrative monitoring (ADMIN auth required) - **Returns `isAuthorized` field**

### 📦 Categories (5 endpoints) - Authorized MOM Access Only
- `GET /api/categories` - Get all categories (Authorized MOM auth required - 4+ sessions)
- `GET /api/categories/{id}` - Get category by ID (Authorized MOM auth required - 4+ sessions)
- `POST /api/categories` - Create category (auth required)
- `PUT /api/categories/{id}` - Update category (auth required)
- `DELETE /api/categories/{id}` - Delete category (auth required)

### 🛍️ Product Catalog (6 endpoints) - Authorized MOM Access Only
- `GET /api/products` - Get all products with pagination (Authorized MOM auth required - 4+ sessions)
- `GET /api/products/{id}` - Get product by ID (Authorized MOM auth required - 4+ sessions)
- `GET /api/products/category/{categoryId}` - Get products by category (Authorized MOM auth required - 4+ sessions)
- `GET /api/products/search` - Search products by name (Authorized MOM auth required - 4+ sessions)
- `POST /api/products` - Create product (auth required)
- `PUT /api/products/{id}` - Update product (auth required)
- `DELETE /api/products/{id}` - Delete product (auth required)

### 💰 SKU Offers (7 endpoints) - Authorized MOM Access Only
- `GET /api/sku-offers` - Get all active offers (Authorized MOM auth required - 4+ sessions)
- `GET /api/sku-offers/{id}` - Get offer by ID (Authorized MOM auth required - 4+ sessions)
- `GET /api/sku-offers/sku/{skuId}` - Get offers by SKU ID (Authorized MOM auth required - 4+ sessions)
- `GET /api/sku-offers/seller/{sellerId}` - Get offers by seller ID (Authorized MOM auth required - 4+ sessions)
- `GET /api/sku-offers/sku/{skuId}/best` - Get best offer for SKU (Authorized MOM auth required - 4+ sessions)
- `POST /api/sku-offers` - Create offer (auth required)
- `PUT /api/sku-offers/{id}` - Update offer (auth required)
- `DELETE /api/sku-offers/{id}` - Delete offer (auth required)

### 🛒 Shopping Cart (5 endpoints) - Authorized MOM Access Only
- `GET /api/cart` - Get shopping cart (Authorized MOM auth required - 4+ sessions)
- `POST /api/cart/add` - Add item to cart (Authorized MOM auth required - 4+ sessions)
- `PUT /api/cart/item/{skuId}` - Update cart item quantity (Authorized MOM auth required - 4+ sessions)
- `DELETE /api/cart/item/{skuId}` - Remove item from cart (Authorized MOM auth required - 4+ sessions)
- `DELETE /api/cart` - Clear entire cart (Authorized MOM auth required - 4+ sessions)

### 📁 File Serving (1 endpoint)
- `GET /uploads/*` - Access uploaded files

## API Response Patterns

### Optimized Response Structure
The API now uses clean, consistent response patterns:

#### **Profile Endpoints (Clean Responses)**
- **Mom Profile** (`/api/moms/profile`): Returns profile data without `isAuthorized` field
- **Doctor Profile** (`/api/doctors/profile`): Returns profile data without `isAuthorized` field
- **Purpose**: Cleaner responses focused on profile information only

#### **Authorization Endpoints (Include Status)**
- **Mom Authorization Check** (`/api/moms/check-authorization`): Returns `isAuthorized` and `momId`
- **Doctor Authorization Check** (`/api/doctors/check-authorization`): Returns `isAuthorized` and `doctorId`
- **Admin Mom Sessions** (`/api/admin/moms/{id}/sessions`): Returns `isAuthorized` and session data
- **Admin Doctor Status** (`/api/admin/doctors/{id}/status`): Returns `isAuthorized` and status data
- **Purpose**: Dedicated endpoints for authorization status checks

#### **Benefits**
- ✅ **Cleaner Profile Responses**: Profile endpoints focus on profile data only
- ✅ **Dedicated Authorization**: Separate endpoints for authorization status
- ✅ **Consistent Patterns**: Standardized response structure across all endpoints
- ✅ **Better API Design**: Clear separation of concerns

## E-Commerce Authorization System

### Session-Based Access Control
The platform implements a session-based authorization system for e-commerce features:

- **Authorization Threshold**: Moms need 4+ sessions to access e-commerce features
- **E-commerce Features**: Products, categories, SKU offers, and shopping cart
- **Profile Access**: Always available to authenticated moms regardless of session count
- **Session Updates**: Moms can update their session count via `/api/moms/sessions`

### Authorization Flow
1. **Registration**: New moms start with 0 sessions (unauthorized)
2. **Session Updates**: Moms can update session count through the API
3. **Real-time Authorization**: Authorization status checked on every e-commerce request
4. **Access Control**: Unauthorized moms receive 403 Forbidden for e-commerce endpoints

### Example Authorization States
- **Alice (3 sessions)**: Can access profile, cannot access products/cart (403 Forbidden)
- **Beth (8 sessions)**: Can access all features including e-commerce (200 OK)

## Profile Management Features

### Mom Profiles
- **Profile Updates**: Name, phone, marital status with optional photo upload
- **Session Tracking**: Number of sessions for purchase eligibility
- **JWT Authentication**: Secure access with userType validation
- **Multipart Support**: Both JSON and multipart form data supported

### Doctor Profiles  
- **Profile Updates**: Name, phone, specialization with optional photo upload
- **Real-time Authorization**: Authorization status checked on every request
- **JWT Authentication**: Secure access with userType validation
- **Authorization Management**: Track and manage doctor authorization status

## Real-time Doctor Authorization Flow

### How It Works
1. **Registration**: Doctors register with `isAuthorized: false` by default
2. **Admin Authorization**: Admins can authorize/revoke doctor access via API
3. **Real-time Validation**: Every doctor request checks database authorization status
4. **Immediate Effect**: Authorization changes take effect on the next request

### Authorization Scenarios
- **Authorized Doctor**: Can access profile management and check authorization status
- **Unauthorized Doctor**: Receives 403 Forbidden on all doctor-specific endpoints
- **Revoked Access**: Previously authorized doctors immediately lose access when revoked

## 🤖 Automation Scripts

The project includes comprehensive automation scripts for testing and development:

### Test Scripts
- **`tests/main/test-api-comprehensive.sh`**: Master orchestrator that runs all test suites in logical order
- **`tests/test-auth.sh`**: Authentication and registration tests
- **`tests/test-public.sh`**: Public API endpoints (no auth required)
- **`tests/test-mom.sh`**: Mom-specific endpoints and authorization tests
- **`tests/test-doctor.sh`**: Doctor profile management tests
- **`tests/test-admin.sh`**: Admin operations and CRUD tests
- **`tests/test-security.sh`**: Security validation and edge case tests

### Script Features
- **Color-coded Output**: Real-time visual feedback with pass/fail indicators
- **Dependency Validation**: Automatic checking for required tools (curl, jq)
- **Server Health Checks**: Pre-flight validation of server availability
- **Modular Execution**: Run specific test suites with command-line flags
- **Comprehensive Reporting**: Detailed test results and coverage analysis
- **Error Handling**: Graceful failure handling with informative error messages

### Usage Examples
```bash
# Run all tests
./tests/main/test-api-comprehensive.sh

# Run only authentication tests
./tests/main/test-api-comprehensive.sh --auth-only

# Run only public API tests
./tests/main/test-api-comprehensive.sh --public-only

# Run only admin tests
./tests/main/test-api-comprehensive.sh --admin-only
```

## Next Steps

- **Order Management**: Implement order processing and tracking
- **Payment Integration**: Add payment processing capabilities
- **Review System**: Implement product rating and review functionality
- **Frontend Development**: Use KMP integration guide
- **Mobile App Integration**: Complete backend-to-mobile workflow
- **Production Deployment**: Deploy with current comprehensive foundation

---

**The Mom Care Platform backend with complete e-commerce functionality is operational and ready for extension! 🚀**

