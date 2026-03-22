# Setup New Feature

## Overview
Step-by-step guide for setting up a new feature in the Mom Care Platform backend, following project standards and best practices.

## Pre-Development Setup

### 1. Create Feature Branch
```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Create new feature branch
git checkout -b feature/your-feature-name

# Push branch to remote
git push -u origin feature/your-feature-name
```

### 2. Plan the Feature
- [ ] Define feature requirements
- [ ] Identify affected components
- [ ] Plan API endpoints
- [ ] Design data models
- [ ] Plan authentication/authorization
- [ ] Identify testing requirements

## Development Setup

### 1. Data Models
Create data models in `src/main/kotlin/data/models/`:

```kotlin
// Example: NewFeature.kt
package com.evelolvetech.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class NewFeature(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 2. Request Models
Create request models in `src/main/kotlin/data/requests/`:

```kotlin
// Example: CreateNewFeatureRequest.kt
package com.evelolvetech.data.requests

data class CreateNewFeatureRequest(
    val name: String,
    val description: String? = null
)

// Example: UpdateNewFeatureRequest.kt
data class UpdateNewFeatureRequest(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)
```

### 3. Response Models
Create response models in `src/main/kotlin/data/responses/`:

```kotlin
// Example: NewFeatureResponse.kt
package com.evelolvetech.data.responses

import com.evelolvetech.data.models.NewFeature

data class NewFeatureResponse(
    val id: String,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromNewFeature(newFeature: NewFeature): NewFeatureResponse {
            return NewFeatureResponse(
                id = newFeature.id,
                name = newFeature.name,
                description = newFeature.description,
                isActive = newFeature.isActive,
                createdAt = newFeature.createdAt,
                updatedAt = newFeature.updatedAt
            )
        }
    }
}
```

### 4. Repository Layer
Create repository in `src/main/kotlin/data/repository/`:

```kotlin
// Example: NewFeatureRepository.kt
package com.evelolvetech.data.repository

import com.evelolvetech.data.models.NewFeature
import com.mongodb.client.MongoDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.eq

class NewFeatureRepository : KoinComponent {
    private val database: MongoDatabase by inject()
    private val collection = database.getCollection<NewFeature>("newFeatures")
    
    fun findById(id: String): NewFeature? {
        return collection.findOne(NewFeature::id eq id)
    }
    
    fun findAll(): List<NewFeature> {
        return collection.find().toList()
    }
    
    fun save(newFeature: NewFeature): Boolean {
        return try {
            collection.insertOne(newFeature)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun update(id: String, newFeature: NewFeature): Boolean {
        return try {
            val result = collection.replaceOne(NewFeature::id eq id, newFeature)
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun delete(id: String): Boolean {
        return try {
            val result = collection.deleteOne(NewFeature::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}
```

### 5. Service Layer
Create service in `src/main/kotlin/service/`:

```kotlin
// Example: NewFeatureService.kt
package com.evelolvetech.service

import com.evelolvetech.data.models.NewFeature
import com.evelolvetech.data.repository.NewFeatureRepository
import com.evelolvetech.data.requests.CreateNewFeatureRequest
import com.evelolvetech.data.requests.UpdateNewFeatureRequest
import com.evelolvetech.util.ValidationUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NewFeatureService : KoinComponent {
    private val newFeatureRepository: NewFeatureRepository by inject()
    
    sealed class ValidationEvent {
        object Success : ValidationEvent()
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorInvalidFormat : ValidationEvent()
    }
    
    fun validateCreateRequest(request: CreateNewFeatureRequest): ValidationEvent {
        return when {
            request.name.isBlank() -> ValidationEvent.ErrorFieldEmpty
            !ValidationUtil.isValidName(request.name) -> ValidationEvent.ErrorInvalidFormat
            else -> ValidationEvent.Success
        }
    }
    
    fun validateUpdateRequest(request: UpdateNewFeatureRequest): ValidationEvent {
        return when {
            request.name?.isBlank() == true -> ValidationEvent.ErrorFieldEmpty
            request.name != null && !ValidationUtil.isValidName(request.name) -> ValidationEvent.ErrorInvalidFormat
            else -> ValidationEvent.Success
        }
    }
    
    fun getNewFeatureById(id: String): NewFeature? {
        return newFeatureRepository.findById(id)
    }
    
    fun getAllNewFeatures(): List<NewFeature> {
        return newFeatureRepository.findAll()
    }
    
    fun createNewFeature(request: CreateNewFeatureRequest): Boolean {
        return try {
            val newFeature = NewFeature(
                name = request.name,
                description = request.description
            )
            newFeatureRepository.save(newFeature)
        } catch (e: Exception) {
            false
        }
    }
    
    fun updateNewFeature(id: String, request: UpdateNewFeatureRequest): Boolean {
        return try {
            val existingFeature = newFeatureRepository.findById(id) ?: return false
            val updatedFeature = existingFeature.copy(
                name = request.name ?: existingFeature.name,
                description = request.description ?: existingFeature.description,
                isActive = request.isActive ?: existingFeature.isActive,
                updatedAt = System.currentTimeMillis()
            )
            newFeatureRepository.update(id, updatedFeature)
        } catch (e: Exception) {
            false
        }
    }
    
    fun deleteNewFeature(id: String): Boolean {
        return newFeatureRepository.delete(id)
    }
}
```

### 6. Route Layer
Create routes in `src/main/kotlin/routes/`:

```kotlin
// Example: NewFeatureRoutes.kt
package com.evelolvetech.routes

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.CreateNewFeatureRequest
import com.evelolvetech.data.requests.UpdateNewFeatureRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.NewFeatureResponse
import com.evelolvetech.service.NewFeatureService
import com.evelolvetech.util.Constants
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.newFeatureRoutes(newFeatureService: NewFeatureService, gson: Gson) {
    adminRoute("/api/new-features", newFeatureService) {
        get {
            val newFeatures = newFeatureService.getAllNewFeatures()
            val response = newFeatures.map { NewFeatureResponse.fromNewFeature(it) }
            
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    success = true,
                    data = response
                )
            )
        }
        
        post {
            val request = gson.fromJson(call.receiveText(), CreateNewFeatureRequest::class.java)
            
            when (newFeatureService.validateCreateRequest(request)) {
                is NewFeatureService.ValidationEvent.ErrorFieldEmpty -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Name cannot be empty"
                        )
                    )
                    return@post
                }
                is NewFeatureService.ValidationEvent.ErrorInvalidFormat -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Invalid name format"
                        )
                    )
                    return@post
                }
                is NewFeatureService.ValidationEvent.Success -> {
                    val success = newFeatureService.createNewFeature(request)
                    if (success) {
                        call.respond(
                            HttpStatusCode.Created,
                            BasicApiResponse(
                                success = true,
                                message = "New feature created successfully"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = Constants.UNKNOWN_ERROR
                            )
                        )
                    }
                }
            }
        }
    }
    
    adminRoute("/api/new-features/{id}", newFeatureService) {
        get {
            val id = call.parameters["id"]
            if (id != null) {
                val newFeature = newFeatureService.getNewFeatureById(id)
                if (newFeature != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            success = true,
                            data = NewFeatureResponse.fromNewFeature(newFeature)
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "New feature not found"
                        )
                    )
                }
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Invalid ID"
                    )
                )
            }
        }
        
        put {
            val id = call.parameters["id"]
            if (id != null) {
                val request = gson.fromJson(call.receiveText(), UpdateNewFeatureRequest::class.java)
                
                when (newFeatureService.validateUpdateRequest(request)) {
                    is NewFeatureService.ValidationEvent.ErrorFieldEmpty -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Name cannot be empty"
                            )
                        )
                        return@put
                    }
                    is NewFeatureService.ValidationEvent.ErrorInvalidFormat -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid name format"
                            )
                        )
                        return@put
                    }
                    is NewFeatureService.ValidationEvent.Success -> {
                        val success = newFeatureService.updateNewFeature(id, request)
                        if (success) {
                            call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse(
                                    success = true,
                                    message = "New feature updated successfully"
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                BasicApiResponse<Unit>(
                                    success = false,
                                    message = Constants.UNKNOWN_ERROR
                                )
                            )
                        }
                    }
                }
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Invalid ID"
                    )
                )
            }
        }
        
        delete {
            val id = call.parameters["id"]
            if (id != null) {
                val success = newFeatureService.deleteNewFeature(id)
                if (success) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            success = true,
                            message = "New feature deleted successfully"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNKNOWN_ERROR
                        )
                    )
                }
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Invalid ID"
                    )
                )
            }
        }
    }
}
```

### 7. Dependency Injection
Update `src/main/kotlin/di/MainModule.kt`:

```kotlin
// Add to MainModule.kt
single<NewFeatureRepository> { NewFeatureRepository() }
single<NewFeatureService> { NewFeatureService() }
```

### 8. Route Registration
Update `src/main/kotlin/Routing.kt`:

```kotlin
// Add to Routing.kt
newFeatureRoutes(get(), get())
```

## Testing Setup

### 1. Unit Tests
Create unit tests in `src/test/kotlin/`:

```kotlin
// Example: NewFeatureServiceTest.kt
package com.evelolvetech.service

import com.evelolvetech.data.requests.CreateNewFeatureRequest
import com.evelolvetech.data.requests.UpdateNewFeatureRequest
import org.junit.Test
import org.junit.Assert.*

class NewFeatureServiceTest {
    
    @Test
    fun `validateCreateRequest should return Success for valid request`() {
        val service = NewFeatureService()
        val request = CreateNewFeatureRequest(
            name = "Test Feature",
            description = "Test Description"
        )
        
        val result = service.validateCreateRequest(request)
        assertTrue(result is NewFeatureService.ValidationEvent.Success)
    }
    
    @Test
    fun `validateCreateRequest should return ErrorFieldEmpty for empty name`() {
        val service = NewFeatureService()
        val request = CreateNewFeatureRequest(
            name = "",
            description = "Test Description"
        )
        
        val result = service.validateCreateRequest(request)
        assertTrue(result is NewFeatureService.ValidationEvent.ErrorFieldEmpty)
    }
}
```

### 2. API Tests
Create API tests in `tests/`:

```bash
# Example: test-new-feature.sh
#!/bin/bash

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

test_new_feature_management() {
    print_subheader "New Feature Management"
    
    # Ensure admin login
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Test create new feature
    print_test "Create New Feature"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/new-features" \
        '{"name":"Test Feature","description":"Test Description"}' \
        "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 201 "$status_code" "Create New Feature"; then
        test_json_field "$response_body" "success" "true" "Create Success"
        print_success "✅ New feature created successfully"
    else
        print_error "❌ Failed to create new feature (Status: $status_code)"
    fi
    
    # Test get all new features
    print_test "Get All New Features"
    response=$(curl_request "GET" "$BASE_URL/api/new-features" "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get All New Features"; then
        test_json_field "$response_body" "success" "true" "Get Success"
        print_success "✅ New features retrieved successfully"
    else
        print_error "❌ Failed to get new features (Status: $status_code)"
    fi
}

# Run tests
test_new_feature_management
```

## Documentation Setup

### 1. OpenAPI Documentation
Update `src/main/resources/openapi/documentation.yaml`:

```yaml
  /api/new-features:
    get:
      operationId: getAllNewFeatures
      tags: [ Admin ]
      summary: Get all new features
      description: Retrieve all new features (admin only)
      security:
        - BearerAuth: []
      responses:
        "200":
          description: New features retrieved successfully
          content:
            application/json:
              schema:
                allOf:
                  - $ref: "#/components/schemas/BasicApiResponse"
                  - type: object
                    properties:
                      data:
                        type: array
                        items:
                          $ref: "#/components/schemas/NewFeatureResponse"
        "401":
          description: Unauthorized
        "403":
          description: Forbidden - admin access required
    
    post:
      operationId: createNewFeature
      tags: [ Admin ]
      summary: Create new feature
      description: Create a new feature (admin only)
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/CreateNewFeatureRequest"
      responses:
        "201":
          description: New feature created successfully
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/BasicApiResponse"
        "400":
          description: Bad request - validation error
        "401":
          description: Unauthorized
        "403":
          description: Forbidden - admin access required

components:
  schemas:
    NewFeatureResponse:
      type: object
      properties:
        id:
          type: string
          example: "507f1f77bcf86cd799439011"
        name:
          type: string
          example: "Test Feature"
        description:
          type: string
          example: "Test Description"
        isActive:
          type: boolean
          example: true
        createdAt:
          type: integer
          format: int64
          example: 1640995200000
        updatedAt:
          type: integer
          format: int64
          example: 1640995200000
    
    CreateNewFeatureRequest:
      type: object
      required:
        - name
      properties:
        name:
          type: string
          example: "Test Feature"
        description:
          type: string
          example: "Test Description"
    
    UpdateNewFeatureRequest:
      type: object
      properties:
        name:
          type: string
          example: "Updated Feature"
        description:
          type: string
          example: "Updated Description"
        isActive:
          type: boolean
          example: false
```

### 2. Postman Collection
Update `postman_collection.json`:

```json
{
  "name": "New Feature Management",
  "item": [
    {
      "name": "Get All New Features",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{auth_token}}"}],
        "url": {
          "raw": "{{base_url}}/api/new-features",
          "host": ["{{base_url}}"],
          "path": ["api", "new-features"]
        },
        "description": "Get all new features (admin only)"
      }
    },
    {
      "name": "Create New Feature",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Authorization", "value": "Bearer {{auth_token}}"},
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Test Feature\",\n  \"description\": \"Test Description\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/new-features",
          "host": ["{{base_url}}"],
          "path": ["api", "new-features"]
        },
        "description": "Create a new feature (admin only)"
      }
    }
  ]
}
```

## Validation & Testing

### 1. Build and Test
```bash
# Build project
./gradlew build

# Run unit tests
./gradlew test

# Run API tests
./tests/run-all-tests.sh
```

### 2. Manual Testing
```bash
# Start server
./gradlew run

# Test endpoints manually
curl -X GET http://localhost:8080/api/new-features \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 3. Documentation Testing
```bash
# Check Swagger UI
open http://localhost:8080/swagger

# Test Postman collection
# Import and test the updated collection
```

## Deployment Checklist

- [ ] All tests pass
- [ ] Code follows project standards
- [ ] Documentation is updated
- [ ] Security review completed
- [ ] Performance impact assessed
- [ ] Database migrations (if needed)
- [ ] Environment configuration updated
- [ ] Monitoring and logging configured

## Best Practices

1. **Follow the established patterns** - Use existing code as templates
2. **Test thoroughly** - Write comprehensive tests
3. **Document everything** - Update both Swagger and Postman
4. **Security first** - Implement proper authentication/authorization
5. **Validate inputs** - Always validate and sanitize user input
6. **Handle errors gracefully** - Provide meaningful error messages
7. **Use Gson** - Never use kotlinx.serialization
8. **Keep it simple** - Follow the principle of least complexity
9. **Review code** - Have others review your implementation
10. **Monitor performance** - Ensure your feature doesn't impact performance
