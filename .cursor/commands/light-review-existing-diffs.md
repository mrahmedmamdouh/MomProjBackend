# Light Review Existing Diffs

## Overview
Quick review checklist for examining existing code changes and pull requests to identify potential issues and improvements.

## Quick Review Checklist

### Code Quality
- [ ] Code follows project standards (see `.cursorrules`)
- [ ] No obvious bugs or logic errors
- [ ] Functions are appropriately sized and focused
- [ ] Variable names are descriptive
- [ ] No code duplication
- [ ] Consistent formatting and style

### Serialization & Data Handling
- [ ] Uses Gson for JSON serialization (NOT kotlinx.serialization)
- [ ] Request models are properly structured
- [ ] Input validation is implemented
- [ ] Error handling is appropriate
- [ ] No hardcoded values that should be configurable

### Authentication & Security
- [ ] Proper authentication/authorization checks
- [ ] No security vulnerabilities
- [ ] Input validation prevents injection attacks
- [ ] Sensitive data is handled securely
- [ ] File uploads are properly validated

### API Design
- [ ] RESTful API design principles followed
- [ ] Consistent response format using `BasicApiResponse`
- [ ] Appropriate HTTP status codes
- [ ] Error messages are user-friendly
- [ ] API endpoints follow naming conventions

### Testing
- [ ] Tests are included for new functionality
- [ ] Both success and error scenarios are tested
- [ ] Test data is properly set up
- [ ] Tests follow project patterns

### Documentation
- [ ] OpenAPI documentation is updated
- [ ] Postman collection is updated
- [ ] Code comments are minimal (only actionable TODOs)
- [ ] README or relevant docs are updated if needed

## Review Process

### 1. Initial Scan (5 minutes)
- [ ] Check for obvious issues
- [ ] Verify code follows project standards
- [ ] Look for security concerns
- [ ] Check test coverage

### 2. Detailed Review (10-15 minutes)
- [ ] Examine logic flow
- [ ] Check error handling
- [ ] Verify input validation
- [ ] Review API design
- [ ] Check documentation updates

### 3. Final Assessment
- [ ] Overall code quality
- [ ] Security implications
- [ ] Performance impact
- [ ] Maintainability
- [ ] Ready for merge

## Common Issues to Look For

### Serialization Issues
```kotlin
// ❌ Wrong - Using kotlinx.serialization
@Serializable
data class Request(...)

// ✅ Correct - Use Gson
data class Request(...)
val request = gson.fromJson(call.receiveText(), Request::class.java)
```

### Authentication Issues
```kotlin
// ❌ Wrong - Missing authentication
route("/api/endpoint") {
    // Implementation
}

// ✅ Correct - Proper authentication
momRoute("/api/endpoint", momService) {
    // Implementation
}
```

### Validation Issues
```kotlin
// ❌ Wrong - No validation
val request = gson.fromJson(call.receiveText(), Request::class.java)
// Process request directly

// ✅ Correct - With validation
val request = gson.fromJson(call.receiveText(), Request::class.java)
when (service.validateRequest(request)) {
    is Service.ValidationEvent.ErrorFieldEmpty -> {
        call.respond(HttpStatusCode.BadRequest, BasicApiResponse(
            success = false,
            message = "Field cannot be empty"
        ))
        return@post
    }
    is Service.ValidationEvent.Success -> {
        // Process request
    }
}
```

### Response Format Issues
```kotlin
// ❌ Wrong - Inconsistent response
call.respond("Success")

// ✅ Correct - Consistent response
call.respond(
    HttpStatusCode.OK,
    BasicApiResponse(
        success = true,
        data = responseData
    )
)
```

## Review Questions

### Functionality
- Does the code do what it's supposed to do?
- Are edge cases handled appropriately?
- Is error handling comprehensive?
- Are there any obvious bugs?

### Security
- Are there any security vulnerabilities?
- Is input validation implemented?
- Are authentication/authorization checks proper?
- Is sensitive data handled securely?

### Performance
- Are there any performance issues?
- Are database queries efficient?
- Is there unnecessary code duplication?
- Are there any memory leaks?

### Maintainability
- Is the code readable and well-structured?
- Are functions appropriately sized?
- Is the code following project conventions?
- Is documentation adequate?

## Review Tools

### Command Line Tools
```bash
# Check for common issues
grep -r "kotlinx.serialization" src/
grep -r "println" src/
grep -r "TODO" src/

# Check test coverage
./gradlew test
./tests/run-all-tests.sh
```

### IDE Tools
- Use IDE's built-in code analysis
- Check for unused imports
- Verify code formatting
- Look for potential issues

### Manual Checks
- Review diff line by line
- Check for logical errors
- Verify error handling
- Test API endpoints manually

## Review Comments

### Positive Feedback
- "Great implementation of the validation logic"
- "Good use of the project's authentication patterns"
- "Comprehensive test coverage"
- "Clear and readable code"

### Constructive Feedback
- "Consider adding input validation here"
- "This could benefit from better error handling"
- "The function might be too large - consider breaking it down"
- "Missing test case for error scenario"

### Critical Issues
- "Security vulnerability: input not validated"
- "This will cause a runtime error"
- "Missing authentication check"
- "Hardcoded secret detected"

## Review Decision

### Approve
- [ ] Code quality is good
- [ ] Security is adequate
- [ ] Tests are comprehensive
- [ ] Documentation is updated
- [ ] No critical issues

### Request Changes
- [ ] Minor issues that need fixing
- [ ] Missing tests
- [ ] Documentation needs updates
- [ ] Code quality improvements needed

### Reject
- [ ] Critical security issues
- [ ] Major functionality problems
- [ ] Poor code quality
- [ ] Missing essential components

## Follow-up Actions

### After Approval
- [ ] Merge the pull request
- [ ] Delete feature branch
- [ ] Update related issues
- [ ] Notify team of changes

### After Requesting Changes
- [ ] Provide specific feedback
- [ ] Suggest improvements
- [ ] Offer to help with implementation
- [ ] Schedule follow-up review

### After Rejection
- [ ] Explain reasons clearly
- [ ] Provide guidance for improvement
- [ ] Suggest alternative approaches
- [ ] Offer to discuss further

## Best Practices

1. **Be Constructive** - Provide helpful feedback
2. **Be Specific** - Point out exact issues
3. **Be Timely** - Review promptly
4. **Be Thorough** - Check all aspects
5. **Be Respectful** - Maintain professional tone
6. **Be Helpful** - Offer solutions, not just problems
7. **Be Consistent** - Apply same standards to all reviews

## Common Review Patterns

### New API Endpoint
- Check route definition
- Verify authentication
- Review request/response models
- Check validation logic
- Verify error handling
- Check test coverage
- Verify documentation updates

### Bug Fix
- Understand the problem
- Verify the fix addresses the issue
- Check for regressions
- Verify test coverage
- Check for similar issues elsewhere

### Feature Addition
- Review overall design
- Check integration points
- Verify security implications
- Check performance impact
- Verify test coverage
- Check documentation updates

### Refactoring
- Verify functionality is preserved
- Check for performance improvements
- Verify code quality improvements
- Check for any new issues
- Verify test coverage

## Review Checklist by File Type

### Kotlin Files
- [ ] Follows Kotlin conventions
- [ ] Proper error handling
- [ ] No hardcoded values
- [ ] Appropriate use of Kotlin features
- [ ] Good variable naming

### Test Files
- [ ] Comprehensive test coverage
- [ ] Both success and error scenarios
- [ ] Proper test data setup
- [ ] Clear test names
- [ ] Follows project test patterns

### Configuration Files
- [ ] Proper configuration structure
- [ ] No sensitive data exposed
- [ ] Appropriate default values
- [ ] Clear documentation

### Documentation Files
- [ ] Accurate and up-to-date
- [ ] Clear and comprehensive
- [ ] Includes examples
- [ ] Follows documentation standards

## Review Metrics

### Quality Metrics
- Code coverage percentage
- Number of issues found
- Types of issues (security, performance, etc.)
- Time to review
- Number of iterations

### Process Metrics
- Review completion time
- Number of comments
- Number of changes requested
- Final approval rate
- Time to merge

## Continuous Improvement

### Review Process
- Regularly update review checklist
- Share best practices with team
- Learn from review feedback
- Improve review efficiency
- Maintain review quality

### Team Development
- Provide constructive feedback
- Share knowledge and expertise
- Mentor junior developers
- Improve team coding standards
- Foster collaborative environment
