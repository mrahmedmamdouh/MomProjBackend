# Maintain Test Automation

## Overview
Comprehensive guide for maintaining and updating test automation scripts, unit tests, and test data to keep them current with code changes.

## Maintenance Checklist
- [ ] **Test Automation Scripts** - All scripts executable and up-to-date
- [ ] **Unit Tests** - All tests passing and covering new functionality
- [ ] **Test Data** - Test data current and comprehensive
- [ ] **Test Coverage** - Adequate coverage for all features
- [ ] **Test Documentation** - Test procedures documented

## Steps

### 1. Test Automation Scripts Maintenance
```bash
# Check all test scripts
ls -la tests/*.sh

# Make scripts executable
chmod +x tests/*.sh

# Test script functionality
./tests/test-common.sh
```

**Test Scripts to Maintain:**
- `tests/test-auth.sh` - Authentication tests
- `tests/test-mom.sh` - Mom functionality tests
- `tests/test-doctor.sh` - Doctor functionality tests
- `tests/test-admin.sh` - Admin functionality tests
- `tests/test-public.sh` - Public endpoint tests
- `tests/test-security.sh` - Security tests
- `tests/test-common.sh` - Shared test utilities

**Maintenance Tasks:**
- [ ] Update test data for new endpoints
- [ ] Add tests for new functionality
- [ ] Update authentication tokens
- [ ] Fix broken test assertions
- [ ] Update test descriptions

### 2. Unit Tests Maintenance
```bash
# Run all unit tests
./gradlew test --no-daemon

# Run specific test class
./gradlew test --tests "com.evelolvetech.service.MomServiceTest"

# Run tests with verbose output
./gradlew test --info --no-daemon
```

**Unit Test Maintenance:**
- [ ] Add tests for new services
- [ ] Update existing tests for API changes
- [ ] Fix failing tests
- [ ] Add edge case tests
- [ ] Update test data

### 3. Test Data Management
```bash
# Check test data files
ls -la test-files/

# Validate test data
file test-files/*.jpg test-files/*.png
```

**Test Data Files:**
- `test-files/test-photo.jpg` - Profile photo
- `test-files/test-nid-front.jpg` - NID front image
- `test-files/test-nid-back.jpg` - NID back image

**Test Data Maintenance:**
- [ ] Update test images if needed
- [ ] Add new test data for new features
- [ ] Validate file formats and sizes
- [ ] Clean up unused test data

### 4. Test Coverage Analysis
```bash
# Check test coverage
./gradlew test --no-daemon

# Count test files
find src/test/kotlin -name "*.kt" | wc -l

# Count test scripts
ls tests/*.sh | wc -l
```

**Coverage Requirements:**
- [ ] All services have unit tests
- [ ] All API endpoints have integration tests
- [ ] All validation rules are tested
- [ ] All error scenarios are covered
- [ ] All authentication flows are tested

### 5. Test Documentation Updates
```bash
# Check test documentation
grep -r "TODO\|FIXME" tests/ src/test/

# Update test procedures
# Document new test patterns
# Update test data requirements
```

**Documentation Tasks:**
- [ ] Update test procedures
- [ ] Document new test patterns
- [ ] Update test data requirements
- [ ] Document test environment setup
- [ ] Update troubleshooting guides

## Common Update Scenarios

### New API Endpoint Added
1. **Add unit tests** for new service methods
2. **Add integration tests** for new endpoints
3. **Update test data** if needed
4. **Update test documentation**

### Authentication Changes
1. **Update test tokens** in test scripts
2. **Add tests** for new auth flows
3. **Update security tests**
4. **Test token refresh scenarios**

### Validation Rules Changed
1. **Update validation tests** in unit tests
2. **Add API tests** for new validation
3. **Test error scenarios**
4. **Update test data** for validation

### New File Upload Feature
1. **Add test files** for new file types
2. **Update file upload tests**
3. **Test file validation**
4. **Test file size limits**

## Test Maintenance Scripts

### Update Test Scripts
```bash
#!/bin/bash
# update-test-scripts.sh

echo "Updating test automation scripts..."

# Make all scripts executable
chmod +x tests/*.sh

# Check for required functions in test-common.sh
if grep -q "curl_request\|login_mom\|login_doctor" tests/test-common.sh; then
    echo "✅ test-common.sh has required functions"
else
    echo "❌ test-common.sh missing required functions"
fi

# Validate test data files
if [ -d "test-files" ]; then
    echo "✅ Test data directory exists"
    ls -la test-files/
else
    echo "❌ Test data directory missing"
fi

echo "Test script update completed"
```

### Run Test Suite
```bash
#!/bin/bash
# run-test-suite.sh

echo "Running complete test suite..."

# Run unit tests
echo "Running unit tests..."
./gradlew test --no-daemon

# Run API tests
echo "Running API tests..."
./tests/run-all-tests.sh

echo "Test suite completed"
```

### Validate Test Coverage
```bash
#!/bin/bash
# validate-test-coverage.sh

echo "Validating test coverage..."

# Count test files
unit_tests=$(find src/test/kotlin -name "*.kt" | wc -l)
api_tests=$(ls tests/*.sh | wc -l)

echo "Unit tests: $unit_tests"
echo "API tests: $api_tests"

# Check for missing tests
echo "Checking for missing tests..."

# Check if all services have tests
for service in src/main/kotlin/service/*/*.kt; do
    service_name=$(basename "$service" .kt)
    test_file="src/test/kotlin/com/evelolvetech/service/${service_name}Test.kt"
    if [ ! -f "$test_file" ]; then
        echo "⚠️  Missing test for $service_name"
    fi
done

echo "Test coverage validation completed"
```

## Maintenance Schedule

### Daily
- [ ] Run unit tests
- [ ] Check for test failures
- [ ] Validate test scripts

### Weekly
- [ ] Run complete test suite
- [ ] Update test data if needed
- [ ] Review test coverage

### Monthly
- [ ] Comprehensive test review
- [ ] Update test documentation
- [ ] Clean up unused test data
- [ ] Add missing tests

### Before Releases
- [ ] Run all tests
- [ ] Validate test coverage
- [ ] Update test procedures
- [ ] Test in production-like environment

## Quality Checks

### Test Script Quality
```bash
# Check script syntax
bash -n tests/*.sh

# Check for required functions
grep -r "curl_request\|login_mom\|login_doctor" tests/

# Check test data availability
ls -la test-files/
```

### Unit Test Quality
```bash
# Run tests with coverage
./gradlew test --no-daemon

# Check test structure
find src/test/kotlin -name "*Test.kt" | head -5

# Validate test imports
grep -r "import org.junit" src/test/kotlin/
```

### Test Data Quality
```bash
# Check file formats
file test-files/*

# Check file sizes
ls -lh test-files/

# Validate test data integrity
md5sum test-files/*
```

## Best Practices

### Test Maintenance
1. **Keep tests current** with code changes
2. **Add tests immediately** for new features
3. **Fix failing tests** promptly
4. **Update test data** regularly
5. **Document test procedures** clearly

### Test Quality
1. **Write comprehensive tests** for all scenarios
2. **Use descriptive test names** and descriptions
3. **Test both success and error cases**
4. **Keep tests independent** and isolated
5. **Use proper test data** and cleanup

### Test Automation
1. **Automate test execution** where possible
2. **Use consistent test patterns** across all tests
3. **Maintain test scripts** and utilities
4. **Monitor test performance** and reliability
5. **Keep test environment** consistent

## Troubleshooting

### Common Issues
- **Test scripts not executable**: Run `chmod +x tests/*.sh`
- **Missing test functions**: Check `test-common.sh` for required functions
- **Test data missing**: Verify `test-files/` directory and files
- **Unit tests failing**: Check test imports and dependencies
- **API tests failing**: Verify server is running and endpoints are accessible

### Solutions
- **Fix permissions**: Make scripts executable
- **Update functions**: Add missing functions to `test-common.sh`
- **Add test data**: Create missing test files
- **Fix imports**: Update test imports and dependencies
- **Check connectivity**: Verify server and endpoint availability

## Success Metrics

### Test Maintenance Success
- **All test scripts executable** and functional
- **All unit tests passing** and comprehensive
- **All test data available** and valid
- **Adequate test coverage** for all features
- **Test documentation current** and accurate

### Test Quality
- **Tests cover all scenarios** (success and error)
- **Tests are reliable** and consistent
- **Tests run quickly** and efficiently
- **Tests are maintainable** and well-documented
- **Tests catch regressions** effectively

### Test Automation
- **Test execution automated** where possible
- **Test results reliable** and consistent
- **Test maintenance efficient** and systematic
- **Test coverage comprehensive** and current
- **Test procedures documented** and followed
