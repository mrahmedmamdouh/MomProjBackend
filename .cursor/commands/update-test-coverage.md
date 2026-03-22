# Update Test Coverage

## Overview
Comprehensive guide for updating and maintaining test coverage to ensure all features are properly tested.

## Coverage Update Checklist
- [ ] **Unit Test Coverage** - All services and utilities tested
- [ ] **API Test Coverage** - All endpoints and scenarios tested
- [ ] **Integration Test Coverage** - End-to-end workflows tested
- [ ] **Error Scenario Coverage** - All error cases tested
- [ ] **Edge Case Coverage** - Boundary conditions tested

## Steps

### 1. Analyze Current Coverage
```bash
# Count unit test files
find src/test/kotlin -name "*.kt" | wc -l

# Count service files
find src/main/kotlin/service -name "*.kt" | wc -l

# Count API test scripts
ls tests/*.sh | wc -l

# Count route files
find src/main/kotlin/routes -name "*.kt" | wc -l
```

### 2. Identify Coverage Gaps
```bash
# Check for missing unit tests
for service in src/main/kotlin/service/*/*.kt; do
    service_name=$(basename "$service" .kt)
    test_file="src/test/kotlin/com/evelolvetech/service/${service_name}Test.kt"
    if [ ! -f "$test_file" ]; then
        echo "❌ Missing unit test for $service_name"
    fi
done

# Check for missing API tests
for route in src/main/kotlin/routes/*/*.kt; do
    route_name=$(basename "$route" .kt)
    if ! grep -q "$route_name" tests/*.sh; then
        echo "⚠️  Check API test coverage for $route_name"
    fi
done
```

### 3. Add Missing Unit Tests
```bash
# Create unit test template
cat > src/test/kotlin/com/evelolvetech/service/NewServiceTest.kt << 'EOF'
package com.evelolvetech.service

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.koin.test.KoinTest
import org.koin.test.inject

class NewServiceTest : KoinTest {
    
    private val service: NewService by inject()
    
    @Before
    fun setup() {
        // Setup test data
    }
    
    @Test
    fun `test success scenario`() {
        // Test implementation
        val result = service.method()
        assertNotNull(result)
    }
    
    @Test
    fun `test error scenario`() {
        // Test error handling
        try {
            service.methodWithError()
            fail("Expected exception")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }
}
EOF
```

### 4. Add Missing API Tests
```bash
# Add to existing test script or create new one
cat >> tests/test-new-feature.sh << 'EOF'
test_new_feature_management() {
    print_test "New Feature Management"
    
    # Test create new feature
    print_test "Create New Feature"
    local response_body status_code
    curl_request "POST" "/api/new-feature" \
        '{"name":"Test Feature","description":"Test Description"}' \
        "$MOM_TOKEN" \
        response_body status_code
    
    if test_status_code 201 "$status_code" "Create New Feature"; then
        test_json_field "$response_body" "success" "true" "Create Success"
    fi
    
    # Test get new feature
    print_test "Get New Feature"
    curl_request "GET" "/api/new-feature/1" \
        "" \
        "$MOM_TOKEN" \
        response_body status_code
    
    if test_status_code 200 "$status_code" "Get New Feature"; then
        test_json_field "$response_body" "success" "true" "Get Success"
    fi
}

# Run tests
test_new_feature_management
EOF
```

### 5. Update Test Data
```bash
# Add new test data files
mkdir -p test-files

# Add test images
# cp new-test-image.jpg test-files/

# Add test documents
# cp new-test-document.pdf test-files/

# Validate test data
file test-files/*
ls -la test-files/
```

### 6. Update Test Documentation
```bash
# Update test procedures
# Document new test patterns
# Update test data requirements
# Document test environment setup
```

## Coverage Requirements

### Unit Test Coverage
- [ ] **All services tested** - Every service class has unit tests
- [ ] **All utilities tested** - Every utility function has tests
- [ ] **All validation tested** - Every validation rule has tests
- [ ] **All error handling tested** - Every error scenario has tests
- [ ] **All edge cases tested** - Boundary conditions covered

### API Test Coverage
- [ ] **All endpoints tested** - Every API endpoint has tests
- [ ] **All HTTP methods tested** - GET, POST, PUT, DELETE covered
- [ ] **All authentication tested** - JWT and role-based access tested
- [ ] **All validation tested** - Request validation tested
- [ ] **All error responses tested** - Error scenarios covered

### Integration Test Coverage
- [ ] **End-to-end workflows tested** - Complete user journeys tested
- [ ] **Database operations tested** - CRUD operations tested
- [ ] **File uploads tested** - File handling tested
- [ ] **Authentication flows tested** - Login/logout/refresh tested
- [ ] **Authorization boundaries tested** - Role-based access tested

## Test Coverage Scripts

### Coverage Analysis Script
```bash
#!/bin/bash
# analyze-coverage.sh

echo "Analyzing test coverage..."

# Count files
unit_tests=$(find src/test/kotlin -name "*.kt" | wc -l)
services=$(find src/main/kotlin/service -name "*.kt" | wc -l)
api_tests=$(ls tests/*.sh | wc -l)
routes=$(find src/main/kotlin/routes -name "*.kt" | wc -l)

echo "Unit tests: $unit_tests"
echo "Services: $services"
echo "API tests: $api_tests"
echo "Routes: $routes"

# Check coverage gaps
echo "Checking for missing unit tests..."
missing_tests=0
for service in src/main/kotlin/service/*/*.kt; do
    service_name=$(basename "$service" .kt)
    test_file="src/test/kotlin/com/evelolvetech/service/${service_name}Test.kt"
    if [ ! -f "$test_file" ]; then
        echo "❌ Missing unit test for $service_name"
        ((missing_tests++))
    fi
done

echo "Missing unit tests: $missing_tests"

# Check API test coverage
echo "Checking API test coverage..."
for route in src/main/kotlin/routes/*/*.kt; do
    route_name=$(basename "$route" .kt)
    if ! grep -q "$route_name" tests/*.sh; then
        echo "⚠️  Check API test coverage for $route_name"
    fi
done

echo "Coverage analysis completed"
```

### Add Missing Tests Script
```bash
#!/bin/bash
# add-missing-tests.sh

echo "Adding missing tests..."

# Add missing unit tests
for service in src/main/kotlin/service/*/*.kt; do
    service_name=$(basename "$service" .kt)
    test_file="src/test/kotlin/com/evelolvetech/service/${service_name}Test.kt"
    if [ ! -f "$test_file" ]; then
        echo "Creating unit test for $service_name"
        # Create test file template
        cat > "$test_file" << EOF
package com.evelolvetech.service

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.koin.test.KoinTest
import org.koin.test.inject

class ${service_name}Test : KoinTest {
    
    private val service: ${service_name} by inject()
    
    @Before
    fun setup() {
        // Setup test data
    }
    
    @Test
    fun \`test success scenario\`() {
        // Test implementation
        val result = service.method()
        assertNotNull(result)
    }
    
    @Test
    fun \`test error scenario\`() {
        // Test error handling
        try {
            service.methodWithError()
            fail("Expected exception")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }
}
EOF
    fi
done

echo "Missing tests added"
```

## Coverage Maintenance

### Daily Coverage Checks
- [ ] Run unit tests
- [ ] Check for test failures
- [ ] Validate test coverage

### Weekly Coverage Review
- [ ] Analyze coverage gaps
- [ ] Add missing tests
- [ ] Update test data
- [ ] Review test quality

### Monthly Coverage Audit
- [ ] Comprehensive coverage analysis
- [ ] Update test procedures
- [ ] Clean up obsolete tests
- [ ] Plan coverage improvements

### Before Releases
- [ ] Ensure 100% coverage for critical paths
- [ ] Run complete test suite
- [ ] Validate test coverage
- [ ] Update test documentation

## Quality Checks

### Unit Test Quality
```bash
# Check test structure
find src/test/kotlin -name "*Test.kt" | head -5

# Validate test imports
grep -r "import org.junit" src/test/kotlin/

# Check test coverage
./gradlew test --no-daemon
```

### API Test Quality
```bash
# Check test script structure
grep -r "print_test\|curl_request" tests/

# Validate test functions
grep -r "test_status_code\|test_json_field" tests/

# Check test data usage
grep -r "test-files" tests/
```

### Integration Test Quality
```bash
# Check end-to-end tests
grep -r "login_mom\|login_doctor" tests/

# Validate workflow tests
grep -r "test.*management\|test.*flow" tests/

# Check error scenario tests
grep -r "error\|fail\|invalid" tests/
```

## Best Practices

### Test Coverage
1. **Aim for high coverage** but focus on quality
2. **Test critical paths** thoroughly
3. **Cover edge cases** and error scenarios
4. **Keep tests current** with code changes
5. **Document test coverage** requirements

### Test Quality
1. **Write meaningful tests** that catch real issues
2. **Test behavior, not implementation** details
3. **Use descriptive test names** and descriptions
4. **Keep tests independent** and isolated
5. **Use proper test data** and cleanup

### Test Maintenance
1. **Update tests immediately** when adding features
2. **Remove obsolete tests** when removing features
3. **Refactor tests** when code changes
4. **Keep test data current** and valid
5. **Document test procedures** clearly

## Troubleshooting

### Common Issues
- **Low test coverage**: Add missing tests for uncovered code
- **Failing tests**: Fix test code and data
- **Missing test data**: Create required test files
- **Outdated tests**: Update tests for API changes
- **Slow tests**: Optimize test execution

### Solutions
- **Add coverage**: Create tests for untested code
- **Fix tests**: Update test code and assertions
- **Add data**: Create missing test files and data
- **Update tests**: Modify tests for new API behavior
- **Optimize tests**: Improve test performance and efficiency

## Success Metrics

### Coverage Success
- **High test coverage** for critical paths
- **All features tested** comprehensively
- **All error scenarios covered** adequately
- **All edge cases tested** thoroughly
- **Test coverage maintained** over time

### Test Quality
- **Tests catch regressions** effectively
- **Tests validate functionality** correctly
- **Tests are reliable** and consistent
- **Tests are maintainable** and well-documented
- **Tests provide confidence** in code quality

### Test Maintenance
- **Tests stay current** with code changes
- **Test coverage improves** over time
- **Test quality maintained** consistently
- **Test procedures documented** clearly
- **Test maintenance efficient** and systematic
