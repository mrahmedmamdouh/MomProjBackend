# Run Test Suite

## Overview
Quick command to run the complete test suite including unit tests, API tests, and test automation scripts.

## Steps

### 1. Run Complete Test Suite
```bash
# Run comprehensive API tests
./tests/main/test-api-comprehensive.sh

# Run comprehensive tests (no exit on failure)
./tests/main/test-api-comprehensive-no-exit.sh

# Run all API endpoints
./tests/main/test-api-endpoints.sh

# Run unit tests only
./gradlew test --no-daemon
```

### 2. Run Individual Test Suites
```bash
# Authentication tests
./tests/test-auth.sh

# Mom functionality tests
./tests/test-mom.sh

# Doctor functionality tests
./tests/test-doctor.sh

# Admin functionality tests
./tests/test-admin.sh

# Public endpoint tests
./tests/test-public.sh

# Security tests
./tests/test-security.sh
```

### 3. Run Specific Tests
```bash
# Run specific unit test class
./gradlew test --tests "com.evelolvetech.service.MomServiceTest"

# Run tests with verbose output
./gradlew test --info --no-daemon

# Run tests with coverage
./gradlew test --no-daemon
```

### 4. Test Results Analysis
```bash
# Save comprehensive test results to file
./tests/main/test-api-comprehensive.sh > comprehensive-test-results.log 2>&1

# Save test results (no exit on failure)
./tests/main/test-api-comprehensive-no-exit.sh > test-results.log 2>&1

# View test results
cat comprehensive-test-results.log
cat test-results.log

# Check for failures
grep -i "fail\|error" comprehensive-test-results.log
grep -i "fail\|error" test-results.log

# Clean up log files after analysis
rm -f comprehensive-test-results.log test-results.log
```

## Test Suite Components

### Unit Tests
- **Location**: `src/test/kotlin/`
- **Count**: 24 test files
- **Command**: `./gradlew test --no-daemon`
- **Coverage**: Service layer, validation, utilities

### API Tests
- **Location**: `tests/` and root directory
- **Count**: 7 individual test scripts + 3 comprehensive scripts
- **Commands**: 
  - `./test-api-comprehensive.sh` - Complete comprehensive tests
  - `./test-api-comprehensive-no-exit.sh` - Comprehensive tests (no exit on failure)
  - `./test-api-endpoints.sh` - All API endpoints
- **Coverage**: All API endpoints, authentication, authorization, comprehensive scenarios

### Test Automation Scripts
- **test-api-comprehensive.sh**: Complete comprehensive API tests
- **test-api-comprehensive-no-exit.sh**: Comprehensive tests (no exit on failure)
- **test-api-endpoints.sh**: All API endpoints testing
- **test-auth.sh**: Authentication and JWT token tests
- **test-mom.sh**: Mom profile and functionality tests
- **test-doctor.sh**: Doctor profile and functionality tests
- **test-admin.sh**: Admin operations and management tests
- **test-public.sh**: Public endpoints and registration tests
- **test-security.sh**: Security and authorization tests
- **test-common.sh**: Shared test utilities and functions

## Quick Test Commands

### Run All Tests
```bash
# Comprehensive API tests
./test-api-comprehensive.sh

# Comprehensive tests (no exit on failure)
./test-api-comprehensive-no-exit.sh

# All API endpoints
./test-api-endpoints.sh
```

### Run Unit Tests
```bash
# All unit tests
./gradlew test --no-daemon

# Specific test class
./gradlew test --tests "MomServiceTest"

# With verbose output
./gradlew test --info --no-daemon
```

### Run API Tests
```bash
# Comprehensive API tests
./test-api-comprehensive.sh

# Comprehensive tests (no exit on failure)
./test-api-comprehensive-no-exit.sh

# All API endpoints
./test-api-endpoints.sh

# Individual test suites
./tests/test-auth.sh
./tests/test-mom.sh
./tests/test-doctor.sh
./tests/test-admin.sh
./tests/test-public.sh
./tests/test-security.sh
```

### Test Validation
```bash
# Check test scripts are executable
ls -la tests/*.sh

# Validate test data
ls -la test-files/

# Check test coverage
find src/test/kotlin -name "*.kt" | wc -l
```

## Test Results

### Success Indicators
- ✅ **Unit tests pass** - All JUnit tests successful
- ✅ **API tests pass** - All endpoint tests successful
- ✅ **Test scripts executable** - All bash scripts run
- ✅ **Test data available** - All test files present
- ✅ **Coverage adequate** - All features tested

### Failure Indicators
- ❌ **Unit test failures** - JUnit tests failing
- ❌ **API test failures** - Endpoint tests failing
- ❌ **Script errors** - Bash script execution errors
- ❌ **Missing test data** - Test files not found
- ❌ **Coverage gaps** - Features not tested

## Test Maintenance

### Before Running Tests
- [ ] Ensure server is running (`./gradlew run`)
- [ ] Check test scripts are executable
- [ ] Verify test data files exist
- [ ] Validate test environment

### After Running Tests
- [ ] Review test results
- [ ] Fix any failing tests
- [ ] Update test data if needed
- [ ] Document test issues

### Regular Maintenance
- [ ] Run tests daily during development
- [ ] Run complete suite before commits
- [ ] Run tests after code changes
- [ ] Update tests for new features

## Troubleshooting

### Common Issues
- **Server not running**: Start server with `./gradlew run`
- **Scripts not executable**: Run `chmod +x tests/*.sh`
- **Missing test data**: Check `test-files/` directory
- **Test failures**: Review test output and fix issues
- **Coverage gaps**: Add missing tests

### Solutions
- **Start server**: `./gradlew run &`
- **Fix permissions**: `chmod +x tests/*.sh`
- **Add test data**: Create missing test files
- **Fix tests**: Update test code and data
- **Add coverage**: Create new tests for untested features

## Best Practices

### Test Execution
1. **Run tests frequently** during development
2. **Run complete suite** before commits
3. **Fix failing tests** immediately
4. **Keep tests current** with code changes
5. **Document test issues** and solutions

### Test Quality
1. **Write comprehensive tests** for all scenarios
2. **Test both success and error cases**
3. **Use descriptive test names** and descriptions
4. **Keep tests independent** and isolated
5. **Use proper test data** and cleanup

### Test Maintenance
1. **Update tests** when adding new features
2. **Remove obsolete tests** when removing features
3. **Keep test data current** and valid
4. **Maintain test scripts** and utilities
5. **Document test procedures** clearly

## Success Metrics

### Test Execution Success
- **All tests pass** consistently
- **Tests run quickly** and efficiently
- **Test results reliable** and consistent
- **Test coverage comprehensive** and current
- **Test maintenance efficient** and systematic

### Test Quality
- **Tests catch regressions** effectively
- **Tests validate functionality** correctly
- **Tests cover edge cases** and error scenarios
- **Tests are maintainable** and well-documented
- **Tests provide confidence** in code quality
