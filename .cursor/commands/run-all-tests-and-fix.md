# Run All Tests and Fix Issues

## Overview
Comprehensive guide for running all tests and systematically fixing any issues that arise in the Mom Care Platform backend.

## Test Execution Order

### 1. Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.evelolvetech.service.MomServiceTest"

# Run tests with verbose output
./gradlew test --info
```

### 2. API Integration Tests
```bash
# Run comprehensive API tests
./tests/main/test-api-comprehensive.sh

# Run individual test suites
./tests/test-auth.sh
./tests/test-mom.sh
./tests/test-doctor.sh
./tests/test-admin.sh
./tests/test-public.sh
./tests/test-security.sh
```

### 3. Comprehensive Test Suite
```bash
# Run the complete test suite
./tests/main/test-api-comprehensive.sh

# Run without exit on first failure
./tests/main/test-api-comprehensive-no-exit.sh
```

## Common Test Issues and Fixes

### Build Failures

#### Gradle Build Issues
```bash
# Clean and rebuild
./gradlew clean build

# Check dependencies
./gradlew dependencies

# Force refresh dependencies
./gradlew build --refresh-dependencies
```

#### Common Build Errors
1. **Serialization Conflicts**
   ```kotlin
   // ❌ Wrong - Using kotlinx.serialization
   @Serializable
   data class Request(...)
   
   // ✅ Correct - Use Gson
   data class Request(...)
   val request = gson.fromJson(call.receiveText(), Request::class.java)
   ```

2. **Import Issues**
   ```kotlin
   // ❌ Wrong
   import kotlinx.serialization.Serializable
   
   // ✅ Correct
   import com.google.gson.Gson
   ```

3. **Missing Dependencies**
   ```kotlin
   // Add to build.gradle.kts if missing
   implementation(libs.ktor.serialization.gson)
   ```

### API Test Failures

#### Authentication Issues
```bash
# Check if server is running
curl -s http://localhost:8080/api/categories | jq .

# Test login endpoints
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"beth@example.com","password":"password123"}' | jq .
```

#### Common API Test Errors
1. **401 Unauthorized**
   - Check JWT token validity
   - Verify user exists in database
   - Check authentication interceptor

2. **403 Forbidden**
   - Verify user authorization status
   - Check route interceptor usage
   - Ensure proper role-based access

3. **400 Bad Request**
   - Validate request format
   - Check required fields
   - Verify enum values (uppercase for specializations)

4. **404 Not Found**
   - Check endpoint URL
   - Verify route registration
   - Check server is running

### Database Issues

#### Connection Problems
```bash
# Check MongoDB connection
# Look for connection errors in logs
tail -f logs/application.log

# Restart MongoDB if needed
brew services restart mongodb-community
```

#### Data Issues
```bash
# Seed database with test data
./seed.sh

# Check if test users exist
# Login with test credentials to verify
```

## Systematic Fix Process

### 1. Identify the Issue
```bash
# Run tests and capture output
./tests/main/test-api-comprehensive.sh > test-results.log 2>&1

# Analyze failures
grep -i "failed\|error" test-results.log

# Clean up log files after analysis
rm -f test-results.log
```

### 2. Categorize Issues
- **Build Issues**: Compilation errors, dependency problems
- **Runtime Issues**: Server startup, database connection
- **API Issues**: Endpoint failures, authentication problems
- **Test Issues**: Test script problems, assertion failures

### 3. Fix by Priority
1. **Critical Issues**: Build failures, server startup
2. **High Priority**: Authentication, authorization
3. **Medium Priority**: API functionality, validation
4. **Low Priority**: Test improvements, documentation

### 4. Verify Fixes
```bash
# Run specific test to verify fix
./tests/test-mom.sh

# Run all tests to ensure no regressions
./test-api-comprehensive.sh
```

## Common Fix Patterns

### Fix Serialization Issues
```kotlin
// 1. Remove kotlinx.serialization imports
// 2. Remove @Serializable annotations
// 3. Use Gson for deserialization
val request = gson.fromJson(call.receiveText(), RequestClass::class.java)
```

### Fix Authentication Issues
```kotlin
// 1. Check route interceptor usage
momRoute("/api/endpoint", momService) { // Requires authorization
// vs
momRouteBasic(momService) { // No authorization check
    route("/api/endpoint") {
        // Implementation
    }
}

// 2. Verify JWT token handling
val userId = call.getCurrentUserIdSafe()
```

### Fix Validation Issues
```kotlin
// 1. Check enum values (uppercase)
"PEDIATRICS" // ✅ Correct
"Pediatrics" // ❌ Wrong

// 2. Verify validation logic
when (service.validateRequest(request)) {
    is Service.ValidationEvent.ErrorFieldEmpty -> {
        // Handle error
    }
    is Service.ValidationEvent.Success -> {
        // Process request
    }
}
```

### Fix Test Issues
```bash
# 1. Check test data setup
# Ensure test users exist and are properly configured

# 2. Verify test environment
# Check server is running on correct port
# Verify database is accessible

# 3. Fix test assertions
# Check expected vs actual values
# Verify JSON field paths
```

## Debugging Tools

### Server Logs
```bash
# View application logs
tail -f logs/application.log

# Check for specific errors
grep -i "error\|exception" logs/application.log
```

### API Testing
```bash
# Test individual endpoints
curl -v -X GET http://localhost:8080/api/moms/profile \
  -H "Authorization: Bearer YOUR_TOKEN"

# Check response headers
curl -I http://localhost:8080/api/categories
```

### Database Debugging
```bash
# Connect to MongoDB
mongosh

# Check collections
show collections

# Query test data
db.moms.find({email: "beth@example.com"})
```

## Test Environment Setup

### Prerequisites
```bash
# Check required tools
which curl
which jq
which mongosh

# Verify server dependencies
./gradlew dependencies
```

### Environment Variables
```bash
# Set test environment
export BASE_URL="http://localhost:8080"
export TEST_MODE=true
```

### Database Setup
```bash
# Start MongoDB
brew services start mongodb-community

# Seed test data
./seed.sh

# Verify test data
./tests/test-auth.sh
```

## Continuous Integration

### Pre-commit Checks
```bash
# Run before committing
./gradlew build
./test-api-comprehensive.sh
```

### Automated Testing
```bash
# Set up CI pipeline
# 1. Build project
# 2. Run unit tests
# 3. Start server
# 4. Run API tests
# 5. Generate test report
```

## Test Reporting

### Generate Test Reports
```bash
# Run tests with detailed output
./tests/main/test-api-comprehensive.sh > comprehensive-test-results.log 2>&1

# Analyze results
echo "=== TEST SUMMARY ==="
grep -c "✅" comprehensive-test-results.log
grep -c "❌" comprehensive-test-results.log

# Clean up log files after analysis
rm -f comprehensive-test-results.log
```

### Performance Testing
```bash
# Basic performance test
ab -n 1000 -c 10 http://localhost:8080/api/categories

# Load testing
# Use tools like JMeter or Artillery for comprehensive load testing
```

## Best Practices

1. **Run tests frequently** - Don't wait until the end
2. **Fix issues immediately** - Don't let them accumulate
3. **Test in isolation** - Run individual test suites
4. **Verify fixes** - Always run tests after making changes
5. **Document issues** - Keep track of common problems
6. **Automate testing** - Set up CI/CD pipelines
7. **Monitor performance** - Watch for performance regressions
