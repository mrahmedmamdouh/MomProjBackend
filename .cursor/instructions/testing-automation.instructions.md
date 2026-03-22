---
applyTo: '**'
---
# API Testing Automation Instructions

## Overview

This document provides comprehensive instructions for automated API testing using our custom cURL test script. The automation validates all endpoints, authentication flows, authorization systems, CRUD operations, and error handling after recent improvements.

## 🚀 Quick Start

### Prerequisites

1. **Server Running**: Ensure the Ktor server is running
2. **Database Ready**: MongoDB should be running with replica set configured
3. **Dependencies**: `curl` (required), `jq` (recommended for JSON parsing)

### Run All Tests

```bash
# Start server in one terminal
./gradlew run

# Run comprehensive test suite in another terminal
./tests/main/test-api-endpoints.sh
```

## 📋 Test Script Features

### Comprehensive Coverage

- **Authentication Tests**: Login, token validation, invalid credentials
- **Registration Tests**: Mom/Doctor registration, duplicate handling, file uploads
- **Authorization Tests**: Role-based access, admin operations, permission validation
- **CRUD Operations**: Create, Read, Update, Delete across all entities
- **Error Handling**: Malformed requests, missing fields, invalid endpoints
- **Response Extensions**: Validation of new helper extension implementations
- **Performance & Cache**: Authorization cache testing, response time validation

### Smart Validation

- **HTTP Status Codes**: Validates expected vs actual status codes
- **Response Content**: Checks for expected response patterns
- **Token Management**: Automatically obtains and manages JWT tokens
- **File Uploads**: Tests multipart form data with actual image files
- **Error Scenarios**: Validates proper error responses and status codes

## 🔧 Test Categories

### 1. Authentication Tests
```bash
# What it tests:
- Admin login with valid credentials
- Invalid login attempts
- Token extraction and validation
- JWT token format and expiration
```

### 2. Registration Tests
```bash
# What it tests:
- Mom registration with file uploads
- Doctor registration with file uploads
- Duplicate email handling
- Validation of required fields
- File upload security (images not saved on validation failure)
```

### 3. Authorization Tests
```bash
# What it tests:
- Admin-only endpoint access
- Unauthorized access attempts
- Role-based permission validation
- Doctor authorization workflow
- Mom authorization status checks
```

### 4. CRUD Operations Tests
```bash
# What it tests:
- Public endpoint access (categories, products)
- Authenticated endpoint access (cart, profile)
- Authorization-required operations
- Data retrieval and manipulation
```

### 5. Error Handling Tests
```bash
# What it tests:
- Malformed JSON requests → 400 Bad Request
- Missing required fields → 400 Bad Request
- Invalid endpoints → 404 Not Found
- Wrong HTTP methods → 405 Method Not Allowed
- Authentication failures → 401 Unauthorized
- Authorization failures → 403 Forbidden
```

### 6. Response Extensions Tests
```bash
# What it tests:
- ProductRoutes using respondWithBadRequest()
- SkuOfferRoutes using respondWithMapping()
- AdminDoctorRoutes using helper extensions
- Consistent error response formatting
```

### 7. Performance & Cache Tests
```bash
# What it tests:
- Mom authorization cache effectiveness
- Response time improvements
- Cache invalidation logic
- Multiple request performance
```

## 📊 Test Output

### Success Output
```bash
🎉 ALL TESTS PASSED! 🎉
✅ API is working correctly after all improvements

Total Tests: 25
Passed: 25
Failed: 0
```

### Failure Output
```bash
⚠️  SOME TESTS FAILED ⚠️
❌ Please review the failures above

Total Tests: 25
Passed: 22
Failed: 3

❌ FAIL: Admin Login - Expected: 200, Got: 401
   Response: Status code mismatch
```

## 🛠️ Advanced Usage

### Run Specific Test Categories

```bash
# Run only authentication tests
./tests/main/test-api-endpoints.sh | grep -A 20 "AUTHENTICATION TESTS"

# Run only error handling tests  
./tests/main/test-api-endpoints.sh | grep -A 15 "ERROR HANDLING TESTS"
```

### Debug Mode

```bash
# Enable verbose curl output for debugging
export CURL_VERBOSE=1
./tests/main/test-api-endpoints.sh
```

### Custom Configuration

```bash
# Test against different server
export BASE_URL="http://localhost:9090"
./tests/main/test-api-endpoints.sh

# Use different test files
export TEST_FILES_DIR="custom-test-files"
./tests/main/test-api-endpoints.sh
```

## 📝 Test Data Management

### Automatic Setup
- **Database Seeding**: Script automatically runs `./gradlew runSeed`
- **Test Files**: Uses `test-files/` directory for image uploads
- **Dynamic Data**: Generates unique emails/IDs using timestamps

### Required Test Files
```
test-files/
├── test-photo.jpg      # Profile photo for registration
├── test-nid-front.jpg  # NID front image
└── test-nid-back.jpg   # NID back image
```

### Cleanup
- **Automatic**: Each test run starts with fresh database seeding
- **Manual**: Run `./gradlew runSeed` to reset database state

## 🚨 Troubleshooting

### Common Issues

#### Server Not Running
```bash
Error: Server is not running
Please start the server with: ./gradlew run
```
**Solution**: Start server in separate terminal

#### Missing Test Files
```bash
Error: Test files missing
Required: test-photo.jpg, test-nid-front.jpg, test-nid-back.jpg
```
**Solution**: Ensure test files exist in `test-files/` directory

#### Database Connection Issues
```bash
Error: Database seeding failed
Check database connection
```
**Solution**: Verify MongoDB is running with replica set configured

#### Permission Issues
```bash
Error: Permission denied
```
**Solution**: Make script executable: `chmod +x tests/main/test-api-endpoints.sh`

### Debug Steps

1. **Check Server Logs**: Review server output for errors
2. **Verify Database**: Ensure MongoDB is running and accessible
3. **Test Manually**: Try individual cURL commands to isolate issues
4. **Check Tokens**: Verify JWT tokens are being generated correctly

## 🔍 Validation Points

### HTTP Status Code Validation
- `200 OK`: Successful requests
- `201 Created`: Successful resource creation
- `400 Bad Request`: Malformed requests (using respondWithBadRequest)
- `401 Unauthorized`: Missing/invalid authentication
- `403 Forbidden`: Insufficient permissions (using HttpStatusMapper)
- `404 Not Found`: Resource not found
- `405 Method Not Allowed`: Invalid HTTP method
- `409 Conflict`: Duplicate resources (e.g., email exists)

### Response Content Validation
- **Success Responses**: Contains `"success": true`
- **Error Responses**: Contains `"success": false` and appropriate message
- **Token Responses**: Contains valid JWT tokens
- **Data Responses**: Contains expected data structures

### Business Logic Validation
- **Registration**: Creates user records atomically using transactions
- **Authentication**: Returns valid JWT tokens with correct claims
- **Authorization**: Enforces role-based access control
- **File Uploads**: Handles multipart form data correctly
- **Cache**: Mom authorization cache improves performance

## 📈 Continuous Integration

### GitHub Actions Integration
```yaml
name: API Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start MongoDB
        run: # MongoDB setup commands
      - name: Start Server
        run: ./gradlew run &
      - name: Run API Tests
        run: ./tests/main/test-api-endpoints.sh
```

### Local Pre-commit Hook
```bash
# Add to .git/hooks/pre-commit
#!/bin/bash
echo "Running API tests..."
./tests/main/test-api-endpoints.sh
if [ $? -ne 0 ]; then
    echo "API tests failed. Commit aborted."
    exit 1
fi
```

## 🎯 Test Maintenance

### Adding New Tests

1. **Add Test Function**: Create new test function in script
2. **Call from Main**: Add function call to `run_all_tests()`
3. **Update Documentation**: Document new test category here
4. **Validate**: Ensure test covers success and failure scenarios

### Updating Existing Tests

1. **Modify Test Logic**: Update test functions as needed
2. **Adjust Expectations**: Update expected status codes/responses
3. **Test Changes**: Run full test suite to ensure no regressions
4. **Update Docs**: Reflect changes in this documentation

---

## 🏆 Benefits of Automated Testing

- **Regression Prevention**: Catch breaking changes early
- **Confidence**: Deploy with confidence knowing all endpoints work
- **Documentation**: Tests serve as living API documentation
- **Performance Monitoring**: Track response times and cache effectiveness
- **Quality Assurance**: Ensure consistent error handling and status codes

This automated testing system ensures our API improvements are working correctly and provides a reliable foundation for future development! 🚀
