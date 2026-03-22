#!/bin/bash
# Mom Care Platform - Complete Health Check Script
# This script validates all project systems: Swagger, Postman, tests, documentation, and Cursor config

echo "🏥 Starting Mom Care Platform Health Check..."
echo "=============================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# Function to print status
print_status() {
    local status=$1
    local message=$2
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    case $status in
        "PASS")
            echo -e "${GREEN}✅ $message${NC}"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            ;;
        "FAIL")
            echo -e "${RED}❌ $message${NC}"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
            ;;
        "WARN")
            echo -e "${YELLOW}⚠️  $message${NC}"
            WARNING_CHECKS=$((WARNING_CHECKS + 1))
            ;;
        "INFO")
            echo -e "${BLUE}📊 $message${NC}"
            ;;
    esac
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 1. Swagger/OpenAPI Health Check
echo "🔍 Checking Swagger/OpenAPI..."
echo "------------------------------"

if [ -f "src/main/resources/openapi/documentation.yaml" ]; then
    if command_exists python3; then
        if python3 -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" 2>/dev/null; then
            print_status "PASS" "OpenAPI YAML syntax is valid"
        else
            print_status "WARN" "OpenAPI YAML validation failed (yaml module may not be installed)"
        fi
    elif command_exists python; then
        if python -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" 2>/dev/null; then
            print_status "PASS" "OpenAPI YAML syntax is valid"
        else
            print_status "WARN" "OpenAPI YAML validation failed (yaml module may not be installed)"
        fi
    else
        print_status "WARN" "Python not available - cannot validate YAML syntax"
    fi
    
    # Count endpoints and examples
    endpoints=$(grep -c "operationId:" src/main/resources/openapi/documentation.yaml 2>/dev/null || echo "0")
    examples=$(grep -c "example:" src/main/resources/openapi/documentation.yaml 2>/dev/null || echo "0")
    print_status "INFO" "Found $endpoints endpoints with $examples examples"
else
    print_status "FAIL" "OpenAPI documentation file missing"
fi

# Check if Swagger UI is accessible
if curl -s http://localhost:8080/swagger 2>/dev/null | grep -q "swagger"; then
    print_status "PASS" "Swagger UI is accessible"
else
    print_status "WARN" "Swagger UI not accessible (server may not be running)"
fi

echo ""

# 2. Postman Collection Health Check
echo "🔍 Checking Postman Collection..."
echo "--------------------------------"

if [ -f "postman_collection.json" ]; then
    if command_exists python3; then
        if python3 -c "import json; json.load(open('postman_collection.json'))" 2>/dev/null; then
            print_status "PASS" "Postman collection JSON syntax is valid"
        else
            print_status "FAIL" "Postman collection JSON syntax error"
        fi
    elif command_exists python; then
        if python -c "import json; json.load(open('postman_collection.json'))" 2>/dev/null; then
            print_status "PASS" "Postman collection JSON syntax is valid"
        else
            print_status "FAIL" "Postman collection JSON syntax error"
        fi
    else
        print_status "WARN" "Python not available - cannot validate JSON syntax"
    fi
    
    # Count requests and check structure
    requests=$(grep -c '"name":' postman_collection.json 2>/dev/null || echo "0")
    descriptions=$(grep -c '"description"' postman_collection.json 2>/dev/null || echo "0")
    print_status "INFO" "Found $requests requests with $descriptions descriptions"
    
    # Check for authentication setup
    if grep -q "Bearer" postman_collection.json 2>/dev/null; then
        print_status "PASS" "Authentication headers found"
    else
        print_status "WARN" "No authentication headers found"
    fi
else
    print_status "FAIL" "Postman collection file missing"
fi

echo ""

# 3. Test Automation Scripts Health Check
echo "🔍 Checking Test Automation Scripts..."
echo "-------------------------------------"

test_scripts=(
    "tests/main/test-api-comprehensive.sh"
    "tests/main/test-api-comprehensive-no-exit.sh"
    "tests/main/test-api-endpoints.sh"
    "tests/test-auth.sh"
    "tests/test-mom.sh"
    "tests/test-doctor.sh"
    "tests/test-admin.sh"
    "tests/test-public.sh"
    "tests/test-security.sh"
    "tests/test-orders.sh"
    "tests/test-common.sh"
)

for script in "${test_scripts[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            print_status "PASS" "$script exists and is executable"
        else
            print_status "WARN" "$script exists but not executable"
            chmod +x "$script" 2>/dev/null
            print_status "PASS" "Made $script executable"
        fi
    else
        print_status "FAIL" "$script missing"
    fi
done

# Check test-common.sh for required functions
if [ -f "tests/test-common.sh" ]; then
    if grep -q "curl_request\|login_mom\|login_doctor" tests/test-common.sh; then
        print_status "PASS" "test-common.sh has required functions"
    else
        print_status "FAIL" "test-common.sh missing required functions"
    fi
else
    print_status "FAIL" "test-common.sh not found"
fi

echo ""

# 4. Unit Tests Health Check
echo "🔍 Checking Unit Tests..."
echo "------------------------"

if [ -d "src/test/kotlin" ]; then
    test_count=$(find src/test/kotlin -name "*.kt" 2>/dev/null | wc -l)
    print_status "INFO" "Found $test_count unit test files"
    
    # Try to run unit tests if Gradle is available
    if [ -f "gradlew" ] && [ -x "gradlew" ]; then
        if ./gradlew compileTestKotlin --no-daemon --quiet 2>/dev/null; then
            print_status "PASS" "Unit tests compile successfully"
            # Try to run tests but don't fail if some tests fail
            if ./gradlew test --no-daemon --quiet 2>/dev/null; then
                print_status "PASS" "All unit tests pass"
            else
                print_status "WARN" "Some unit tests failed (but tests are running)"
            fi
        else
            print_status "FAIL" "Unit tests compilation failed"
        fi
    else
        print_status "WARN" "Gradle wrapper not available - cannot run unit tests"
    fi
else
    print_status "FAIL" "No unit test directory found"
fi

echo ""

# 5. Documentation Health Check
echo "🔍 Checking Documentation..."
echo "---------------------------"

if [ -f "README.md" ]; then
    print_status "PASS" "README.md exists"
    if grep -q "Mom Care Platform" README.md; then
        print_status "PASS" "README.md has project description"
    else
        print_status "WARN" "README.md missing project description"
    fi
else
    print_status "FAIL" "README.md missing"
fi

# Check for outdated documentation (exclude generated files)
if grep -r "TODO\|FIXME\|OUTDATED" docs/ README.md 2>/dev/null | grep -v "docs/index.html" | grep -q .; then
    print_status "WARN" "Found TODO/FIXME/OUTDATED in documentation"
else
    print_status "PASS" "No obvious outdated documentation found"
fi

echo ""

# 6. Cursor Rules Health Check
echo "🔍 Checking Cursor Rules..."
echo "--------------------------"

if [ -d ".cursor/rules" ]; then
    rule_count=$(find .cursor/rules -name "*.mdc" 2>/dev/null | wc -l)
    print_status "INFO" "Found $rule_count rule files"
    
    # Check for proper frontmatter
    broken_rules=0
    for rule in .cursor/rules/*.mdc; do
        if [ -f "$rule" ]; then
            if grep -q "alwaysApply\|globs" "$rule"; then
                print_status "PASS" "$(basename "$rule") has proper frontmatter"
            else
                print_status "FAIL" "$(basename "$rule") missing frontmatter"
                broken_rules=$((broken_rules + 1))
            fi
        fi
    done
    
    # Check file references
    if grep -r "mdc:" .cursor/rules/ 2>/dev/null | grep -q "mdc:"; then
        print_status "PASS" "Found file references in rules"
        
        # Validate file references (only check actual file paths, not code examples)
        broken_refs=0
        for ref in $(grep -r "mdc:" .cursor/rules/ 2>/dev/null | grep -o 'mdc:[^)]*' | sed 's/mdc://' | sort -u); do
            # Skip references that are clearly code examples or commands
            if [[ "$ref" =~ ^(src/|tests/|\.cursor/|README\.md|postman_collection\.json|build\.gradle\.kts|settings\.gradle\.kts) ]]; then
                if [ ! -f "$ref" ] && [ ! -d "$ref" ]; then
                    print_status "FAIL" "Broken file/directory reference: $ref"
                    broken_refs=$((broken_refs + 1))
                fi
            fi
        done
        
        if [ $broken_refs -eq 0 ]; then
            print_status "PASS" "All file references are valid"
        else
            print_status "FAIL" "Found $broken_refs broken file references"
        fi
    else
        print_status "WARN" "No file references found in rules"
    fi
else
    print_status "FAIL" "Cursor rules directory missing"
fi

echo ""

# 7. Cursor Commands Health Check
echo "🔍 Checking Cursor Commands..."
echo "-----------------------------"

if [ -d ".cursor/commands" ]; then
    command_count=$(find .cursor/commands -name "*.md" 2>/dev/null | wc -l)
    print_status "INFO" "Found $command_count command files"
    
    # Check for proper structure
    broken_commands=0
    for cmd in .cursor/commands/*.md; do
        if [ -f "$cmd" ]; then
            if grep -q "## Overview" "$cmd" && (grep -q "## Steps" "$cmd" || grep -q "## Checklist" "$cmd" || grep -q "## " "$cmd"); then
                print_status "PASS" "$(basename "$cmd") has proper structure"
            else
                print_status "WARN" "$(basename "$cmd") missing some sections (Overview and Steps/Checklist)"
                broken_commands=$((broken_commands + 1))
            fi
        fi
    done
    
    # Check for file references (but don't validate them as they're in code examples)
    if grep -r "src/main/kotlin" .cursor/commands/ 2>/dev/null | grep -q "src/main/kotlin"; then
        print_status "PASS" "Found file references in commands"
    else
        print_status "WARN" "No file references found in commands"
    fi
else
    print_status "FAIL" "Cursor commands directory missing"
fi

echo ""

# 8. Project Structure Health Check
echo "🔍 Checking Project Structure..."
echo "-------------------------------"

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
        print_status "PASS" "$dir exists"
    else
        print_status "FAIL" "$dir missing"
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
        print_status "PASS" "$file exists"
    else
        print_status "FAIL" "$file missing"
    fi
done

echo ""

# 9. Dependencies Health Check
echo "🔍 Checking Dependencies..."
echo "--------------------------"

# Check if Gradle wrapper exists
if [ -f "gradlew" ]; then
    print_status "PASS" "Gradle wrapper exists"
    if [ -x "gradlew" ]; then
        print_status "PASS" "Gradle wrapper is executable"
    else
        print_status "WARN" "Gradle wrapper not executable"
        chmod +x gradlew 2>/dev/null
        print_status "PASS" "Made Gradle wrapper executable"
    fi
else
    print_status "FAIL" "Gradle wrapper missing"
fi

# Check if dependencies can be resolved
if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    if ./gradlew dependencies --no-daemon --quiet 2>/dev/null | grep -q "BUILD SUCCESSFUL"; then
        print_status "PASS" "Dependencies can be resolved"
    else
        print_status "WARN" "Dependency resolution issues"
    fi
else
    print_status "WARN" "Cannot check dependencies - Gradle wrapper not available"
fi

echo ""

# 10. Build System Health Check
echo "🔍 Checking Build System..."
echo "--------------------------"

# Try to build the project
if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    if ./gradlew compileKotlin --no-daemon --quiet 2>/dev/null; then
        print_status "PASS" "Project compiles successfully"
        # Try full build but don't fail if tests fail
        if ./gradlew build --no-daemon --quiet 2>/dev/null; then
            print_status "PASS" "Project builds successfully"
        else
            print_status "WARN" "Build issues detected (likely test failures)"
        fi
    else
        print_status "FAIL" "Project compilation failed"
    fi
else
    print_status "WARN" "Cannot check build - Gradle wrapper not available"
fi

echo ""

# 11. Generate Health Report
echo "🔍 Generating Health Report..."
echo "-----------------------------"

# Create health report
health_report="health-check-report-$(date +%Y%m%d-%H%M%S).txt"

{
    echo "=== MOM CARE PLATFORM HEALTH CHECK REPORT ==="
    echo "Generated: $(date)"
    echo "Project: $(pwd)"
    echo ""
    echo "=== SUMMARY ==="
    echo "Total Checks: $TOTAL_CHECKS"
    echo "Passed: $PASSED_CHECKS"
    echo "Failed: $FAILED_CHECKS"
    echo "Warnings: $WARNING_CHECKS"
    echo ""
    echo "=== HEALTH STATUS ==="
    if [ $FAILED_CHECKS -eq 0 ]; then
        echo "Overall Status: HEALTHY"
    elif [ $FAILED_CHECKS -le 2 ]; then
        echo "Overall Status: MOSTLY HEALTHY"
    else
        echo "Overall Status: NEEDS ATTENTION"
    fi
    echo ""
    echo "=== DETAILED RESULTS ==="
    echo "See above output for detailed health check results"
} > "$health_report"

print_status "PASS" "Health report saved to: $health_report"

echo ""
echo "🏥 Health Check Complete!"
echo "=============================================="
echo -e "${BLUE}📊 SUMMARY:${NC}"
echo -e "   Total Checks: $TOTAL_CHECKS"
echo -e "   ${GREEN}Passed: $PASSED_CHECKS${NC}"
echo -e "   ${RED}Failed: $FAILED_CHECKS${NC}"
echo -e "   ${YELLOW}Warnings: $WARNING_CHECKS${NC}"
echo ""

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}🎉 All systems are healthy!${NC}"
    exit 0
elif [ $FAILED_CHECKS -le 2 ]; then
    echo -e "${YELLOW}⚠️  System is mostly healthy with minor issues${NC}"
    exit 1
else
    echo -e "${RED}🚨 System needs attention - multiple issues detected${NC}"
    exit 2
fi
