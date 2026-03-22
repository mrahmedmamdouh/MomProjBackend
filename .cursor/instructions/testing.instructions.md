---
applyTo: '**'
---
# 🧪 Mom Care Platform - Testing Instructions & Standards

## 📚 Related Documentation

- **[current-tasks.instructions.md](./current-tasks.instructions.md)** - Current development tasks and testing priorities
- **[completed-tasks.instructions.md](./completed-tasks.instructions.md)** - Completed implementation and testing achievements
- **[main.instructions.md](./main.instructions.md)** - Complete API documentation and technical reference
- **[configuration.instructions.md](./configuration.instructions.md)** - Authentication configuration for testing

## 🎯 Testing Philosophy & Standards

### **Testing Approach**
- **Security-First Testing**: All authentication and authorization paths must be thoroughly tested
- **HTTP Status Code Compliance**: Proper 403/404/400 error handling verification
- **JWT Configuration Consistency**: Test JWT setup must match main application exactly
- **Clean Serialization**: Use Gson for request body serialization in all HTTP tests
- **Comprehensive Coverage**: Test all user roles (Public/Mom/Admin) and edge cases

### **Test Infrastructure Standards**
- **JWT Test Configuration**: Must include proper audience, issuer, and secret
- **Request Serialization**: Always use `gson.toJson(requestObject)` for HTTP request bodies
- **Error Code Mapping**: Verify proper HTTP status codes for different error types
- **Authentication Testing**: Test valid tokens, invalid tokens, expired tokens, and no tokens
- **Authorization Boundary Testing**: Verify role-based access control across all endpoints

## 🔧 Testing Infrastructure Setup

### **JWT Configuration for Tests**
```kotlin
private val jwtSecret = "secret" // Must match application.yaml
private val algorithm = Algorithm.HMAC256(jwtSecret)
private val gson = Gson() // For clean request serialization

private fun generateAdminToken(): String {
    return JWT.create()
        .withAudience("jwt-audience")
        .withIssuer("https://jwt-provider-domain/")
        .withClaim("userId", "admin_main")
        .withClaim("userType", "ADMIN")
        .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
        .sign(algorithm)
}

// Test module configuration
private fun Application.testModule() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(Authentication) {
        jwt {
            verifier(
                JWT.require(algorithm)
                    .withAudience("jwt-audience")
                    .withIssuer("https://jwt-provider-domain/")
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains("jwt-audience")) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
```

### **HTTP Request Testing Pattern**
```kotlin
@Test
fun `POST endpoint with proper serialization`() = testApplication {
    application { testModule() }
    
    val token = generateAdminToken()
    val request = CreateSomeRequest(
        field1 = "value1",
        field2 = "value2"
    )
    
    val response = client.post("/api/admin/endpoint") {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(gson.toJson(request)) // Always use gson.toJson()
    }
    
    assertEquals(HttpStatusCode.OK, response.status)
}
```

## ✅ Completed Testing Infrastructure

### **AdminRoutesTest (11/11 Tests Passing)**
**Status**: ✅ **FULLY OPERATIONAL**

**Test Coverage**:
- ✅ Admin Category CRUD operations with proper authentication
- ✅ Admin Product CRUD operations with proper authentication  
- ✅ Admin SKU Offer CRUD operations with proper authentication
- ✅ Authorization boundary testing (admin vs mom vs no-token)
- ✅ HTTP status code verification (200 OK, 401 Unauthorized)
- ✅ Request body serialization using Gson
- ✅ JWT token validation with proper audience/issuer

**Technical Implementation**:
- ✅ **JWT Configuration**: Fixed audience, issuer, and secret alignment
- ✅ **Gson Serialization**: Clean `gson.toJson()` approach for all request bodies
- ✅ **Authentication Testing**: Comprehensive admin vs mom vs no-token scenarios
- ✅ **HTTP Status Verification**: Proper 200/401 status code validation

### **HTTP Status Code Mapping (Completed)**
**Status**: ✅ **IMPLEMENTED ACROSS ALL ROUTES**

**HttpStatusMapper Utility**:
```kotlin
object HttpStatusMapper {
    fun <T> mapErrorToHttpStatus(result: BasicApiResponse<T>): HttpStatusCode {
        return when {
            result.success -> HttpStatusCode.OK
            result.message?.contains("Access denied", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            result.message?.contains("not found", ignoreCase = true) == true -> HttpStatusCode.NotFound
            result.message?.contains("not authorized", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            result.message?.contains("unauthorized", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            result.message?.contains("already exists", ignoreCase = true) == true -> HttpStatusCode.Conflict
            result.message?.contains("invalid", ignoreCase = true) == true -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.BadRequest
        }
    }
}
```

**Applied To**:
- ✅ **CartRoutes**: Proper 403 for authorization failures (was returning 400)
- ✅ **ProductRoutes**: Proper 404 for "Mom not found" (was returning 403)
- ✅ **SkuOfferRoutes**: Proper 404 for "Mom not found" (was returning 403)

## 🔴 Critical Tests Still Needed

### **AdminDoctorRoutes Tests (MISSING - CRITICAL)**
**Priority**: 🔴 **HIGHEST** - Completely missing test coverage

**Required Tests**:
- [ ] `PUT /api/admin/doctors/{id}/authorize` with admin token → 200 OK
- [ ] `PUT /api/admin/doctors/{id}/authorize` with mom token → 401 Unauthorized
- [ ] `PUT /api/admin/doctors/{id}/authorize` without token → 401 Unauthorized
- [ ] `GET /api/admin/doctors/{id}/status` with admin token → 200 OK
- [ ] `GET /api/admin/doctors/{id}/status` with mom token → 401 Unauthorized
- [ ] `GET /api/admin/doctors/{id}/status` without token → 401 Unauthorized

### **JWT Token Edge Cases (MISSING - CRITICAL)**
**Priority**: 🔴 **HIGHEST** - Security vulnerability without these tests

**Required Tests**:
- [ ] **Expired Token Testing**: Verify 401 response for expired JWT tokens
- [ ] **Malformed Token Testing**: Verify 401 response for invalid JWT format
- [ ] **Wrong User Type Testing**: Verify 401 when mom token used on admin endpoint
- [ ] **Missing Claims Testing**: Verify 401 for tokens without userId or userType
- [ ] **Invalid Signature Testing**: Verify 401 for tokens with wrong signature

### **Role-Based Access Control Tests (MISSING - CRITICAL)**
**Priority**: 🔴 **HIGHEST** - Core security boundary testing

**Required Tests**:
- [ ] **Cross-Role Access Attempts**: Mom trying to access admin endpoints
- [ ] **Admin Accessing Mom Endpoints**: Verify admin can access mom-level data
- [ ] **Public Access Boundary**: Verify public endpoints work without authentication
- [ ] **Mom Authorization Boundary**: Verify mom endpoints require 8+ sessions
- [ ] **Unauthorized Mom Access**: Verify moms with <8 sessions get 403

### **Security Boundary Tests (MISSING - CRITICAL)**
**Priority**: 🔴 **HIGHEST** - Prevent unauthorized data access

**Required Tests**:
- [ ] **Business Data Protection**: Verify unauthorized users cannot access sensitive data
- [ ] **Admin-Only Operations**: Verify only admins can modify products/categories/offers
- [ ] **Mom Session Validation**: Verify mom authorization checks session count
- [ ] **Token Tampering Protection**: Verify modified tokens are rejected
- [ ] **Authorization Header Validation**: Verify malformed auth headers are rejected

## 🧪 Test Development Guidelines

### **Test Naming Conventions**
```kotlin
// Good test names
fun `POST admin categories with valid admin token should succeed`()
fun `POST admin categories with mom token should return unauthorized`()
fun `GET public categories without token should succeed`()
fun `PUT admin products with expired token should return unauthorized`()

// Bad test names
fun `testCreateCategory`()
fun `adminTest`()
fun `categoryEndpoint`()
```

### **Test Structure Pattern**
```kotlin
@Test
fun `descriptive test name with expected behavior`() = testApplication {
    // 1. Setup
    application { testModule() }
    val token = generateAppropriateToken()
    val request = createRequestObject()
    
    // 2. Execute
    val response = client.httpMethod("/api/endpoint") {
        header(HttpHeaders.Authorization, "Bearer $token") // if needed
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(gson.toJson(request)) // if needed
    }
    
    // 3. Verify
    assertEquals(ExpectedHttpStatusCode, response.status)
    // Additional response content verification if needed
}
```

### **Mock Service Requirements**
- All tests must use mock repositories and services
- Mock services should return predictable, testable data
- Mock authentication should match real JWT patterns
- Mock authorization should simulate real session checks

## 📊 Current Testing Status

### **Test Suite Overview**
- **Total Tests**: 116/116 passing ✅
- **AdminRoutesTest**: 11/11 passing ✅
- **Service Tests**: All passing ✅
- **Authentication Tests**: All passing ✅
- **Core Functionality**: All passing ✅

### **Testing Gaps (Critical)**
- **AdminDoctorRoutes**: 0 tests (6 needed)
- **JWT Edge Cases**: 0 tests (5 needed)
- **Role-Based Access Control**: 0 tests (5 needed)
- **Security Boundary**: 0 tests (5 needed)
- **Individual Route Testing**: Limited coverage

### **Next Testing Priorities**
1. **AdminDoctorRoutes Tests** - Complete missing endpoint coverage
2. **JWT Token Edge Cases** - Security vulnerability testing
3. **Role-Based Access Control** - Cross-role access prevention
4. **Security Boundary Tests** - Unauthorized access prevention
5. **Individual Route Tests** - Comprehensive endpoint coverage

## 🎯 Testing Success Criteria

### **Definition of Done for Tests**
- [ ] All HTTP status codes properly tested (200, 401, 403, 404, 400, 409)
- [ ] All user roles tested (Public, Mom, Admin)
- [ ] All authentication scenarios tested (valid, invalid, expired, missing)
- [ ] All authorization boundaries tested (role separation, session validation)
- [ ] All request/response serialization working correctly
- [ ] All edge cases covered (malformed requests, missing data, etc.)

### **Security Testing Checklist**
- [ ] Admin endpoints reject mom tokens
- [ ] Mom endpoints reject unauthorized moms (<8 sessions)
- [ ] Public endpoints work without authentication
- [ ] Expired tokens are properly rejected
- [ ] Malformed tokens are properly rejected
- [ ] Cross-role access attempts are blocked
- [ ] Business data is protected from unauthorized access

---

**🧪 Complete testing coverage ensures bulletproof security and reliability for the Mom Care Platform!**
