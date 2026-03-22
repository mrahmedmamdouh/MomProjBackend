#!/bin/bash

# =============================================================================
# Common Test Utilities - Shared functions for all test scripts
# =============================================================================
# This file contains shared utilities, configuration, and helper functions
# used across all API test scripts in the comprehensive testing suite
# =============================================================================

# Colors for output
export RED='\033[0;31m'
export GREEN='\033[0;32m'
export YELLOW='\033[1;33m'
export BLUE='\033[0;34m'
export PURPLE='\033[0;35m'
export CYAN='\033[0;36m'
export NC='\033[0m' # No Color

# Configuration
export BASE_URL="${BASE_URL:-http://localhost:8080}"
export TEST_FILES_DIR="${TEST_FILES_DIR:-test-files}"
export RESULTS_FILE="${RESULTS_FILE:-test-results.log}"

# Global variables for tokens and IDs
export ADMIN_TOKEN=""
export MOM_TOKEN=""
export DOCTOR_TOKEN=""
export MOM_ID=""
export DOCTOR_ID=""
export TEST_CATEGORY_ID=""
export TEST_PRODUCT_ID=""
export TEST_SKU_OFFER_ID=""

# Test counters
export TOTAL_TESTS=0
export PASSED_TESTS=0
export FAILED_TESTS=0

# =============================================================================
# Output Functions
# =============================================================================

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_subheader() {
    echo -e "\n${PURPLE}--- $1 ---${NC}"
}

print_test() {
    echo -e "\n${YELLOW}🧪 TEST: $1${NC}"
    ((TOTAL_TESTS++))
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((PASSED_TESTS++))
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    ((FAILED_TESTS++))
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# =============================================================================
# HTTP Request Functions
# =============================================================================

# Generic curl request function
curl_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local token="$4"
    local additional_args="$5"
    
    local headers=()
    if [ -n "$token" ]; then
        headers+=("-H" "Authorization: Bearer $token")
    fi
    
    if [ -n "$data" ] && [ "$additional_args" != *"-F"* ]; then
        headers+=("-H" "Content-Type: application/json")
    fi
    
    local curl_cmd=(curl -s -w "%{http_code}" -X "$method")
    
    # Add headers
    for header in "${headers[@]}"; do
        curl_cmd+=("$header")
    done
    
    # Add data if provided
    if [ -n "$data" ]; then
        curl_cmd+=("-d" "$data")
    fi
    
    # Add additional arguments (for multipart forms, etc.)
    if [ -n "$additional_args" ]; then
        eval "curl_cmd+=($additional_args)"
    fi
    
    # Add URL
    curl_cmd+=("$url")
    
    # Execute and return response
    "${curl_cmd[@]}"
}

# =============================================================================
# Test Validation Functions
# =============================================================================

test_status_code() {
    local expected="$1"
    local actual="$2"
    local test_name="$3"
    
    if [ "$expected" = "$actual" ]; then
        print_success "$test_name - Status: $actual"
        return 0
    else
        print_error "$test_name - Expected: $expected, Got: $actual"
        return 1
    fi
}

test_json_field() {
    local json="$1"
    local field="$2"
    local expected="$3"
    local test_name="$4"
    
    local actual
    actual=$(echo "$json" | jq -r ".$field" 2>/dev/null || echo "null")
    
    if [ "$actual" = "$expected" ]; then
        print_success "$test_name - $field: $actual"
        return 0
    else
        print_error "$test_name - $field: Expected '$expected', Got '$actual'"
        return 1
    fi
}

test_json_not_null() {
    local json="$1"
    local field="$2"
    local test_name="$3"
    
    local value
    value=$(echo "$json" | jq -r ".$field" 2>/dev/null || echo "null")
    
    if [ "$value" != "null" ] && [ -n "$value" ]; then
        print_success "$test_name - $field: $value"
        return 0
    else
        print_error "$test_name - $field is null or empty"
        return 1
    fi
}

# =============================================================================
# Authentication Helper Functions
# =============================================================================

admin_login() {
    print_test "Admin Login"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"admin@momcare.com","password":"admin123"}')
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Admin Login"; then
        ADMIN_TOKEN=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null || echo "")
        if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
            print_success "Admin token obtained"
            return 0
        else
            print_error "Failed to extract admin token"
            return 1
        fi
    else
        print_error "Admin login failed"
        return 1
    fi
}

mom_login() {
    print_test "Mom Login (Beth - Authorized)"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"beth@example.com","password":"password123"}')
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Mom Login"; then
        MOM_TOKEN=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null || echo "")
        MOM_ID=$(echo "$response_body" | jq -r '.data.userId' 2>/dev/null || echo "")
        if [ -n "$MOM_TOKEN" ] && [ "$MOM_TOKEN" != "null" ]; then
            print_success "Mom token obtained for user: $MOM_ID"
            return 0
        else
            print_error "Failed to extract mom token"
            return 1
        fi
    else
        print_error "Mom login failed"
        return 1
    fi
}

doctor_login() {
    print_test "Doctor Login (Dr. Brown)"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"dr.brown@example.com","password":"password123"}')
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Doctor Login"; then
        DOCTOR_TOKEN=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null || echo "")
        DOCTOR_ID=$(echo "$response_body" | jq -r '.data.userId' 2>/dev/null || echo "")
        if [ -n "$DOCTOR_TOKEN" ] && [ "$DOCTOR_TOKEN" != "null" ]; then
            print_success "Doctor token obtained for user: $DOCTOR_ID"
            return 0
        else
            print_error "Failed to extract doctor token"
            return 1
        fi
    else
        print_error "Doctor login failed"
        return 1
    fi
}

# =============================================================================
# Test Results Functions
# =============================================================================

print_test_summary() {
    local suite_name="$1"
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$suite_name - TEST RESULTS${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 ALL $suite_name TESTS PASSED! 🎉${NC}"
        return 0
    else
        echo -e "\n${RED}⚠️  SOME $suite_name TESTS FAILED ⚠️${NC}"
        return 1
    fi
}

reset_counters() {
    TOTAL_TESTS=0
    PASSED_TESTS=0
    FAILED_TESTS=0
}

# =============================================================================
# Dependency Checks
# =============================================================================

check_dependencies() {
    command -v curl >/dev/null 2>&1 || { 
        echo -e "${RED}Error: curl is required but not installed.${NC}" >&2
        exit 1
    }
    
    command -v jq >/dev/null 2>&1 || { 
        echo -e "${YELLOW}Warning: jq not found. JSON parsing may be limited.${NC}" >&2
    }
}

# =============================================================================
# Test Data Generation
# =============================================================================

generate_timestamp() {
    date +%s
}

generate_test_email() {
    local prefix="$1"
    local timestamp
    timestamp=$(generate_timestamp)
    echo "${prefix}${timestamp}@test.com"
}

# =============================================================================
# Server Health Check
# =============================================================================

check_server_health() {
    print_test "Server Health Check"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/categories")
    
    local status_code="${response: -3}"
    
    if [ "$status_code" = "200" ]; then
        print_success "Server is running and responding"
        return 0
    else
        print_error "Server is not responding (Status: $status_code)"
        return 1
    fi
}

# Ensure doctor is authorized (auto-authorize if needed)
ensure_doctor_authorized() {
    # First, ensure admin is logged in
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Check if doctor is authorized
    local response
    response=$(curl_request "GET" "$BASE_URL/api/admin/doctors/doc_brown/status" "" "$ADMIN_TOKEN")
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if [ "$status_code" = "200" ]; then
        local is_authorized=$(echo "$response_body" | jq -r '.data.isAuthorized' 2>/dev/null || echo "false")
        
        if [ "$is_authorized" = "false" ]; then
            print_info "Auto-authorizing doctor (Dr. Brown)..."
            
            # Authorize the doctor
            local auth_response
            auth_response=$(curl_request "PUT" "$BASE_URL/api/admin/doctors/doc_brown/authorize" '{"isAuthorized": true}' "$ADMIN_TOKEN")
            local auth_status_code="${auth_response: -3}"
            
            if [ "$auth_status_code" = "200" ]; then
                print_success "Doctor authorized successfully"
            else
                print_warning "Failed to auto-authorize doctor (Status: $auth_status_code)"
            fi
        else
            print_info "Doctor is already authorized"
        fi
    else
        print_warning "Could not check doctor authorization status (Status: $status_code)"
    fi
}

# Export all functions
export -f print_header print_subheader print_test print_success print_error print_warning print_info
export -f curl_request test_status_code test_json_field test_json_not_null
export -f admin_login mom_login doctor_login ensure_doctor_authorized
export -f print_test_summary reset_counters check_dependencies
export -f generate_timestamp generate_test_email check_server_health
