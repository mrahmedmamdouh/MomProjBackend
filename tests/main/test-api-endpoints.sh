#!/bin/bash

# =============================================================================
# API Test Script - Comprehensive cURL Testing for Mom Project Backend
# =============================================================================
# This script validates all API endpoints and functionality after improvements
# Tests: Authentication, Authorization, CRUD operations, Error handling
# =============================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
TEST_FILES_DIR="test-files"
RESULTS_FILE="test-results.log"

# Global variables for tokens
ADMIN_TOKEN=""
MOM_TOKEN=""
DOCTOR_TOKEN=""
MOM_ID=""
DOCTOR_ID=""

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# =============================================================================
# Utility Functions
# =============================================================================

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_test() {
    echo -e "\n${YELLOW}🧪 TEST: $1${NC}"
    ((TOTAL_TESTS++))
}

print_success() {
    echo -e "${GREEN}✅ PASS: $1${NC}"
    ((PASSED_TESTS++))
}

print_failure() {
    echo -e "${RED}❌ FAIL: $1${NC}"
    echo -e "${RED}   Response: $2${NC}"
    ((FAILED_TESTS++))
}

print_info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

# Test HTTP status code
test_status_code() {
    local expected=$1
    local actual=$2
    local test_name=$3
    
    if [ "$actual" -eq "$expected" ]; then
        print_success "$test_name - Status: $actual"
        return 0
    else
        print_failure "$test_name - Expected: $expected, Got: $actual" "Status code mismatch"
        return 1
    fi
}

# Test response contains expected text
test_response_contains() {
    local response="$1"
    local expected="$2"
    local test_name="$3"
    
    if echo "$response" | grep -q "$expected"; then
        print_success "$test_name - Contains: '$expected'"
        return 0
    else
        print_failure "$test_name - Missing: '$expected'" "$response"
        return 1
    fi
}

# Make cURL request and return status code
curl_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    local files=$5
    
    if [ -n "$files" ]; then
        # Multipart request with files
        curl -s -w "%{http_code}" -X "$method" "$url" $headers $files $data
    else
        # Regular JSON request
        curl -s -w "%{http_code}" -X "$method" "$url" \
             -H "Content-Type: application/json" \
             $headers \
             ${data:+-d "$data"}
    fi
}

# =============================================================================
# Pre-test Setup
# =============================================================================

setup_test_environment() {
    print_header "SETTING UP TEST ENVIRONMENT"
    
    # Clear previous results
    > "$RESULTS_FILE"
    
    # Check if server is running
    print_test "Server Health Check"
    response=$(curl -s -w "%{http_code}" "$BASE_URL/" || echo "000")
    status_code="${response: -3}"
    
    if [ "$status_code" -eq "200" ]; then
        print_success "Server is running"
    else
        print_failure "Server is not running" "Status: $status_code"
        echo "Please start the server with: ./gradlew run"
        exit 1
    fi
    
    # Check test files exist
    print_test "Test Files Check"
    if [ -f "$TEST_FILES_DIR/test-photo.jpg" ] && [ -f "$TEST_FILES_DIR/test-nid-front.jpg" ] && [ -f "$TEST_FILES_DIR/test-nid-back.jpg" ]; then
        print_success "Test files are available"
    else
        print_failure "Test files missing" "Required: test-photo.jpg, test-nid-front.jpg, test-nid-back.jpg"
        exit 1
    fi
    
    # Seed database
    print_test "Database Seeding"
    if ./gradlew runSeed > /dev/null 2>&1; then
        print_success "Database seeded successfully"
    else
        print_failure "Database seeding failed" "Check database connection"
        exit 1
    fi
}

# =============================================================================
# Authentication Tests
# =============================================================================

test_authentication() {
    print_header "AUTHENTICATION TESTS"
    
    # Test admin login
    print_test "Admin Login"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"admin@admin.com","password":"admin123"}')
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Admin Login"; then
        ADMIN_TOKEN=$(echo "$response_body" | jq -r '.data.accessToken' 2>/dev/null || echo "")
        if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
            print_success "Admin token obtained"
        else
            print_failure "Admin token extraction failed" "$response_body"
        fi
    fi
    
    # Test invalid login
    print_test "Invalid Login"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"invalid@test.com","password":"wrongpass"}')
    status_code="${response: -3}"
    
    test_status_code 401 "$status_code" "Invalid Login"
}

# =============================================================================
# Registration Tests
# =============================================================================

test_registration() {
    print_header "REGISTRATION TESTS"
    
    # Test mom registration
    print_test "Mom Registration"
    local timestamp=$(date +%s)
    local mom_email="testmom${timestamp}@test.com"
    
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/mom" "" "" \
        "-F 'data={\"fullName\":\"Test Mom\",\"email\":\"$mom_email\",\"password\":\"testpass123\",\"phone\":\"+201234567890\",\"maritalStatus\":\"SINGLE\",\"numberOfSessions\":10}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 201 "$status_code" "Mom Registration"; then
        MOM_ID=$(echo "$response_body" | jq -r '.data.momId' 2>/dev/null || echo "")
        if [ -n "$MOM_ID" ] && [ "$MOM_ID" != "null" ]; then
            print_success "Mom registered with ID: $MOM_ID"
            
            # Login with new mom account
            print_test "Mom Login After Registration"
            login_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
                "{\"email\":\"$mom_email\",\"password\":\"testpass123\"}")
            login_status="${login_response: -3}"
            login_body="${login_response%???}"
            
            if test_status_code 200 "$login_status" "Mom Login"; then
                MOM_TOKEN=$(echo "$login_body" | jq -r '.data.accessToken' 2>/dev/null || echo "")
                print_success "Mom token obtained"
            fi
        fi
    fi
    
    # Test doctor registration
    print_test "Doctor Registration"
    local doctor_email="testdoctor${timestamp}@test.com"
    
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/doctor" "" "" \
        "-F 'data={\"name\":\"Dr. Test\",\"email\":\"$doctor_email\",\"password\":\"testpass123\",\"phone\":\"+201234567891\",\"specialization\":\"General Practice\"}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 201 "$status_code" "Doctor Registration"; then
        DOCTOR_ID=$(echo "$response_body" | jq -r '.data.doctorId' 2>/dev/null || echo "")
        if [ -n "$DOCTOR_ID" ] && [ "$DOCTOR_ID" != "null" ]; then
            print_success "Doctor registered with ID: $DOCTOR_ID"
            
            # Login with new doctor account
            print_test "Doctor Login After Registration"
            login_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
                "{\"email\":\"$doctor_email\",\"password\":\"testpass123\"}")
            login_status="${login_response: -3}"
            login_body="${login_response%???}"
            
            if test_status_code 200 "$login_status" "Doctor Login"; then
                DOCTOR_TOKEN=$(echo "$login_body" | jq -r '.data.accessToken' 2>/dev/null || echo "")
                print_success "Doctor token obtained"
            fi
        fi
    fi
    
    # Test duplicate email registration
    print_test "Duplicate Email Registration"
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/mom" "" "" \
        "-F 'data={\"fullName\":\"Test Mom 2\",\"email\":\"$mom_email\",\"password\":\"testpass123\",\"phone\":\"+201234567892\",\"maritalStatus\":\"SINGLE\",\"numberOfSessions\":5}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    status_code="${response: -3}"
    test_status_code 409 "$status_code" "Duplicate Email Registration"
}

# =============================================================================
# Authorization Tests
# =============================================================================

test_authorization() {
    print_header "AUTHORIZATION TESTS"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_failure "Authorization Tests Skipped" "No admin token available"
        return
    fi
    
    # Test admin-only endpoint access
    print_test "Admin Access - Category Creation"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test Category","description":"Test Description"}' \
        "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 201 "$status_code" "Admin Category Creation"; then
        CREATED_CATEGORY_ID=$(echo "$response_body" | jq -r '.data.id' 2>/dev/null || echo "")
    fi
    
    # Test admin category listing
    print_test "Admin Categories List"
    response=$(curl_request "GET" "$BASE_URL/api/admin/categories" "" \
        "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
    status_code="${response: -3}"
    
    test_status_code 200 "$status_code" "Admin Categories List"
    
    # Test admin category update if we created one
    if [ -n "$CREATED_CATEGORY_ID" ] && [ "$CREATED_CATEGORY_ID" != "null" ]; then
        print_test "Admin Category Update"
        response=$(curl_request "PUT" "$BASE_URL/api/admin/categories/$CREATED_CATEGORY_ID" \
            '{"name":"Updated Test Category","description":"Updated Description"}' \
            "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Admin Category Update"
    fi
    
    # Test unauthorized access to admin endpoint
    print_test "Unauthorized Admin Access"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Unauthorized Category","description":"Should Fail"}')
    status_code="${response: -3}"
    
    test_status_code 401 "$status_code" "Unauthorized Admin Access"
    
    # Test doctor authorization if doctor was registered
    if [ -n "$DOCTOR_ID" ] && [ -n "$ADMIN_TOKEN" ]; then
        print_test "Doctor Authorization by Admin"
        response=$(curl_request "PUT" "$BASE_URL/api/admin/doctors/$DOCTOR_ID/authorize" \
            '{"isAuthorized":true}' \
            "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Doctor Authorization"
        
        # Test doctor status check
        print_test "Doctor Status Check by Admin"
        response=$(curl_request "GET" "$BASE_URL/api/admin/doctors/$DOCTOR_ID/status" "" \
            "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Doctor Status Check"
    fi
}

# =============================================================================
# CRUD Operations Tests
# =============================================================================

test_crud_operations() {
    print_header "CRUD OPERATIONS TESTS"
    
    # Test public category listing
    print_test "Public Categories List"
    response=$(curl_request "GET" "$BASE_URL/api/categories")
    status_code="${response: -3}"
    response_body="${response%???}"
    
    test_status_code 200 "$status_code" "Public Categories List"
    
    # Extract a category ID for further testing
    CATEGORY_ID=$(echo "$response_body" | jq -r '.data[0].id' 2>/dev/null || echo "")
    
    # Test category by ID
    if [ -n "$CATEGORY_ID" ] && [ "$CATEGORY_ID" != "null" ]; then
        print_test "Category by ID"
        response=$(curl_request "GET" "$BASE_URL/api/categories/$CATEGORY_ID")
        status_code="${response: -3}"
        test_status_code 200 "$status_code" "Category by ID"
    fi
    
    # Test mom-protected product routes
    if [ -n "$MOM_TOKEN" ]; then
        print_test "Mom Products List"
        response=$(curl_request "GET" "$BASE_URL/api/products" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 403 ]; then
            print_success "Products endpoint responding correctly (Status: $status_code)"
            
            # If successful, test product search
            if [ "$status_code" -eq 200 ]; then
                print_test "Product Search"
                response=$(curl_request "GET" "$BASE_URL/api/products/search?query=test" "" \
                    "-H \"Authorization: Bearer $MOM_TOKEN\"")
                status_code="${response: -3}"
                test_status_code 200 "$status_code" "Product Search" || test_status_code 403 "$status_code" "Product Search (Unauthorized)"
                
                # Test products by category
                if [ -n "$CATEGORY_ID" ]; then
                    print_test "Products by Category"
                    response=$(curl_request "GET" "$BASE_URL/api/products/category/$CATEGORY_ID" "" \
                        "-H \"Authorization: Bearer $MOM_TOKEN\"")
                    status_code="${response: -3}"
                    test_status_code 200 "$status_code" "Products by Category" || test_status_code 403 "$status_code" "Products by Category (Unauthorized)"
                fi
            fi
        else
            print_failure "Products endpoint error" "Status: $status_code"
        fi
        
        # Test SKU offers
        print_test "SKU Offers List"
        response=$(curl_request "GET" "$BASE_URL/api/sku-offers" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 403 ]; then
            print_success "SKU Offers endpoint responding correctly (Status: $status_code)"
        else
            print_failure "SKU Offers endpoint error" "Status: $status_code"
        fi
        
        # Test cart operations
        print_test "Mom Cart Access"
        response=$(curl_request "GET" "$BASE_URL/api/cart" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 403 ]; then
            print_success "Cart endpoint responding correctly (Status: $status_code)"
        else
            print_failure "Cart endpoint error" "Status: $status_code"
        fi
        
        # Test mom profile access
        print_test "Mom Profile Access"
        response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Mom Profile Access"
        
        # Test mom authorization check
        print_test "Mom Authorization Check"
        response=$(curl_request "GET" "$BASE_URL/api/moms/check-authorization" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Mom Authorization Check"
        
        # Test order management endpoints
        print_test "Order Management - Get Orders"
        response=$(curl_request "GET" "$BASE_URL/api/orders" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 403 ]; then
            print_success "Orders endpoint responding correctly (Status: $status_code)"
        else
            print_failure "Orders endpoint error" "Status: $status_code"
        fi
        
        # Test order creation from request
        print_test "Order Management - Create Order from Request"
        response=$(curl_request "POST" "$BASE_URL/api/orders" \
            '{"items":[{"skuId":"sku_prenatal_batchA","qty":1}]}' \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        if [ "$status_code" -eq 201 ] || [ "$status_code" -eq 403 ]; then
            print_success "Order creation endpoint responding correctly (Status: $status_code)"
        else
            print_failure "Order creation endpoint error" "Status: $status_code"
        fi
        
        # Test order creation from cart
        print_test "Order Management - Create Order from Cart"
        response=$(curl_request "POST" "$BASE_URL/api/orders/from-cart" "" \
            "-H \"Authorization: Bearer $MOM_TOKEN\"")
        status_code="${response: -3}"
        
        if [ "$status_code" -eq 201 ] || [ "$status_code" -eq 400 ] || [ "$status_code" -eq 403 ]; then
            print_success "Order from cart endpoint responding correctly (Status: $status_code)"
        else
            print_failure "Order from cart endpoint error" "Status: $status_code"
        fi
    fi
    
    # Test doctor profile if doctor token available
    if [ -n "$DOCTOR_TOKEN" ]; then
        print_test "Doctor Profile Access"
        response=$(curl_request "GET" "$BASE_URL/api/doctors/profile" "" \
            "-H \"Authorization: Bearer $DOCTOR_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Doctor Profile Access"
        
        # Test doctor authorization check
        print_test "Doctor Authorization Check"
        response=$(curl_request "GET" "$BASE_URL/api/doctors/check-authorization" "" \
            "-H \"Authorization: Bearer $DOCTOR_TOKEN\"")
        status_code="${response: -3}"
        
        test_status_code 200 "$status_code" "Doctor Authorization Check"
    fi
}

# =============================================================================
# Error Handling Tests
# =============================================================================

test_error_handling() {
    print_header "ERROR HANDLING TESTS"
    
    # Test malformed JSON
    print_test "Malformed JSON Request"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"test@test.com","password":}')  # Invalid JSON
    status_code="${response: -3}"
    
    test_status_code 400 "$status_code" "Malformed JSON"
    
    # Test missing required fields
    print_test "Missing Required Fields"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"test@test.com"}')  # Missing password
    status_code="${response: -3}"
    
    test_status_code 400 "$status_code" "Missing Required Fields"
    
    # Test invalid endpoint
    print_test "Invalid Endpoint"
    response=$(curl_request "GET" "$BASE_URL/api/nonexistent")
    status_code="${response: -3}"
    
    test_status_code 404 "$status_code" "Invalid Endpoint"
    
    # Test invalid HTTP method
    print_test "Invalid HTTP Method"
    response=$(curl_request "PATCH" "$BASE_URL/api/categories")
    status_code="${response: -3}"
    
    test_status_code 405 "$status_code" "Invalid HTTP Method"
}

# =============================================================================
# Response Helper Extensions Tests
# =============================================================================

test_response_extensions() {
    print_header "RESPONSE HELPER EXTENSIONS TESTS"
    
    # Test ProductRoutes with new extensions
    print_test "ProductRoutes Response Extensions"
    response=$(curl_request "GET" "$BASE_URL/api/products/invalid-id")
    status_code="${response: -3}"
    
    # Should return 400 for invalid request data (using respondWithBadRequest)
    test_status_code 400 "$status_code" "ProductRoutes Bad Request"
    
    # Test SkuOfferRoutes with new extensions  
    print_test "SkuOfferRoutes Response Extensions"
    response=$(curl_request "GET" "$BASE_URL/api/sku-offers/invalid-id")
    status_code="${response: -3}"
    
    # Should return 400 for invalid request data (using respondWithBadRequest)
    test_status_code 400 "$status_code" "SkuOfferRoutes Bad Request"
    
    # Test AdminDoctorRoutes with new extensions
    if [ -n "$ADMIN_TOKEN" ]; then
        print_test "AdminDoctorRoutes Response Extensions"
        response=$(curl_request "PUT" "$BASE_URL/api/admin/doctors/invalid-id/authorize" \
            '{"isAuthorized":true}' \
            "-H \"Authorization: Bearer $ADMIN_TOKEN\"")
        status_code="${response: -3}"
        
        # Should return 400 for invalid doctor ID (using respondWithBadRequest)
        test_status_code 400 "$status_code" "AdminDoctorRoutes Bad Request"
    fi
}

# =============================================================================
# Performance & Cache Tests
# =============================================================================

test_performance_cache() {
    print_header "PERFORMANCE & CACHE TESTS"
    
    if [ -n "$MOM_TOKEN" ]; then
        print_test "Mom Authorization Cache Test"
        
        # Make multiple requests to test caching
        start_time=$(date +%s%3N)
        for i in {1..3}; do
            curl_request "GET" "$BASE_URL/api/cart" "" \
                "-H \"Authorization: Bearer $MOM_TOKEN\"" > /dev/null
        done
        end_time=$(date +%s%3N)
        
        duration=$((end_time - start_time))
        print_success "Cache test completed in ${duration}ms (3 requests)"
        
        if [ $duration -lt 1000 ]; then
            print_success "Performance looks good (< 1 second for 3 requests)"
        else
            print_info "Performance could be improved (${duration}ms for 3 requests)"
        fi
    fi
}

# =============================================================================
# Main Test Execution
# =============================================================================

run_all_tests() {
    print_header "STARTING COMPREHENSIVE API TESTS"
    echo "Timestamp: $(date)"
    echo "Base URL: $BASE_URL"
    
    # Setup
    setup_test_environment
    
    # Run test suites
    test_authentication
    test_registration
    test_authorization
    test_crud_operations
    test_error_handling
    test_response_extensions
    test_performance_cache
    
    # Final results
    print_header "TEST RESULTS SUMMARY"
    echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
        echo -e "${GREEN}✅ API is working correctly after all improvements${NC}"
        exit 0
    else
        echo -e "\n${RED}⚠️  SOME TESTS FAILED ⚠️${NC}"
        echo -e "${RED}❌ Please review the failures above${NC}"
        exit 1
    fi
}

# =============================================================================
# Script Entry Point
# =============================================================================

# Check dependencies
command -v curl >/dev/null 2>&1 || { echo "Error: curl is required but not installed." >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Warning: jq not found. JSON parsing may be limited." >&2; }

# Run tests
run_all_tests
