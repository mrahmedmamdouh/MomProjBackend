# Cursor Configuration for Mom Care Platform Backend

This directory contains Cursor IDE configuration files for the Mom Care Platform backend project.

## Files Overview

### `.cursorrules`
Comprehensive coding standards and guidelines for the project, including:
- Technology stack requirements (Ktor, MongoDB, Gson, etc.)
- Serialization rules (use Gson, avoid kotlinx.serialization)
- Commenting standards (no comments except actionable TODOs)
- Request model structure and validation
- Route structure and authentication patterns
- Error handling and response formats
- File organization and naming conventions
- Testing standards and security considerations

### `.cursor/commands.json`
Pre-configured commands for common development tasks:
- **Build & Test Commands**: Build project, run tests, API testing
- **Server Management**: Start server, check health, view logs
- **Git Operations**: Status, add, commit, push
- **API Testing**: Login tests for different user types
- **Documentation**: OpenAPI docs, Swagger UI access
- **Database**: Seed database, check connection status

### `.cursor/templates.json`
Code templates for rapid development:
- **New Route File**: Complete route handler with authentication
- **New Request Model**: Data class for API requests
- **New Service Class**: Business logic with validation
- **New Repository Class**: Data access layer with MongoDB
- **New Test Function**: Bash test function template
- **Multipart Route Handler**: File upload handling

### `.cursor/snippets.json`
Code snippets for common patterns:
- **Route Handlers**: GET, POST, PUT, DELETE with Gson
- **API Responses**: Success and error response structures
- **Validation**: Service validation patterns
- **Authentication**: Route authentication helpers
- **Testing**: Bash test function components
- **File Upload**: Multipart form data handling

### `.cursor/settings.json`
IDE settings optimized for the project:
- Kotlin language server configuration
- Code formatting and organization
- File associations and exclusions
- Editor preferences (tab size, rulers, etc.)
- Search and file watcher exclusions

## Usage

### Commands
Access commands via Cursor's command palette (Cmd/Ctrl+Shift+P):
- Type "Cursor: Run Command" and select from the list
- Commands are organized by category (Build, Test, Git, etc.)

### Templates
Use templates for rapid code generation:
- Type template name in command palette
- Fill in the variable prompts
- Template generates complete, project-compliant code

### Snippets
Use snippets for common code patterns:
- Type snippet prefix (e.g., `ktor-get`, `api-response`)
- Press Tab to expand
- Fill in the highlighted variables

### Rules
The `.cursorrules` file automatically guides Cursor's AI assistant to:
- Follow project-specific coding standards
- Use correct technology stack (Gson over kotlinx.serialization)
- Maintain consistent code structure
- Apply proper authentication patterns
- Generate appropriate test code

## Key Project Patterns

### Serialization
```kotlin
// ✅ Correct - Use Gson
val request = gson.fromJson(call.receiveText(), RequestClass::class.java)

// ❌ Wrong - Don't use kotlinx.serialization
@Serializable
data class Request(...)
```

### Route Structure
```kotlin
fun Route.routeName(service: Service, gson: Gson) {
    authRoute("/api/endpoint", service) {
        get { /* handler */ }
        post { /* handler */ }
    }
}
```

### API Responses
```kotlin
call.respond(
    HttpStatusCode.OK,
    BasicApiResponse(
        success = true,
        data = responseData
    )
)
```

### Testing
```bash
test_endpoint() {
    print_test "Test Endpoint"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/endpoint" "" "$TOKEN")
    # ... validation
}
```

## Best Practices

1. **Always use Gson** for JSON serialization
2. **No comments** except actionable TODOs
3. **Follow the file structure** defined in rules
4. **Use proper authentication** route functions
5. **Validate all requests** before processing
6. **Test both authorized and unauthorized** scenarios
7. **Update documentation** when adding new endpoints
8. **Use templates and snippets** for consistency

## Troubleshooting

### Common Issues
- **Build failures**: Check Gson usage vs kotlinx.serialization
- **Test failures**: Verify authentication tokens and endpoint URLs
- **Documentation**: Update both Swagger and Postman collection
- **Validation**: Ensure proper enum values (uppercase for specializations)

### Getting Help
- Check `.cursorrules` for coding standards
- Use commands for common operations
- Reference templates for code structure
- Use snippets for quick code generation
