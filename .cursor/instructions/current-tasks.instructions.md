---
applyTo: '**'
---
# 🚧 Current Tasks - Mom Care Platform

## 📚 Related Documentation

- **[main.instructions.md](./main.instructions.md)** - Complete API documentation and technical reference
- **[configuration.instructions.md](./configuration.instructions.md)** - Authentication configuration, token settings, and environment setup
- **[completed-tasks.instructions.md](./completed-tasks.instructions.md)** - Implementation status and achievements
- **[completed-development-phases.instructions.md](./completed-development-phases.instructions.md)** - Historical development phases and audit
- **[project-summary.instructions.md](./project-summary.instructions.md)** - Complete project overview and testing
- **[ktor-kmp-integration.instructions.md](./ktor-kmp-integration.instructions.md)** - Mobile app integration guide
- **[acceptance-criteria.instructions.md](./acceptance-criteria.instructions.md)** - Feature implementation standards and quality gates
- **[stories-acceptance-criteria.instructions.md](./stories-acceptance-criteria.instructions.md)** - User stories acceptance criteria for mom features, circle management, points system, reviews, and payment processing

## 🎯 Current Implementation Status

**Current Status**: ✅ **CORE FOUNDATION + PROFILE MANAGEMENT + PRODUCT CATALOG + SKU OFFERS + SHOPPING CART + MOM AUTHORIZATION + ADMIN AUTHORIZATION SYSTEM + API DOCUMENTATION COMPLETE + API RESPONSE PATTERN OPTIMIZATION COMPLETE** - Authentication, Categories, File Upload, Profile Management with Authorization, Product Catalog, SKU Offers, Shopping Cart, **Complete Admin Authorization System**, **Fully Consistent API Documentation** (Swagger/Postman), and **Optimized API Response Patterns** all operational with secure three-tier access (Public/Mom/Admin)

**Latest Completed**: ✅ **ORDER MANAGEMENT API COMPLETE** - Full implementation with 5 endpoints, cart integration, comprehensive testing (15 unit tests + integration tests), complete documentation (Swagger + Postman), all acceptance criteria met

**Next Phase**: 🚀 **PAYMENT PROCESSING & REVIEW SYSTEM** - Ready to implement payment integration and product review system
## 📋 Tasks to Complete Tomorrow (High Priority)



**🔍 CRITICAL Debugging & Logging**:
- [ ] **[DEBUG-002] Authorization Flow Tracing**: Add detailed logging to MomAuthUtil and DoctorAuthUtil to trace authorization decision process
- [ ] **[DEBUG-003] JWT Token Validation Logging**: Log token parsing, claims extraction, and validation results

**🟠 HIGH Priority Tests (REMAINING)**:
- [ ] **[TEST-010] Mom-only ProductRoutes Tests**: GET endpoints with mom authorization (8+ sessions)
- [ ] **[TEST-011] Mom-only SkuOfferRoutes Tests**: GET endpoints with mom authorization  
- [ ] **[TEST-012] Mom Authorization Edge Cases**: Unauthorized moms, session count validation

**🔐 HIGH Priority Authentication Features**:
- [x] **[AUTH-001] Logout API Implementation**: ✅ **COMPLETED** - Logout endpoint fully implemented, documented, and tested
  - **Endpoint**: `POST /api/auth/logout` - ✅ Implemented and working
  - **Authentication**: Uses refresh token in request body - ✅ Secure
  - **Functionality**: Removes refresh token from database - ✅ Working
  - **Response**: Standard success response - ✅ Consistent
  - **Security**: Immediate token revocation - ✅ Secure
  - **Testing**: Unit and integration tests - ✅ All passing
  - **Documentation**: Swagger/OpenAPI and Postman - ✅ Complete


**🔧 MEDIUM Priority Code Improvements**:
- [ ] **[TASK-003] Cache Validation Logic**: Refactor MomAuthCacheEntry validation logic - Remove comments and rename to createValidatedCacheEntry for better clarity
- [ ] **[TASK-004] AuthInterceptor Deprecation Fix**: Refactor AuthInterceptor to use route-scoped plugins instead of deprecated `Route.intercept`
- [ ] **[TASK-006] Category Data Consistency Fix**: Fix category creation and retrieval data inconsistency - unify response format between creation and getting categories to ensure consistent API responses
- [ ] **[TASK-008] Enhanced Request/Response Logging**: Add comprehensive logging for all requests and responses to debug authorization issues - log JWT tokens, user claims, authorization status, and response details

**🤖 HIGH Priority Test Automation (ACTIVE)**:
- [ ] **[AUTO-006] Doctor API Tests**: Doctor profile GET/PUT endpoints with authentication
- [ ] **[AUTO-007] Security Tests**: Unauthorized access attempts, token edge cases, CORS validation

**📁 Test Suite Structure**:
```bash
# Master Orchestrator
./tests/main/test-api-comprehensive.sh              # Runs all tests with comprehensive reporting

# Modular Test Suites
./tests/test-common.sh                   # Shared utilities and helper functions
./tests/test-auth.sh          ✅         # Authentication & registration flows
./tests/test-public.sh        ✅         # Public APIs (categories)
./tests/test-admin.sh         ✅         # Admin CRUD operations
./tests/test-mom.sh           ✅         # Mom-only APIs (COMPLETED)
./tests/test-doctor.sh        🚧         # Doctor profile APIs (TODO)
./tests/test-security.sh      🚧         # Security & edge cases (TODO)

# Usage Examples
./tests/main/test-api-comprehensive.sh              # Run all tests
./tests/main/test-api-comprehensive.sh --auth-only  # Run only auth tests
./tests/main/test-api-comprehensive.sh --admin-only # Run only admin tests
```

**🔐 Proposed Endpoint Structure**:
```bash
# ADMIN-ONLY ENDPOINTS (NEW)
POST /api/admin/products [ADMIN AUTH] - Create product
PUT /api/admin/products/{id} [ADMIN AUTH] - Update product  
DELETE /api/admin/products/{id} [ADMIN AUTH] - Delete product
POST /api/admin/categories [ADMIN AUTH] - Create category
PUT /api/admin/categories/{id} [ADMIN AUTH] - Update category
DELETE /api/admin/categories/{id} [ADMIN AUTH] - Delete category
POST /api/admin/sku-offers [ADMIN AUTH] - Create SKU offer
PUT /api/admin/sku-offers/{id} [ADMIN AUTH] - Update SKU offer
DELETE /api/admin/sku-offers/{id} [ADMIN AUTH] - Delete SKU offer

# MOM-ONLY ENDPOINTS (EXISTING - READ ONLY)
GET /api/products [MOM AUTH + AUTHORIZED] - Browse products
GET /api/products/{id} [MOM AUTH + AUTHORIZED] - View product details
GET /api/products/category/{categoryId} [MOM AUTH + AUTHORIZED] - Products by category
GET /api/products/search [MOM AUTH + AUTHORIZED] - Search products
GET /api/sku-offers [MOM AUTH + AUTHORIZED] - Browse offers
GET /api/sku-offers/{id} [MOM AUTH + AUTHORIZED] - View offer details
# ... other SKU offer GET endpoints

# PUBLIC ENDPOINTS (NO AUTH)
GET /api/categories - Browse categories
GET /api/categories/{id} - View category details
```

**⚠️ Security Impact**: 
- **HIGH PRIORITY**: Current implementation is a security risk as it allows moms to modify product catalog
- **Data Integrity**: Prevents unauthorized modifications to business-critical data
- **Role Separation**: Proper separation of admin vs customer roles


### 🛒 **Priority 1: Order Management API (HIGH) - ✅ COMPLETED**
**Status**: ✅ **FULLY IMPLEMENTED** - All endpoints, testing, and documentation complete

**Completed Implementation**:
- [x] **[AUTH-001] Logout API**: ✅ **COMPLETED** - `POST /api/auth/logout` - Revoke refresh tokens and logout users
- [x] **[ORDER-001]** `GET /api/orders` [Auth + Mom Authorization Required] - ✅ **COMPLETED** - Get user's order history with pagination
- [x] **[ORDER-002]** `GET /api/orders/{id}` [Auth + Mom Authorization Required] - ✅ **COMPLETED** - Get specific order with access control
- [x] **[ORDER-003]** `POST /api/orders` [Auth + Mom Authorization Required] - ✅ **COMPLETED** - Create order from request items
- [x] **[ORDER-004]** `POST /api/orders/from-cart` [Auth + Mom Authorization Required] - ✅ **COMPLETED** - Create order from cart
- [x] **[ORDER-005]** `PUT /api/orders/{id}/status` [Auth + Mom Authorization Required] - ✅ **COMPLETED** - Update order status

**Implementation Details**:
- ✅ OrderService with complete business logic
- ✅ OrderRoutes with all 5 endpoints
- ✅ Cart system integration for seamless order creation
- ✅ SKU and SkuOffer repository integration
- ✅ Comprehensive unit tests (15 test cases)
- ✅ Integration tests with authentication and authorization
- ✅ Complete Swagger/OpenAPI documentation
- ✅ Postman collection with all endpoints
- ✅ Mom authorization (8+ sessions) for all endpoints
- ✅ Proper access control and error handling

## 📅 Secondary Tasks (Lower Priority)

### 📝 **Phase 2: Review System (MEDIUM)**
**Required for Complete E-commerce**:
- [ ] **[REVIEW-001]** `GET /api/products/{id}/reviews` [Public] - Get product reviews
- [ ] **[REVIEW-002]** `POST /api/products/{id}/reviews` [Mom Auth Required] - Add review
- [ ] **[REVIEW-003]** `PUT /api/reviews/{id}` [Mom Auth Required] - Update review
- [ ] **[REVIEW-004]** `DELETE /api/reviews/{id}` [Mom Auth Required] - Delete review

### 📦 **Phase 3: Inventory & Seller Management (ADMIN-ONLY)**
**Administrative Features**:
- [ ] **[ADMIN-001]** `GET /api/admin/inventory` [Admin Auth Required] - View inventory levels
- [ ] **[ADMIN-002]** `PUT /api/admin/inventory/{skuId}` [Admin Auth Required] - Update stock
- [ ] **[ADMIN-003]** `GET /api/admin/sellers` [Admin Auth Required] - List sellers
- [ ] **[ADMIN-004]** `POST /api/admin/sellers` [Admin Auth Required] - Add new seller
- [ ] **[ADMIN-005]** Seller-specific endpoints for inventory management

## 🔧 Implementation Guidelines

### **Development Approach**
1. **Service-First**: Create service classes before routes
2. **Test-Driven**: Add unit tests for each new service
3. **JWT Integration**: All protected endpoints require authentication
4. **Validation**: Input validation at service layer
5. **Error Handling**: Consistent error responses using `BasicApiResponse`

### **Code Standards**
- Follow existing patterns in `AuthService.kt` and `CategoryService.kt`
- No comments in code (see `.github/instructions/no-comments.instructions.md`)
- Use repository pattern for data access
- Implement proper dependency injection with Koin

### **AuthInterceptor Deprecation Fix (TASK-004)**
**Current Issue**: `Route.intercept` is deprecated in Ktor and shows deprecation warnings
**Solution**: Replace with route-scoped plugins

**Implementation Steps**:
1. Create `VerifyUser` route-scoped plugin in `AuthInterceptor.kt`
2. Update route definitions to use `install(VerifyUser)` instead of `intercept`
3. Test authorization functionality remains intact
4. Remove deprecated `intercept` calls
5. Update unit tests if needed

**Proposed Plugin Structure**:
```kotlin
val VerifyUser = createRouteScopedPlugin("VerifyUser") { 
    onCall { call -> 
        // auth logic...
        if (!authorized) { 
            call.respond(HttpStatusCode.Unauthorized, ...)
            finish() 
        } 
    } 
}

// Usage in routes:
route("/mom") { 
    authenticate { 
        install(VerifyUser) 
        // protected endpoints here 
    } 
}
```

**Files to Modify**:
- `src/main/kotlin/auth/AuthInterceptor.kt`
- Route files that use the interceptor
- Unit tests for authorization


### **File Structure for New Features**
```
src/main/kotlin/
├── service/
│   ├── ProductService.kt     # NEW - Product business logic
│   ├── MomService.kt         # EXTEND - Add profile management
│   ├── DoctorService.kt      # EXTEND - Add profile management
│   └── CartService.kt        # NEW - Shopping cart logic
├── routes/
│   ├── ProductRoutes.kt      # NEW - Product API endpoints
│   ├── MomRoutes.kt          # EXTEND - Profile endpoints
│   ├── DoctorRoutes.kt       # EXTEND - Profile endpoints
│   └── CartRoutes.kt         # NEW - Cart API endpoints
└── data/requests/
    ├── ProductRequests.kt    # NEW - Product DTOs
    ├── ProfileRequests.kt    # NEW - Profile update DTOs
    └── CartRequests.kt       # NEW - Cart operation DTOs
```

## 📊 Current Foundation Assets

### **✅ What's Already Working**
- **Complete E-Commerce Platform**: Products, SKU Offers, Shopping Cart, Mom Authorization
- **Authentication System**: JWT tokens with refresh token mechanism (30-min access, 30-day refresh)
- **User Management**: Moms, Doctors, Categories with full profile management
- **Database**: MongoDB with comprehensive seeded test data
- **Testing**: 59 tests passing across 9 test suites
- **Documentation**: Complete Swagger + Postman collection (40+ endpoints)

### **🗄️ Available Database Collections**
- **Complete E-Commerce Data**: Products, SKU offers, categories, carts, orders, inventory
- **User Data**: Moms (with authorization status), doctors, admins (to be added)
- **Authentication**: JWT refresh tokens with idle timeout tracking

## 🚀 Success Criteria for Tomorrow


### **Next Development Phase**
- [x] **Order Management**: ✅ **COMPLETED** - Full order management system with cart integration
- [ ] **Payment Processing**: Implement payment integration and processing
- [ ] **Review System**: Product review and rating system
- [ ] **Admin Endpoints**: Complete business management functionality

## 💡 Implementation Tips

### **Leverage Existing Code**
- **Copy patterns** from `CategoryService.kt` and `CategoryRoutes.kt`
- **Reuse authentication** middleware from existing protected routes
- **Follow validation** patterns from `MomService.kt`
- **Use seeded data** for realistic testing scenarios

### **Quick Wins**
1. **Start with Products**: Models exist, just need service + routes
2. **Profile Management**: Authentication already handles user resolution
3. **Copy-Paste Strategy**: Existing code provides excellent templates

### **🔐 Refresh Token Implementation**
**New Authentication Endpoints:**
- `POST /api/auth/refresh` - Get new access token using refresh token
- `POST /api/auth/logout` - Revoke refresh token to logout user

**Security Features:**
- **Access Token**: Configurable expiry (default: 30 minutes / 1800 seconds)
- **Refresh Token**: Configurable expiry (default: 30 days / 2592000 seconds)
- **Idle Timeout**: Configurable idle timeout (default: 24 hours / 86400 seconds)
- **Token Rotation**: New refresh token generated on each refresh
- **Secure Storage**: Refresh tokens stored in MongoDB with revocation support

**Configuration (application.yaml):**
```yaml
auth:
  accessTokenExpiryMinutes: 30    # Access token expiry in minutes
  refreshTokenExpiryDays: 30      # Refresh token expiry in days
  idleTimeoutHours: 24           # Idle timeout in hours
```

**Response Format (Login/Registration/Refresh):**
```json
{
  "success": true,
  "data": {
    "userId": "mom_alice",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "MJCvRLCw7vw_lo2PU3KCN4zrHB1BfT_X86SW63zLWuk",
    "userType": "MOM",
    "expiresIn": 1800,
    "refreshExpiresIn": 2592000
  }
}
```

### **Testing Strategy**
- **Use seeded accounts**: alice@example.com, beth@example.com (password: password123)
- **Test session validation**: Alice (3 sessions) vs Beth (8 sessions)
- **Verify JWT claims**: userType and userId in tokens
- **Test refresh flow**: Login → Wait → Refresh → Verify new tokens
- **Test idle timeout**: Login → Wait 24+ hours → Refresh should fail
- **Test logout**: Login → Logout → Refresh should fail
- **Test configuration**: Modify application.yaml auth settings and verify behavior changes
- **Unit test coverage**: Run `./gradlew test` to verify 11 test suites pass including TransactionService

---

## 📖 Stories Acceptance Criteria Reference

**📋 Next Development Phase**: Refer to **[stories-acceptance-criteria.instructions.md](./stories-acceptance-criteria.instructions.md)** for detailed acceptance criteria for upcoming features:

### 🎯 **Priority 1: Step-by-Step Mom Registration**
- **4-Step Registration Flow**: Basic Info → Location & Demographics → Socioeconomic → Language & Preferences
- **Sequential Validation**: Cannot proceed without completing current step
- **Profile Completion Tracking**: Track progress and completion percentage
- **App Functionality Blocking**: Block full features until registration complete
- **Light Dashboard Access**: Allow limited access with completion banner
- **Registration Resume**: Resume from where mom left off
- **UI/UX Insights**: Progress indicators, step navigation, completion status

### 🎯 **Priority 2: Circle/Cluster Management System**
- Location-based circle creation and matching
- Smart matching algorithm (location → age → socioeconomic → language)
- Virtual circles (1-4 members) vs Physical circles (5+ members)
- Circle discovery and member management

### 🎯 **Priority 3: Points & Authorization System**
- Points earning through circle attendance and reviews
- 120 points threshold for `isAuthorized = true`
- Admin points management and penalty system
- E-commerce feature unlocking

### 🎯 **Priority 4: Review System**
- Circle review system for session completion
- Mom-to-mom review system
- Public review display (3 reviews for moms/doctors)
- Review moderation and privacy controls

### 🎯 **Priority 5: Payment Processing System**
- Multiple payment methods integration
- Secure payment processing
- Payment history and refund processing
- E-commerce payment completion

**📚 Implementation Standards**: All features must meet the criteria defined in **[acceptance-criteria.instructions.md](./acceptance-criteria.instructions.md)** including code quality, testing, documentation, security, and performance requirements.

---

**🚀 READY FOR MOM FEATURES & PAYMENT PROCESSING IMPLEMENTATION!**

*Current Status: Complete e-commerce platform with order management system, optimized API response patterns, proper security separation, comprehensive documentation, and logout API. All tests passing (59 unit + 6 integration + 15 order tests), Order Management API fully implemented and documented. Next priority: Mom Registration Enhancement, Circle Management, Points System, Review System, and Payment Processing implementation as defined in stories acceptance criteria.*
