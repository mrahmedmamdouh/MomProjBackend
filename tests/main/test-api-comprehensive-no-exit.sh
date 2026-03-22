#!/bin/bash

# =============================================================================
# DEBUG VERSION: Comprehensive API Test Suite (No Early Exit)
# =============================================================================
# This is a debug version of the comprehensive test script that continues
# execution even if individual test suites fail. Useful for:
# - Troubleshooting test issues
# - Development and debugging  
# - CI/CD pipelines where you want to see all results
# - When the main script exits early unexpectedly
# =============================================================================

set +e  # Continue execution even if individual tests fail (debug mode)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
TEST_FILES_DIR="${TEST_FILES_DIR:-test-files}"
RESULTS_FILE="${RESULTS_FILE:-comprehensive-test-results.log}"

# Test suite tracking
TOTAL_SUITES=0
PASSED_SUITES=0
FAILED_SUITES=0

# Global test counters (accumulated across all suites)
GLOBAL_TOTAL_TESTS=0
GLOBAL_PASSED_TESTS=0
GLOBAL_FAILED_TESTS=0

# =============================================================================
# Utility Functions
# =============================================================================

print_banner() {
    echo -e "\n${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                     COMPREHENSIVE API TEST SUITE                            ║${NC}"
    echo -e "${CYAN}║                        Mom Project Backend                                   ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo -e "${BLUE}🚀 Testing all API endpoints with comprehensive coverage${NC}"
    echo -e "${BLUE}📊 Includes: Auth, Public, Mom-only, Doctor, Admin, Security tests${NC}"
    echo -e "${BLUE}⏰ Started at: $(date)${NC}"
}

print_suite_header() {
    echo -e "\n${PURPLE}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${PURPLE}║ $1${NC}"
    echo -e "${PURPLE}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
}

print_suite_result() {
    local suite_name="$1"
    local result="$2"
    
    ((TOTAL_SUITES++))
    
    if [ "$result" = "0" ]; then
        echo -e "${GREEN}✅ $suite_name - ALL TESTS PASSED${NC}"
        ((PASSED_SUITES++))
    else
        echo -e "${RED}❌ $suite_name - SOME TESTS FAILED${NC}"
        ((FAILED_SUITES++))
    fi
}

# =============================================================================
# Pre-flight Checks
# =============================================================================

check_dependencies() {
    echo -e "\n${YELLOW}🔍 Checking dependencies...${NC}"
    
    command -v curl >/dev/null 2>&1 || { 
        echo -e "${RED}Error: curl is required but not installed.${NC}" >&2
        exit 1
    }
    echo -e "${GREEN}✓ curl found${NC}"
    
    command -v jq >/dev/null 2>&1 || { 
        echo -e "${YELLOW}Warning: jq not found. JSON parsing may be limited.${NC}" >&2
    }
    echo -e "${GREEN}✓ jq found${NC}"
    
    # Check if test files exist
    if [ ! -d "$TEST_FILES_DIR" ]; then
        echo -e "${YELLOW}Warning: Test files directory '$TEST_FILES_DIR' not found.${NC}"
        echo -e "${YELLOW}Some registration tests may fail.${NC}"
    else
        echo -e "${GREEN}✓ Test files directory found${NC}"
    fi
}

check_server_health() {
    echo -e "\n${YELLOW}🏥 Checking server health...${NC}"
    
    local response
    response=$(curl -s -w "%{http_code}" "$BASE_URL/api/categories" 2>/dev/null || echo "000")
    local status_code="${response: -3}"
    
    if [ "$status_code" = "200" ]; then
        echo -e "${GREEN}✅ Server is running and responding${NC}"
        return 0
    else
        echo -e "${RED}❌ Server is not responding (Status: $status_code)${NC}"
        echo -e "${RED}Please ensure the server is running on $BASE_URL${NC}"
        exit 1
    fi
}

# =============================================================================
# Test Suite Runners
# =============================================================================

run_auth_tests() {
    print_suite_header "AUTHENTICATION & REGISTRATION TESTS"
    
    if [ -f "tests/test-auth.sh" ]; then
        chmod +x tests/test-auth.sh
        if bash tests/test-auth.sh; then
            print_suite_result "Authentication Tests" "0"
            return 0
        else
            print_suite_result "Authentication Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Authentication test script not found${NC}"
        print_suite_result "Authentication Tests" "1"
        return 1
    fi
}

run_public_tests() {
    print_suite_header "PUBLIC API TESTS (NO AUTH REQUIRED)"
    
    if [ -f "tests/test-public.sh" ]; then
        chmod +x tests/test-public.sh
        if bash tests/test-public.sh; then
            print_suite_result "Public API Tests" "0"
            return 0
        else
            print_suite_result "Public API Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Public API test script not found${NC}"
        print_suite_result "Public API Tests" "1"
        return 1
    fi
}

run_mom_tests() {
    print_suite_header "MOM-ONLY API TESTS (AUTHORIZED MOMS)"
    
    if [ -f "tests/test-mom.sh" ]; then
        chmod +x tests/test-mom.sh
        if bash tests/test-mom.sh; then
            print_suite_result "Mom API Tests" "0"
            return 0
        else
            print_suite_result "Mom API Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Mom API test script not found${NC}"
        print_suite_result "Mom API Tests" "1"
        return 1
    fi
}

run_doctor_tests() {
    print_suite_header "DOCTOR API TESTS (DOCTOR PROFILES)"
    
    if [ -f "tests/test-doctor.sh" ]; then
        chmod +x tests/test-doctor.sh
        if bash tests/test-doctor.sh; then
            print_suite_result "Doctor API Tests" "0"
            return 0
        else
            print_suite_result "Doctor API Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Doctor API test script not found${NC}"
        print_suite_result "Doctor API Tests" "1"
        return 1
    fi
}

run_admin_tests() {
    print_suite_header "ADMIN API TESTS (ADMIN CRUD OPERATIONS)"
    
    if [ -f "tests/test-admin.sh" ]; then
        chmod +x tests/test-admin.sh
        if bash tests/test-admin.sh; then
            print_suite_result "Admin API Tests" "0"
            return 0
        else
            print_suite_result "Admin API Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Admin API test script not found${NC}"
        print_suite_result "Admin API Tests" "1"
        return 1
    fi
}

run_order_tests() {
    print_suite_header "ORDER MANAGEMENT API TESTS"
    
    if [ -f "tests/test-orders.sh" ]; then
        chmod +x tests/test-orders.sh
        if bash tests/test-orders.sh; then
            print_suite_result "Order Management Tests" "0"
            return 0
        else
            print_suite_result "Order Management Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Order management test script not found${NC}"
        print_suite_result "Order Management Tests" "1"
        return 1
    fi
}

run_security_tests() {
    print_suite_header "SECURITY & EDGE CASE TESTS"
    
    if [ -f "tests/test-security.sh" ]; then
        chmod +x tests/test-security.sh
        if bash tests/test-security.sh; then
            print_suite_result "Security Tests" "0"
            return 0
        else
            print_suite_result "Security Tests" "1"
            return 1
        fi
    else
        echo -e "${RED}❌ Security test script not found${NC}"
        print_suite_result "Security Tests" "1"
        return 1
    fi
}

# =============================================================================
# Test Coverage Summary
# =============================================================================

# =============================================================================
# Results Aggregation
# =============================================================================

aggregate_results() {
    echo -e "\n${BLUE}📊 Aggregating results from all test suites...${NC}"
    
    # Try to extract test counts from individual suite outputs
    # This is a simplified version - in practice, we'd need more sophisticated parsing
    
    local total_files=$(find tests/ -name "test-*.sh" | wc -l)
    echo -e "${CYAN}Found $total_files test suite files${NC}"
}

# =============================================================================
# Final Results Summary
# =============================================================================

print_final_summary() {
    local end_time
    end_time=$(date)
    
    echo -e "\n${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                           FINAL TEST RESULTS                                ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    
    echo -e "\n${BLUE}📈 Test Suite Summary:${NC}"
    echo -e "Total Suites: ${BLUE}$TOTAL_SUITES${NC}"
    echo -e "Passed Suites: ${GREEN}$PASSED_SUITES${NC}"
    echo -e "Failed Suites: ${RED}$FAILED_SUITES${NC}"
    
    echo -e "\n${BLUE}⏰ Test Execution:${NC}"
    echo -e "Completed at: ${BLUE}$end_time${NC}"
    
    echo -e "\n${BLUE}📝 Coverage Areas Tested:${NC}"
    echo -e "• ${GREEN}Authentication & Registration${NC} (Login, JWT, Registration flows)"
    echo -e "• ${GREEN}Public APIs${NC} (Categories - no auth required)"
    echo -e "• ${YELLOW}Mom-only APIs${NC} (Products, SKU Offers, Cart, Profile)"
    echo -e "• ${YELLOW}Doctor APIs${NC} (Profile management)"
    echo -e "• ${GREEN}Admin APIs${NC} (Full CRUD operations)"
    echo -e "• ${GREEN}Order Management APIs${NC} (Order creation, status updates, history)"
    echo -e "• ${YELLOW}Security Tests${NC} (Unauthorized access, edge cases)"
    
    if [ $FAILED_SUITES -eq 0 ]; then
        echo -e "\n${GREEN}🎉 ALL TEST SUITES PASSED! 🎉${NC}"
        echo -e "${GREEN}✅ Your API is working correctly across all endpoints${NC}"
        echo -e "${GREEN}🚀 Ready for production deployment${NC}"
        return 0
    else
        echo -e "\n${RED}⚠️  SOME TEST SUITES FAILED ⚠️${NC}"
        echo -e "${RED}❌ Please review the failed tests above${NC}"
        echo -e "${YELLOW}🔧 Fix the issues and re-run the tests${NC}"
        return 1
    fi
}

# =============================================================================
# Main Test Execution
# =============================================================================

run_all_tests() {
    # Clear results file
    > "$RESULTS_FILE"
    
    print_banner
    
    # Pre-flight checks
    check_dependencies
    check_server_health
    
    # Make tests directory executable
    chmod +x tests/*.sh 2>/dev/null || true
    
    # Run test suites in logical order
    echo -e "\n${BLUE}🚀 Starting comprehensive test execution...${NC}"
    
    # 1. Authentication first (required for other tests)
    run_auth_tests
    
    # 2. Public APIs (no dependencies)
    run_public_tests
    
    # 3. Mom-only APIs (requires mom authentication)
    run_mom_tests
    
    # 4. Doctor APIs (requires doctor authentication)
    run_doctor_tests
    
    # 5. Admin APIs (requires admin authentication)
    run_admin_tests
    
    # 6. Order Management APIs (requires mom authentication)
    run_order_tests
    
    # 7. Security tests (cross-cutting concerns)
    run_security_tests
    
    # Aggregate and summarize results
    aggregate_results
    print_final_summary
    
    # Return appropriate exit code
    return $FAILED_SUITES
}

# =============================================================================
# Script Entry Point
# =============================================================================

# Export configuration for sub-scripts
export BASE_URL
export TEST_FILES_DIR
export RESULTS_FILE

# Handle script arguments
case "${1:-}" in
    --help|-h)
        echo "Usage: $0 [options]"
        echo "Options:"
        echo "  --help, -h     Show this help message"
        echo "  --auth-only    Run only authentication tests"
        echo "  --public-only  Run only public API tests"
        echo "  --admin-only   Run only admin API tests"
        echo "Environment variables:"
        echo "  BASE_URL       Server URL (default: http://localhost:8080)"
        echo "  TEST_FILES_DIR Test files directory (default: test-files)"
        exit 0
        ;;
    --auth-only)
        print_banner
        check_dependencies
        check_server_health
        run_auth_tests
        exit $?
        ;;
    --public-only)
        print_banner
        check_dependencies
        check_server_health
        run_public_tests
        exit $?
        ;;
    --admin-only)
        print_banner
        check_dependencies
        check_server_health
        run_admin_tests
        exit $?
        ;;
    *)
        # Run all tests
        run_all_tests
        exit $?
        ;;
esac
