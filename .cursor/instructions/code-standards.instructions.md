---
applyTo: '**/*.kt'
---
# 🔧 Kotlin Code Standards & Development Instructions

## 📋 Code Quality Standards

### **🚫 STRICT RULES - NO EXCEPTIONS**

1. **NO COMMENTS ALLOWED**
   - No `//` single-line comments
   - No `/* */` block comments  
   - No `/** */` documentation comments
   - Code must be self-documenting through clear naming

2. **NO DEBUG LOGS IN PRODUCTION CODE**
   - No `println()` statements
   - No `logger.debug()` calls
   - Logs only for debugging during development (remove before commit)

3. **ONLY TODO COMMENTS PERMITTED**
   - `// TODO: specific task description` - ONLY format allowed
   - Must include specific actionable description
   - Example: `// TODO: implement caching for better performance`

4. **NO EXTRA BLANK LINES**
   - Maximum 1 blank line between functions/classes
   - No trailing blank lines at end of file
   - No multiple consecutive blank lines anywhere in code
   - No blank lines within function bodies unless separating logical blocks

## 🏗️ Architecture Patterns

### **Repository Pattern Implementation**
- All data access through repository interfaces
- Repository interfaces in `data/repository/api/` organized by domain:
  - `api/auth/` - Authentication repositories (UserRepository, RefreshTokenRepository)
  - `api/mom/` - Mom domain repositories (MomRepository, NidRepository)
  - `api/doctor/` - Doctor domain repositories (DoctorRepository)
  - `api/mom/ecommerce/` - E-commerce repositories (ProductRepository, CartRepository, etc.)
- Repository implementations in `data/repository/impl/` with matching structure
- Service layer handles business logic
- Controllers/Routes handle HTTP concerns only

### **Dependency Injection (Koin)**
- All dependencies injected via Koin
- Services declared in `di/MainModule.kt`
- Use constructor injection pattern
- No static dependencies or singletons
- Explicit type annotations for service constructors to avoid inference issues

### **Repository Structure Guidelines**
- **Interface Location**: `data/repository/api/{domain}/`
- **Implementation Location**: `data/repository/impl/{domain}/`
- **Domain Organization**:
  - `auth/` - Authentication and user management
  - `mom/` - Mom-specific domain logic
  - `doctor/` - Doctor-specific domain logic
  - `mom/ecommerce/` - E-commerce functionality for moms
- **Import Patterns**: Always use explicit imports for repository interfaces
- **Example**: `import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository`

### **Error Handling**
- Use `BasicApiResponse<T>` for all API responses
- Service layer returns domain objects or null
- Route layer handles HTTP status codes
- Never leak sensitive information in error messages

## 🔐 Security Standards

### **Authentication & Authorization**
- JWT tokens required for protected endpoints
- Validate user permissions at service layer
- Use `userType` and `userId` from JWT claims
- Implement proper session validation

### **Input Validation**
- Validate all input at service layer
- Use data classes with validation
- Sanitize user input to prevent injection
- Check file types and sizes for uploads
- **CRITICAL**: Use nullable types for required fields to prevent silent defaults
- **Example**: `val numberOfSessions: Int?` instead of `val numberOfSessions: Int`
- Always validate nullable fields before use to prevent security vulnerabilities

### **Data Class Design Principles**
- **NO DEFAULT VALUES**: Avoid `= null` defaults in data class properties
- **Explicit Nullability**: Use `String?` instead of `String? = null`
- **Consistent Patterns**: All similar data classes should follow the same pattern
- **Clear Intent**: Nullable types should clearly indicate optional vs required fields
- **Example**: 
  ```kotlin
  // ✅ GOOD - Explicit nullable without defaults
  data class UpdateRequest(val name: String?, val phone: String?)
  
  // ❌ BAD - Default values hide intent
  data class UpdateRequest(val name: String? = null, val phone: String? = null)
  ```

### **File Upload Security**
- UUID naming for all uploaded files
- Validate file types and extensions
- Check file sizes and prevent oversized uploads
- Store file paths in database, not content
- Early email validation to prevent unnecessary uploads

## 📝 Naming Conventions

### **Classes & Interfaces**
- PascalCase: `MomService`, `UserRepository`
- Interfaces without `I` prefix
- Implementation classes with `Impl` suffix: `UserRepositoryImpl`

### **Functions & Variables**
- camelCase: `getUserById`, `isValidEmail`
- Descriptive names that explain purpose
- Boolean functions start with `is`, `has`, `can`
- No abbreviations unless widely understood

### **Constants**
- UPPER_SNAKE_CASE in `util/Constants.kt`
- Group related constants together
- Use meaningful names: `PASSWORD_MIN_LENGTH`

## 🔄 API Endpoint Patterns

### **Route Structure**
```kotlin
fun Route.entityRoutes(service: EntityService) {
    route("/api/entities") {
        authenticate(optional = false) {
            get {
                // Implementation
            }
            post {
                // Implementation
            }
        }
    }
}
```

### **Request/Response Pattern (NEW - Use Response Helpers)**
```kotlin
post("/endpoint") {
    val request = kotlin.runCatching { 
        call.receive<RequestType>() 
    }.getOrNull() ?: run {
        call.respondWithBadRequest<Unit>(Constants.INVALID_REQUEST_DATA)
        return@post
    }
    
    val result = service.performAction(request)
    call.respondWithMapping(result)
}
```

### **Response Helper Methods (PREFERRED)**
```kotlin
// For successful responses with data
call.respondWithMapping(result, HttpStatusCode.OK)

// For error responses
call.respondWithBadRequest<Unit>("Error message")
call.respondWithInternalServerError<Unit>("Internal error message")

// Response helpers automatically handle:
// - HTTP status code mapping via HttpStatusMapper
// - BasicApiResponse wrapper
// - Consistent error format
```

### **Security Fix Pattern for Required Fields**
```kotlin
// Data class with nullable required field
@Serializable
data class UpdateSessionsRequest(
    val numberOfSessions: Int?  // Nullable to prevent silent defaults
)

// Route handler with explicit validation
put("/sessions") {
    val request = kotlin.runCatching {
        call.receive<UpdateSessionsRequest>()
    }.getOrNull() ?: run {
        call.respondWithBadRequest<Unit>(Constants.INVALID_REQUEST_DATA)
        return@put
    }

    val numberOfSessions = request.numberOfSessions ?: run {
        call.respondWithBadRequest<Unit>("numberOfSessions is required")
        return@put
    }

    // Now safe to use numberOfSessions
    val result = service.updateSessions(userId, numberOfSessions)
    call.respondWithMapping(result)
}
```

### **Legacy Request/Response Pattern (DEPRECATED)**
```kotlin
post("/endpoint") {
    val request = kotlin.runCatching { 
        call.receive<RequestType>() 
    }.getOrNull() ?: run {
        call.respond(HttpStatusCode.BadRequest)
        return@post
    }
    
    val result = service.performAction(request)
    if (result != null) {
        call.respond(
            HttpStatusCode.OK,
            BasicApiResponse(successful = true, data = result)
        )
    } else {
        call.respond(
            HttpStatusCode.OK,
            BasicApiResponse<Unit>(
                successful = false,
                message = Constants.ERROR_MESSAGE
            )
        )
    }
}
```

### **Authentication Pattern**
```kotlin
authenticate(optional = false) {
    get {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()
        val userType = principal?.payload?.getClaim("userType")?.asString()
        
        if (userId == null || userType == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }
        
        // Implementation
    }
}
```

## 💾 Database Patterns

### **Repository Implementation**
```kotlin
class EntityRepositoryImpl(private val database: MongoDatabase) : EntityRepository {
    private val collection = database.getCollection<Entity>("entities")
    
    override suspend fun findById(id: String): Entity? {
        return collection.findOne(Entity::id eq id)
    }
    
    override suspend fun create(entity: Entity): Boolean {
        return try {
            collection.insertOne(entity).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }
}
```

### **Service Implementation**
```kotlin
class EntityService(
    private val repository: EntityRepository,
    private val hashingService: HashingService
) {
    suspend fun createEntity(request: CreateEntityRequest): Entity? {
        return when (validateRequest(request)) {
            is ValidationEvent.Success -> {
                val entity = Entity(
                    id = generateUniqueId(),
                    // Map request to entity
                )
                if (repository.create(entity)) entity else null
            }
            else -> null
        }
    }
    
    private fun validateRequest(request: CreateEntityRequest): ValidationEvent {
        // Validation logic
    }
}
```

## 📁 File Upload Patterns

### **Multipart Handling**
```kotlin
post("/register") {
    val multipartData = call.receiveMultipart()
    var request: RequestType? = null
    var emailExists = false
    var emailChecked = false
    val fileParts = mutableMapOf<String, PartData.FileItem>()
    
    multipartData.forEachPart { part ->
        when {
            part is PartData.FormItem && part.name == "data" -> {
                request = jsonConfig.decodeFromString<RequestType>(part.value)
                emailExists = service.doesEmailExist(request!!.email)
                emailChecked = true
                part.dispose()
            }
            
            part is PartData.FileItem && part.name in expectedFileFields -> {
                if (!emailChecked) {
                    fileParts[part.name!!] = part
                } else if (emailExists) {
                    part.dispose()
                } else {
                    // Process file
                    part.dispose()
                }
            }
            
            else -> part.dispose()
        }
        
        // Handle buffered files after email check
        if (emailChecked && fileParts.isNotEmpty()) {
            fileParts.forEach { (name, bufferedPart) ->
                if (emailExists) {
                    bufferedPart.dispose()
                } else {
                    // Process buffered file
                    bufferedPart.dispose()
                }
            }
            fileParts.clear()
        }
    }
}
```

## 🧪 Testing Patterns

### **Service Testing**
- Test business logic in service layer
- Mock repository dependencies
- Test validation scenarios
- Test error conditions

### **Integration Testing**
- Test complete API endpoints
- Use test database
- Test authentication flows
- Test file upload scenarios

## 📚 Documentation Standards

### **Self-Documenting Code**
- Clear function and variable names
- Logical code organization
- Consistent patterns across codebase
- Minimal cognitive complexity

### **API Documentation**
- Update `main.instructions.md` for new endpoints
- Update Postman collection with new requests
- Document request/response examples
- Include authentication requirements

## 🚀 Performance Guidelines

### **Database Optimization**
- Use appropriate indexes
- Implement pagination for large datasets
- Avoid N+1 queries
- Use projections when needed

### **Memory Management**
- Dispose multipart data properly
- Avoid large object creation in loops
- Use streaming for large files
- Clean up resources in finally blocks

## 🔧 Development Workflow

### **Before Implementing New Features**
1. Design data models and validation
2. Create repository interface and implementation
3. Implement service layer with business logic
4. Add routes with proper authentication
5. Update API documentation
6. Add to Postman collection
7. Test all scenarios

### **Code Review Checklist**
- [ ] No comments except TODO
- [ ] Proper error handling
- [ ] Input validation implemented
- [ ] Authentication requirements met
- [ ] File uploads secure
- [ ] Database operations optimized
- [ ] Documentation updated

---

**Follow these standards for consistent, secure, and maintainable Kotlin code! 🎯**
