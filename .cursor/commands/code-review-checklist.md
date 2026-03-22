# Code Review Checklist

## Overview
Comprehensive checklist for conducting thorough code reviews to ensure quality, security, and maintainability for the Mom Care Platform backend.

## Review Categories

### Functionality
- [ ] Code does what it's supposed to do
- [ ] Edge cases are handled appropriately
- [ ] Error handling is comprehensive and meaningful
- [ ] No obvious bugs or logic errors
- [ ] API endpoints return correct HTTP status codes
- [ ] Request validation works as expected
- [ ] Database operations are correct

### Code Quality
- [ ] Code is readable and well-structured
- [ ] Functions are small and focused (single responsibility)
- [ ] Variable names are descriptive and follow Kotlin conventions
- [ ] No code duplication
- [ ] Follows project conventions and patterns
- [ ] Proper use of Kotlin idioms and best practices
- [ ] Consistent formatting and style

### Serialization & Data Handling
- [ ] Uses Gson for JSON serialization (NOT kotlinx.serialization)
- [ ] Request models are in `src/main/kotlin/data/requests/`
- [ ] Each request class is in its own file
- [ ] Proper validation for all input fields
- [ ] Handles both JSON and multipart requests correctly
- [ ] File upload validation (type, size) is implemented

### Authentication & Authorization
- [ ] Proper use of route interceptors (`momRoute`, `doctorRoute`, `adminRoute`)
- [ ] JWT token validation is implemented
- [ ] Role-based access control is enforced
- [ ] Unauthorized access attempts are properly handled
- [ ] Authorization checks are appropriate for the endpoint
- [ ] Uses `momRouteBasic`/`doctorRouteBasic` when authorization check not needed

### API Design
- [ ] RESTful API design principles followed
- [ ] Consistent response format using `BasicApiResponse`
- [ ] Appropriate HTTP status codes used
- [ ] Error messages are user-friendly and informative
- [ ] API endpoints follow naming conventions
- [ ] Request/response models are properly defined

### Security
- [ ] No obvious security vulnerabilities
- [ ] Input validation is present and comprehensive
- [ ] Sensitive data is handled properly
- [ ] No hardcoded secrets or credentials
- [ ] SQL injection prevention (parameterized queries)
- [ ] File upload security (type validation, size limits)
- [ ] CORS configuration is appropriate
- [ ] JWT token security best practices followed

### Testing
- [ ] Unit tests cover the new functionality
- [ ] API tests are added to appropriate test scripts
- [ ] Both authorized and unauthorized scenarios are tested
- [ ] Edge cases and error conditions are tested
- [ ] Test data is properly set up and cleaned up
- [ ] Tests follow the project's bash testing patterns

### Documentation
- [ ] OpenAPI documentation is updated (`documentation.yaml`)
- [ ] Postman collection is updated (`postman_collection.json`)
- [ ] Code comments are minimal (only actionable TODOs)
- [ ] README or relevant docs are updated if needed
- [ ] API examples include both success and error responses

### Performance
- [ ] Database queries are efficient
- [ ] No unnecessary database calls
- [ ] Proper use of connection pooling
- [ ] File operations are optimized
- [ ] Memory usage is reasonable
- [ ] No obvious performance bottlenecks

### Error Handling
- [ ] All exceptions are caught and handled appropriately
- [ ] Error responses are consistent with project standards
- [ ] Logging is appropriate (no sensitive data in logs)
- [ ] Graceful degradation when possible
- [ ] User-friendly error messages

### Dependencies & Configuration
- [ ] No unnecessary dependencies added
- [ ] Dependencies are properly declared in `build.gradle.kts`
- [ ] Configuration changes are documented
- [ ] Environment-specific settings are handled correctly

### Git & Version Control
- [ ] Commit message is descriptive and follows conventions
- [ ] No sensitive data in commit history
- [ ] Branch naming follows project conventions
- [ ] Pull request description is comprehensive
- [ ] Related issues are referenced

## Project-Specific Checks

### Mom Care Platform Specific
- [ ] Mom authorization logic (8+ sessions) is correctly implemented
- [ ] Doctor authorization by admin is properly handled
- [ ] E-commerce functionality respects authorization levels
- [ ] File uploads (photos, NID documents) are handled securely
- [ ] Session management is correct
- [ ] Role transitions are handled properly

### Ktor Framework Specific
- [ ] Route definitions follow project patterns
- [ ] Dependency injection (Koin) is used correctly
- [ ] Middleware and interceptors are properly configured
- [ ] Content negotiation is handled correctly
- [ ] CORS configuration is appropriate

### MongoDB Specific
- [ ] Database operations use proper KMongo patterns
- [ ] Data models are correctly defined
- [ ] Repository pattern is followed
- [ ] Connection handling is correct
- [ ] Data validation at database level

## Review Process
1. **Initial Review**: Check functionality and basic code quality
2. **Security Review**: Focus on security implications
3. **Performance Review**: Look for performance issues
4. **Documentation Review**: Ensure documentation is updated
5. **Final Approval**: All checkboxes completed and concerns addressed

## Common Issues to Watch For
- Using `kotlinx.serialization` instead of Gson
- Missing request validation
- Inappropriate use of route interceptors
- Hardcoded values that should be configurable
- Missing error handling
- Inconsistent response formats
- Security vulnerabilities in file uploads
- Missing tests for new functionality
- Outdated documentation
