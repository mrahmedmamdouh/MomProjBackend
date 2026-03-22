---
applyTo: '**'
---
# Mom Project - Ktor Backend API Documentation

## 📚 Related Documentation

This is the main API documentation file. For specific aspects of the project, refer to:

- **[current-tasks.instructions.md](./current-tasks.instructions.md)** - Current development tasks and implementation priorities
- **[completed-tasks.instructions.md](./completed-tasks.instructions.md)** - Implementation history and achievements
- **[project-summary.instructions.md](./project-summary.instructions.md)** - Complete project overview and testing instructions
- **[configuration.instructions.md](./configuration.instructions.md)** - Authentication configuration, token settings, and environment setup
- **[ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md)** - Kotlin Multiplatform mobile app integration guide
- **[transaction-atomicity.instructions.md](./transaction-atomicity.instructions.md)** - Database transaction architecture and ACID compliance

## Overview
This Ktor backend provides a comprehensive API for a mom-focused e-commerce platform with healthcare features. The system supports mom and doctor user management with unified authentication, product catalog, shopping cart, orders, payments, and inventory management.

**⚠️ Implementation Status Note**: This documentation includes both **implemented endpoints** (Authentication, Categories, Profile Management, Product Catalog, SKU Offers, Shopping Cart) and **planned endpoints** (Orders, Payments, Reviews). See [ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md) for accurate current implementation status.

**For complete implementation details and project status, see:** [completed-tasks.instructions.md](./completed-tasks.instructions.md)

## Architecture
- **Framework**: Ktor 3.2.3 with Kotlin 2.1.10
- **Database**: MongoDB with KMongo 4.11.0 and ACID transaction support
- **Architecture Pattern**: Repository Pattern with Service Layer
- **Dependency Injection**: Koin 3.5.6
- **Authentication**: JWT with unified login system
- **File Upload**: Multipart form data with secure file handling
- **Transaction Management**: Atomic operations with automatic rollback (see [transaction-atomicity.instructions.md](./transaction-atomicity.instructions.md))
- **Build Tool**: Gradle with Kotlin DSL

## 🔒 Enhanced Security & Access Control

### Role-Based Access Control (Recently Enhanced)
The API implements comprehensive role-based access control with strict user type validation:

#### **Moms (E-commerce Customers)**
- ⚠️ **E-commerce READ Access**: Should only browse products, categories, SKU offers (READ-ONLY)
- ✅ **Healthcare Access**: Can view authorized doctors for consultation via `/api/doctors/authorized`
- ✅ **Profile Management**: Can manage their own profile and session data
- ✅ **Shopping Cart**: Complete cart operations with security validation
- ⚠️ **SECURITY ISSUE**: Currently has CRUD access to products/categories - needs admin refactoring

#### **Doctors (Healthcare Providers)**
- ✅ **Profile Management**: Can manage their own profile and authorization status
- ❌ **E-commerce Restricted**: Cannot access products, categories, SKU offers, or shopping cart
- ❌ **No Order Access**: Cannot place orders or access e-commerce features
- ❌ **No Category Access**: Cannot view or manage product categories

### Security Fixes & Validation
- ✅ **Required Field Validation**: Critical fields are properly validated to prevent silent defaults
- ✅ **Malformed Request Rejection**: Empty or incomplete requests are rejected with 400 Bad Request
- ✅ **Session Reset Prevention**: Malformed session update requests cannot accidentally reset mom sessions to 0
- ✅ **Input Sanitization**: All user input is validated and sanitized at the service layer

#### **Admins (Business Managers) - TODO: IMPLEMENT**
- 🔄 **Full Business Control**: Complete CRUD access to products, categories, SKU offers
- 🔄 **Order Management**: View and update order statuses
- 🔄 **User Management**: Manage mom/doctor authorization status
- 🔄 **Inventory Control**: Manage stock levels and pricing
- 🔄 **Separate Endpoints**: Should use `/api/admin/*` routes for security separation

#### **Unauthenticated Users**
- ❌ **No Access**: Cannot access any user data or e-commerce features
- ✅ **Public Access**: Only authentication endpoints (login, registration) and category browsing

### Enhanced Security Features
- **JWT Token Validation**: All protected endpoints require valid JWT tokens
- **User Type Verification**: Endpoints verify `userType` claim in JWT tokens with strict validation
- **Role-Based Endpoint Protection**: Each endpoint validates appropriate user type access
- **Consistent Error Handling**: Standardized unauthorized access responses using `Constants.UNAUTHORIZED_ACCESS`
- **No Public User Data**: All user information requires authentication
- **E-commerce Isolation**: Doctors are completely isolated from e-commerce features
- **Response Format Standardization**: All responses use consistent `BasicApiResponse` format with `success` field

## Database Collections

### Core Authentication & User Management
1. **Users** - Central authentication table with email/password and user type mapping
   - Fields: email, password (hashed), userType, momId?, doctorId?
   - Purpose: Unified login system for all user types

2. **Moms** - Mom user profiles with healthcare tracking
   - Fields: authUid, fullName, email, phone, maritalStatus, numberOfSessions (default: 0), photoUrl
   - Purpose: Mom-specific profile data and session tracking

3. **Doctors** - Healthcare provider profiles
   - Fields: authUid, name, email, phone, specialization, isAuthorized, photo
   - Purpose: Doctor profiles with specialization and authorization status

4. **NIDs** - National ID verification documents (file-based)
   - Fields: imageFront (file path), imageBack (file path), momId?, doctorId?
   - Purpose: Identity verification with secure file storage

### Product Catalog & E-commerce
5. **Categories** - Product categories (Mother Care, Fitness, Nutrition, etc.)
6. **Sellers** - Product vendors and suppliers
7. **Products** - Product catalog with session requirements
8. **SKUs** - Product variants and specifications
9. **SkuOffers** - Seller-specific pricing and availability
10. **Inventory** - Stock management and availability

### Shopping & Orders
11. **Carts** - Shopping cart management
12. **Orders** - Order processing and tracking
13. **Payments** - Payment processing and history
14. **ProductRatings** - Product reviews and ratings

### Authentication Mapping Tables
15. **MomAuth** - Essential mapping between authUid and mom profiles
   - Fields: uid (authUid), momId
   - Purpose: Links authentication tokens to specific mom records
16. **DoctorAuth** - Essential mapping between authUid and doctor profiles
   - Fields: uid (authUid), doctorId
   - Purpose: Links authentication tokens to specific doctor records

## API Endpoints

### 🔐 Unified Authentication System

#### Registration (Multipart Form Data)
```bash
#### Mom Registration with File Uploads
```bash
POST /api/auth/register/mom
Content-Type: multipart/form-data

Form Fields:
- data: JSON string containing:
  {
    "email": "jane.doe@example.com",
    "password": "securepassword123",
    "fullName": "Jane Doe",
    "phone": "+1-555-1234",
    "maritalStatus": "MARRIED"
  }
- photo: File (image - JPG, PNG, GIF)
- nidFront: File (image - JPG, PNG, GIF)
- nidBack: File (image - JPG, PNG, GIF)

Response: BasicApiResponse<Unit>
Status: 200 OK (success) | 400 Bad Request (validation error) | 409 Conflict (email exists)
```

# Doctor Registration with File Uploads  
POST /api/auth/register/doctor
Content-Type: multipart/form-data

Form Fields:
- data: JSON string containing:
  {
    "email": "dr.smith@example.com",
    "password": "securepassword123",
    "name": "Dr. John Smith",
    "phone": "+1-555-5678",
    "specialization": "GENERAL_MEDICINE"
  }
- nidFront: File (image - JPG, PNG, GIF)
- nidBack: File (image - JPG, PNG, GIF)

Response: BasicApiResponse<Unit>
Status: 200 OK (success) | 400 Bad Request (validation error) | 409 Conflict (email exists)
```

#### Universal Login
```bash
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}

Success Response:
{
  "successful": true,
  "data": {
    "userId": "mom_specific_id_or_doctor_specific_id",
    "token": "jwt_token_with_userType_and_userId_claims",
    "userType": "MOM"
  }
}

Error Response:
{
  "successful": false,
  "message": "Invalid credentials"
}
```

### 🔑 JWT Token Structure
```json
{
  "userId": "specific_mom_id_or_doctor_id",
  "userType": "MOM" | "DOCTOR", 
  "email": "user@example.com",
  "iss": "https://jwt-provider-domain/",
  "aud": "jwt-audience",
  "exp": 1725483600
}
```

### 📦 Product Catalog & E-commerce

#### Categories (Moms Only)
```bash
GET /api/categories [Auth Required - Moms Only]
# Get all product categories - only accessible by authenticated moms
Response: BasicApiResponse<List<Category>>

GET /api/categories/{id} [Auth Required - Moms Only]
# Get specific category by ID - only accessible by authenticated moms
Response: BasicApiResponse<Category>

POST /api/categories [Auth Required]
# Create new category
Request: CreateCategoryRequest
Response: BasicApiResponse<Unit>

PUT /api/categories/{id} [Auth Required]
# Update existing category  
Request: UpdateCategoryRequest
Response: BasicApiResponse<Unit>

DELETE /api/categories/{id} [Auth Required]
# Delete category
Response: BasicApiResponse<Unit>
```

#### Products (Moms Only)
```bash
GET /api/products?page=0&size=20 [Auth Required - Moms Only]
# Get paginated product list - only accessible by authenticated moms
Response: BasicApiResponse<List<Product>>

GET /api/products/{id} [Auth Required - Moms Only]
# Get product details by ID - only accessible by authenticated moms
Response: BasicApiResponse<Product>

GET /api/products/category/{categoryId}?page=0&size=20 [Auth Required - Moms Only]
# Get products by category (paginated) - only accessible by authenticated moms
Response: BasicApiResponse<List<Product>>

GET /api/products/search?query=keyword&page=0&size=20 [Auth Required - Moms Only]
# Search products by name (paginated) - only accessible by authenticated moms
Response: BasicApiResponse<List<Product>>

POST /api/products [Auth Required]
# Create new product
Request: CreateProductRequest
Response: BasicApiResponse<Unit>

PUT /api/products/{id} [Auth Required] 
# Update product details
Request: UpdateProductRequest
Response: BasicApiResponse<Unit>

DELETE /api/products/{id} [Auth Required]
# Delete product
Response: BasicApiResponse<Unit>
```

### 🛒 Shopping & Order Management
#### Shopping Cart (Moms Only)
```bash
GET /api/cart [Auth Required - Moms Only]
# Get current mom's shopping cart
Response: BasicApiResponse<Cart>

POST /api/cart/add [Auth Required - Moms Only]
# Add item to cart
Request: AddToCartRequest
Response: BasicApiResponse<Unit>

PUT /api/cart/item/{skuId} [Auth Required - Moms Only]
# Update cart item quantity
Request: UpdateCartItemRequest  
Response: BasicApiResponse<Unit>

DELETE /api/cart/item/{skuId} [Auth Required - Moms Only]
# Remove specific item from cart
Response: BasicApiResponse<Unit>

DELETE /api/cart [Auth Required - Moms Only]
# Clear entire cart
Response: BasicApiResponse<Unit>
```

#### Order Management
```bash
GET /api/orders [Auth Required]
# Get user's order history
Response: BasicApiResponse<List<Order>>

GET /api/orders/{id} [Auth Required]  
# Get specific order details
Response: BasicApiResponse<Order>

POST /api/orders [Auth Required]
# Create new order from cart
Request: CreateOrderRequest
Response: BasicApiResponse<OrderResponse>

PUT /api/orders/{id}/status [Auth Required]
# Update order status (admin/seller only)
Request: UpdateOrderStatusRequest
Response: BasicApiResponse<Unit>
```

### 👥 User Profile Management

#### Mom Profiles
```bash
GET /api/moms/check-authorization [Auth Required - Mom only]
# Check mom authorization status and session count
Response: BasicApiResponse<{isAuthorized: Boolean, momId: String}>

GET /api/moms/profile [Auth Required - Mom only]
# Get current mom's profile
Response: BasicApiResponse<MomResponse>

PUT /api/moms/profile [Auth Required - Mom only]
# Update mom profile (supports JSON and multipart with photo)
Request (JSON): UpdateMomRequest
Request (Multipart): data + photo file
Response: BasicApiResponse<Unit>

PUT /api/moms/profile/sessions [Auth Required - Mom only]
# Update session count
Request: UpdateSessionsRequest (numberOfSessions field is required - malformed requests rejected with 400)
Response: BasicApiResponse<Unit>
```

#### Doctor Profiles
```bash
GET /api/doctors/authorized [Auth Required - Moms Only]
# Get all authorized doctors - only accessible by authenticated moms
Response: BasicApiResponse<List<DoctorResponse>>

GET /api/doctors/profile [Auth Required - Doctor only]
# Get current doctor's profile
Response: BasicApiResponse<DoctorResponse>

PUT /api/doctors/profile [Auth Required - Doctor only]
# Update doctor profile (supports JSON and multipart with photo)
Request (JSON): UpdateDoctorRequest
Request (Multipart): data + photo file
Response: BasicApiResponse<Unit>
```

## 📁 File Upload System

### Directory Structure
```
uploads/
├── nids/
│   ├── mom_front_[uuid].jpg
│   ├── mom_back_[uuid].jpg  
│   ├── doctor_front_[uuid].jpg
│   └── doctor_back_[uuid].jpg
└── profiles/
    └── profile_[uuid].jpg
```

### File Upload Status
- ✅ **File Upload**: Images are successfully uploaded and saved
- ✅ **UUID Naming**: Files are saved with unique names to prevent conflicts
- ✅ **Directory Creation**: Upload directories are automatically created
- ✅ **File Validation**: File types and sizes are validated
- 🔄 **File Serving**: Static file serving endpoint added at `/uploads/*`
- 📝 **Access URLs**: Uploaded files can be accessed via `http://localhost:8080/uploads/profiles/filename.jpg`

### File Upload Security & Validation
- **Supported Formats**: JPG, PNG, GIF, JPEG
- **File Size Limits**: Configurable per endpoint (default: 10MB)
- **Unique Naming**: UUID-based naming prevents conflicts and overwrites
- **Path Validation**: Prevents directory traversal attacks
- **Directory Isolation**: Separate folders for different document types
- **Auto-Creation**: Upload directories created automatically on startup
- **Email Validation**: Files are disposed if email validation fails
- **Memory Management**: Proper disposal of multipart data to prevent memory leaks

### File Upload Implementation Details
- **Multipart Handling**: Uses Ktor's multipart content negotiation
- **Streaming Upload**: Efficient handling of large files
- **Error Handling**: Comprehensive validation and error responses
- **Cleanup**: Failed uploads are automatically cleaned up
- **Security Note**: Due to HTTP multipart processing, files may be temporarily processed even if validation fails, but are properly disposed

### File Upload Behavior
- **Email Validation**: Email existence is checked after multipart parsing
- **File Disposal**: Files are properly disposed if email validation fails
- **Memory Management**: All multipart data is cleaned up to prevent memory leaks
- **Simplified Approach**: Clean, straightforward validation without complex field ordering requirements

## 🔒 Security Implementation

### Password Security
- **Hashing Algorithm**: PBKDF2 with SHA-256
- **Salt Generation**: 32-byte random salt per password
- **Storage Format**: "hash:salt" string in database
- **Verification**: Secure comparison prevents timing attacks

### JWT Security
- **Algorithm**: HMAC256 for token signing
- **Claims**: userId, userType, email, standard JWT claims
- **Expiration**: Configurable token lifetime (default: 1 year)
- **Validation**: Comprehensive token validation on protected routes

### API Security
- **Input Validation**: All request data validated at service layer
- **Authorization**: Role-based access control using JWT userType
- **Error Handling**: Secure error messages without information leakage
- **CORS**: Configurable cross-origin resource sharing

## 💼 Business Logic Implementation

### Authentication Flow
1. **Registration Process**:
   - User submits multipart form (data + NID images)
   - Files validated and saved with UUID names
   - Password hashed with PBKDF2 + salt
   - Central User record created with type mapping
   - Type-specific profile created (Mom/Doctor)
   - Authentication mapping created (MomAuth/DoctorAuth) linking authUid to profile
   - NID document records created with file paths

2. **Login Process**:
   - Single endpoint handles all user types
   - Email lookup in central User table
   - Password verification with stored hash
   - JWT generated with userType and specific userId (from MomAuth/DoctorAuth mapping)
   - Token returned for API authorization

3. **Token Validation**:
   - JWT contains authUid and userType claims
   - Protected endpoints use MomAuth/DoctorAuth tables to resolve specific user profiles
   - Ensures secure mapping between authentication and user data

### Session-Based Product Access
- **Product Requirements**: Products have `minSessionsToPurchase` field
- **Mom Session Tracking**: Moms have `numberOfSessions` counter (default: 0)
- **Purchase Validation**: System checks session eligibility before purchase
- **Session Updates**: Moms can update session count via API

### File Management Lifecycle
- **Upload**: Files saved with security validation
- **Storage**: File paths stored in database, not file content
- **Access**: Controlled access through API endpoints
- **Cleanup**: Orphaned files can be identified and removed

## ⚙️ Configuration & Environment

### Application Configuration (application.yaml)
```yaml
ktor:
  deployment:
    port: 8080
    
jwt:
  domain: "https://jwt-provider-domain/"
  audience: "jwt-audience"
  realm: "ktor sample app"
  secret: "your-jwt-secret-key"
```

### MongoDB Configuration
- **Connection**: Default localhost:27017
- **Database**: "momproject" (configurable)
- **Collections**: Auto-created on first use
- **Indexes**: Configured for optimal query performance

### File Upload Settings
- **Base Directory**: `uploads/` (auto-created)
- **Max File Size**: 10MB (configurable)
- **Allowed Extensions**: jpg, jpeg, png, gif
- **Subdirectories**: nids/, profiles/ (auto-created)

## 🔧 API Testing Tools

### **Postman Collection**
- **File**: `postman_collection.json` - Complete API testing collection with 40+ endpoints
- **Usage**: Import into Postman for comprehensive API testing
- **⚠️ Important**: Keep this collection updated when API changes are made
- **Variables**: Set `base_url` to `http://localhost:8080` and `auth_token` after login

### **Proxyman Integration**
- **Purpose**: Primary HTTP debugging proxy for API testing and monitoring
- **Server URL**: `http://localhost:8080`
- **Key Features**: Request/response inspection, SSL debugging, API documentation
- **Usage**: Always keep Proxyman updated with latest API changes during development

## 🧪 API Testing Examples

### Testing Authentication

#### Register Mom with File Uploads
```bash
curl -X POST http://localhost:8080/api/auth/register/mom \
  -F 'data={"email":"mom@example.com","password":"password123","fullName":"Jane Doe","phone":"+1234567890","maritalStatus":"MARRIED","photoUrl":""}' \
  -F 'nidFront=@/path/to/nid_front.jpg' \
  -F 'nidBack=@/path/to/nid_back.jpg'
```

#### Register Doctor with File Uploads  
```bash
curl -X POST http://localhost:8080/api/auth/register/doctor \
  -F 'data={"email":"doctor@example.com","password":"password123","name":"Dr. John Smith","phone":"+1234567890","specialization":"GENERAL_MEDICINE"}' \
  -F 'nidFront=@/path/to/nid_front.jpg' \
  -F 'nidBack=@/path/to/nid_back.jpg'
```

#### Universal Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Testing Protected Endpoints
```bash
# Get Categories (Public)
curl -X GET http://localhost:8080/api/categories

# Create Category (Requires Auth)
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Category",
    "description": "Category description"
  }'

# Get Mom Profile (Mom Only)  
curl -X GET http://localhost:8080/api/moms/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🚀 Getting Started

### Prerequisites
1. **Java 21+**: Required for Ktor 3.2.3
2. **MongoDB**: Database server (local or cloud)
3. **Gradle**: Build tool (included via wrapper)

### Installation & Setup
```bash
# 1. Clone and navigate to project
cd /path/to/momproj

# 2. Make gradlew executable (if needed)
chmod +x gradlew

# 3. Build the project
./gradlew build

# 4. Start MongoDB (if local)
mongod --dbpath /path/to/your/db

# 5. Run the application
./gradlew run
```

### Verification
- **Application starts**: Look for "Application started" in logs
- **MongoDB connects**: Check connection logs
- **Upload directories**: `uploads/nids/` and `uploads/profiles/` created
- **API accessible**: `http://localhost:8080` responds
- **Health check**: `GET /` returns welcome message

## 🏗️ Project Structure Overview
```
src/main/kotlin/
├── data/
│   ├── models/           # Data models (User, Mom, Doctor, Product, etc.)
│   ├── repository/       # Repository interfaces & implementations  
│   ├── requests/         # API request DTOs
│   └── responses/        # API response DTOs
├── di/                   # Dependency injection (Koin modules)
├── routes/              # API route handlers (Auth, Mom, Category, etc.)  
├── service/             # Business logic layer
├── util/                # Utilities (FileUpload, Hashing, etc.)
├── Application.kt       # Main application entry point
├── Databases.kt         # MongoDB configuration
├── Routing.kt          # Route configuration
├── Security.kt         # JWT authentication setup
└── *.kt               # Other configuration files
```

## 🔧 Troubleshooting

### Common Issues & Solutions

#### Build Errors
```bash
# Permission denied for gradlew
chmod +x gradlew

# Compilation errors after changes
./gradlew clean build

# Dependencies not downloading  
./gradlew --refresh-dependencies build
```

#### Runtime Issues
```bash
# MongoDB connection refused
# Solution: Start MongoDB service
mongod --dbpath /your/db/path

# Port already in use
# Solution: Change port in application.yaml or kill process
lsof -ti:8080 | xargs kill -9

# File upload directory errors
# Solution: Check write permissions for uploads/ folder
mkdir -p uploads/nids uploads/profiles
chmod 755 uploads/
```

#### API Testing Issues
```bash
# JWT token expired
# Solution: Login again to get new token

# Multipart upload fails
# Solution: Check file exists and is readable
ls -la /path/to/your/file.jpg

# Invalid credentials
# Solution: Verify email/password and user exists
```

##  Additional Documentation

For specific implementation aspects, see:

- **[tasks.instructions.md](./tasks.instructions.md)** - Development roadmap and task management
- **[project-summary.instructions.md](./project-summary.instructions.md)** - Complete project overview and testing instructions
- **[ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md)** - Mobile app integration with code examples
- **[completed-tasks.instructions.md](./completed-tasks.instructions.md)** - Complete implementation history and achievements

---

**This unified authentication system provides a cleaner API structure while maintaining flexibility for different user types. The multipart file upload ensures efficient image handling and better security compared to base64 encoding.**
