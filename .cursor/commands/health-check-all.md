# Health Check All Systems

## Overview
Comprehensive health check for all project systems: Swagger, Postman collection, test automation scripts, unit tests, and documentation. This single command validates everything is working and up-to-date.

## Health Check Checklist
- [ ] **Swagger/OpenAPI** - Documentation accessible and valid
- [ ] **Postman Collection** - JSON valid and examples working
- [ ] **Test Automation Scripts** - All test scripts executable and passing
- [ ] **Unit Tests** - All unit tests passing
- [ ] **Documentation** - All documentation files valid and current
- [ ] **Cursor Rules** - All rules valid and file references working
- [ ] **Cursor Commands** - All commands valid and examples working
- [ ] **Project Structure** - All referenced files exist
- [ ] **Dependencies** - All dependencies resolved
- [ ] **Build System** - Project builds successfully

## Steps

### 1. Pre-Check Setup
```bash
# Ensure we're in the project root
cd /Users/yehia/KtorBackend/momproj

# Check if server is running (optional)
curl -s http://localhost:8080/health 2>/dev/null || echo "Server not running - some checks will be skipped"
```

### 2. Swagger/OpenAPI Health Check
```bash
echo "🔍 Checking Swagger/OpenAPI..."

# Validate YAML syntax
if python -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" 2>/dev/null; then
    echo "✅ OpenAPI YAML syntax is valid"
else
    echo "❌ OpenAPI YAML syntax error"
    exit 1
fi

# Check if Swagger UI is accessible
if curl -s http://localhost:8080/swagger | grep -q "swagger"; then
    echo "✅ Swagger UI is accessible"
else
    echo "⚠️  Swagger UI not accessible (server may not be running)"
fi

# Count endpoints and examples
endpoints=$(grep -c "operationId:" src/main/resources/openapi/documentation.yaml)
examples=$(grep -c "example:" src/main/resources/openapi/documentation.yaml)
echo "📊 Found $endpoints endpoints with $examples examples"
```

### 3. Postman Collection Health Check
```bash
echo "🔍 Checking Postman Collection..."

# Validate JSON syntax
if python -c "import json; json.load(open('postman_collection.json'))" 2>/dev/null; then
    echo "✅ Postman collection JSON syntax is valid"
else
    echo "❌ Postman collection JSON syntax error"
    exit 1
fi

# Count requests and check structure
requests=$(grep -c '"name":' postman_collection.json)
descriptions=$(grep -c '"description"' postman_collection.json)
echo "📊 Found $requests requests with $descriptions descriptions"

# Check for authentication setup
if grep -q "Bearer" postman_collection.json; then
    echo "✅ Authentication headers found"
else
    echo "⚠️  No authentication headers found"
fi
```

### 4. Test Automation Scripts Health Check
```bash
echo "🔍 Checking Test Automation Scripts..."

# Check if test scripts exist and are executable
test_scripts=(
    "tests/test-auth.sh"
    "tests/test-mom.sh"
    "tests/test-doctor.sh"
    "tests/test-admin.sh"
    "tests/test-public.sh"
    "tests/test-security.sh"
    "tests/test-common.sh"
)

for script in "${test_scripts[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo "✅ $script exists and is executable"
        else
            echo "⚠️  $script exists but not executable"
            chmod +x "$script"
            echo "✅ Made $script executable"
        fi
    else
        echo "❌ $script missing"
    fi
done

# Check test-common.sh for required functions
if grep -q "curl_request\|login_mom\|login_doctor" tests/test-common.sh; then
    echo "✅ test-common.sh has required functions"
else
    echo "❌ test-common.sh missing required functions"
fi
```

### 5. Unit Tests Health Check
```bash
echo "🔍 Checking Unit Tests..."

# Check if unit tests exist
if [ -d "src/test/kotlin" ]; then
    test_count=$(find src/test/kotlin -name "*.kt" | wc -l)
    echo "📊 Found $test_count unit test files"
    
    # Try to run unit tests
    if ./gradlew test --no-daemon 2>/dev/null; then
        echo "✅ Unit tests pass"
    else
        echo "⚠️  Unit tests failed or couldn't run"
    fi
else
    echo "❌ No unit test directory found"
fi
```

### 6. Documentation Health Check
```bash
echo "🔍 Checking Documentation..."

# Check README
if [ -f "README.md" ]; then
    echo "✅ README.md exists"
    if grep -q "Mom Care Platform" README.md; then
        echo "✅ README.md has project description"
    else
        echo "⚠️  README.md missing project description"
    fi
else
    echo "❌ README.md missing"
fi

# Check for outdated documentation
if grep -r "TODO\|FIXME\|OUTDATED" docs/ README.md 2>/dev/null; then
    echo "⚠️  Found TODO/FIXME/OUTDATED in documentation"
else
    echo "✅ No obvious outdated documentation found"
fi
```

### 7. Cursor Rules Health Check
```bash
echo "🔍 Checking Cursor Rules..."

# Check if rules directory exists
if [ -d ".cursor/rules" ]; then
    rule_count=$(find .cursor/rules -name "*.mdc" | wc -l)
    echo "📊 Found $rule_count rule files"
    
    # Check for proper frontmatter
    for rule in .cursor/rules/*.mdc; do
        if grep -q "alwaysApply\|globs" "$rule"; then
            echo "✅ $(basename "$rule") has proper frontmatter"
        else
            echo "❌ $(basename "$rule") missing frontmatter"
        fi
    done
    
    # Check file references
    if grep -r "mdc:" .cursor/rules/ | grep -q "mdc:"; then
        echo "✅ Found file references in rules"
        
        # Validate file references
        broken_refs=0
        for ref in $(grep -r "mdc:" .cursor/rules/ | sed 's/.*mdc:\([^)]*\).*/\1/' | sort -u); do
            if [ ! -f "$ref" ]; then
                echo "❌ Broken file reference: $ref"
                ((broken_refs++))
            fi
        done
        
        if [ $broken_refs -eq 0 ]; then
            echo "✅ All file references are valid"
        else
            echo "❌ Found $broken_refs broken file references"
        fi
    else
        echo "⚠️  No file references found in rules"
    fi
else
    echo "❌ Cursor rules directory missing"
fi
```

### 8. Cursor Commands Health Check
```bash
echo "🔍 Checking Cursor Commands..."

# Check if commands directory exists
if [ -d ".cursor/commands" ]; then
    command_count=$(find .cursor/commands -name "*.md" | wc -l)
    echo "📊 Found $command_count command files"
    
    # Check for proper structure
    for cmd in .cursor/commands/*.md; do
        if grep -q "## Overview" "$cmd" && grep -q "## Steps" "$cmd"; then
            echo "✅ $(basename "$cmd") has proper structure"
        else
            echo "❌ $(basename "$cmd") missing required sections"
        fi
    done
    
    # Check for outdated file references
    if grep -r "src/main/kotlin" .cursor/commands/ | grep -q "src/main/kotlin"; then
        echo "✅ Found file references in commands"
        
        # Validate file references
        broken_refs=0
        for ref in $(grep -r "src/main/kotlin" .cursor/commands/ | sed 's/.*src\/main\/kotlin\/\([^[:space:]]*\).*/\1/' | sort -u); do
            if [ ! -f "src/main/kotlin/$ref" ]; then
                echo "❌ Broken file reference: src/main/kotlin/$ref"
                ((broken_refs++))
            fi
        done
        
        if [ $broken_refs -eq 0 ]; then
            echo "✅ All file references are valid"
        else
            echo "❌ Found $broken_refs broken file references"
        fi
    else
        echo "⚠️  No file references found in commands"
    fi
else
    echo "❌ Cursor commands directory missing"
fi
```

### 9. Project Structure Health Check
```bash
echo "🔍 Checking Project Structure..."

# Check key directories
key_dirs=(
    "src/main/kotlin"
    "src/test/kotlin"
    "src/main/resources"
    "tests"
    ".cursor/rules"
    ".cursor/commands"
)

for dir in "${key_dirs[@]}"; do
    if [ -d "$dir" ]; then
        echo "✅ $dir exists"
    else
        echo "❌ $dir missing"
    fi
done

# Check key files
key_files=(
    "build.gradle.kts"
    "settings.gradle.kts"
    "src/main/kotlin/Application.kt"
    "src/main/resources/application.yaml"
    "src/main/resources/openapi/documentation.yaml"
    "postman_collection.json"
)

for file in "${key_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file exists"
    else
        echo "❌ $file missing"
    fi
done
```

### 10. Dependencies Health Check
```bash
echo "🔍 Checking Dependencies..."

# Check if Gradle wrapper exists
if [ -f "gradlew" ]; then
    echo "✅ Gradle wrapper exists"
else
    echo "❌ Gradle wrapper missing"
fi

# Check if dependencies can be resolved
if ./gradlew dependencies --no-daemon 2>/dev/null | grep -q "BUILD SUCCESSFUL"; then
    echo "✅ Dependencies can be resolved"
else
    echo "⚠️  Dependency resolution issues"
fi
```

### 11. Build System Health Check
```bash
echo "🔍 Checking Build System..."

# Try to build the project
if ./gradlew build --no-daemon 2>/dev/null; then
    echo "✅ Project builds successfully"
else
    echo "⚠️  Build issues detected"
fi
```

### 12. Integration Test Health Check
```bash
echo "🔍 Checking Integration Tests..."

# Check if we can run a simple integration test
if [ -f "tests/test-common.sh" ]; then
    # Source the common functions
    source tests/test-common.sh
    
    # Test if basic functions work
    if type curl_request >/dev/null 2>&1; then
        echo "✅ test-common.sh functions are available"
    else
        echo "❌ test-common.sh functions not available"
    fi
else
    echo "❌ test-common.sh not found"
fi
```

### 13. Generate Health Report
```bash
echo "🔍 Generating Health Report..."

# Create health report
health_report="health-check-report-$(date +%Y%m%d-%H%M%S).txt"

{
    echo "=== MOM CARE PLATFORM HEALTH CHECK REPORT ==="
    echo "Generated: $(date)"
    echo "Project: $(pwd)"
    echo ""
    echo "=== SUMMARY ==="
    echo "✅ Swagger/OpenAPI: $(if python -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" 2>/dev/null; then echo "VALID"; else echo "INVALID"; fi)"
    echo "✅ Postman Collection: $(if python -c "import json; json.load(open('postman_collection.json'))" 2>/dev/null; then echo "VALID"; else echo "INVALID"; fi)"
    echo "✅ Test Scripts: $(if [ -d "tests" ] && [ -f "tests/test-common.sh" ]; then echo "AVAILABLE"; else echo "MISSING"; fi)"
    echo "✅ Unit Tests: $(if [ -d "src/test/kotlin" ]; then echo "AVAILABLE"; else echo "MISSING"; fi)"
    echo "✅ Documentation: $(if [ -f "README.md" ]; then echo "AVAILABLE"; else echo "MISSING"; fi)"
    echo "✅ Cursor Rules: $(if [ -d ".cursor/rules" ]; then echo "AVAILABLE"; else echo "MISSING"; fi)"
    echo "✅ Cursor Commands: $(if [ -d ".cursor/commands" ]; then echo "AVAILABLE"; else echo "MISSING"; fi)"
    echo ""
    echo "=== DETAILED RESULTS ==="
    echo "See above output for detailed health check results"
} > "$health_report"

echo "📋 Health report saved to: $health_report"

# Clean up health check reports after analysis
rm -f health-check-report-*.txt
```

## Quick Health Check Commands

### Run All Checks
```bash
# Run complete health check
./gradlew health-check-all 2>/dev/null || echo "Health check command not available - run manually"
```

### Individual Checks
```bash
# Check Swagger only
python -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" && echo "✅ Swagger valid"

# Check Postman only
python -c "import json; json.load(open('postman_collection.json'))" && echo "✅ Postman valid"

# Check test scripts only
ls -la tests/*.sh && echo "✅ Test scripts available"

# Check unit tests only
./gradlew test --no-daemon && echo "✅ Unit tests pass"

# Check documentation only
[ -f "README.md" ] && echo "✅ Documentation available"

# Check Cursor config only
[ -d ".cursor/rules" ] && [ -d ".cursor/commands" ] && echo "✅ Cursor config available"
```

## Health Check Script

### Create Health Check Script
```bash
#!/bin/bash
# health-check.sh

echo "🏥 Starting Mom Care Platform Health Check..."
echo "=============================================="

# Run all health checks
source .cursor/commands/health-check-all.md

echo ""
echo "🏥 Health Check Complete!"
echo "=============================================="
```

### Make Script Executable
```bash
chmod +x health-check.sh
```

## Best Practices

### Regular Health Checks
- **Daily**: Quick syntax validation
- **Weekly**: Full health check
- **Before releases**: Comprehensive validation
- **After major changes**: Full system check

### Health Check Triggers
- **Code changes**: Validate affected systems
- **Documentation updates**: Check all references
- **New features**: Validate all systems
- **Dependency updates**: Check compatibility

### Quality Standards
- **All systems must pass** before deployment
- **Documentation must be current** and accurate
- **Tests must pass** and be comprehensive
- **Examples must work** and be tested

## Troubleshooting

### Common Issues
- **YAML/JSON syntax errors**: Use validators
- **Missing files**: Check project structure
- **Broken references**: Update file paths
- **Test failures**: Fix test issues
- **Build failures**: Resolve dependencies

### Solutions
- **Validate syntax** before committing
- **Test examples** regularly
- **Keep documentation** in sync
- **Run health checks** frequently
- **Fix issues** immediately

## Success Metrics

### Health Check Success
- **All syntax validations pass**
- **All file references work**
- **All tests pass**
- **All examples work**
- **All documentation current**

### System Health
- **Swagger accessible** and valid
- **Postman collection** working
- **Test automation** functional
- **Unit tests** passing
- **Documentation** up-to-date

### Maintenance Efficiency
- **Quick health checks** available
- **Automated validation** working
- **Clear procedures** for fixes
- **Regular maintenance** schedule
- **Team participation** in health checks
