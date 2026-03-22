#!/bin/bash

# =============================================================================
# Public API Tests - No Authentication Required
# =============================================================================
# Tests: Category endpoints that are accessible without authentication
# Coverage: GET /api/categories, GET /api/categories/{id}
# =============================================================================

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# =============================================================================
# Category Public Tests
# =============================================================================

test_public_categories() {
    print_subheader "Public Category Endpoints"
    
    # Test get all categories
    print_test "Get All Categories (Public)"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/categories")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get All Categories"; then
        test_json_field "$response_body" "success" "true" "Categories Success"
        
        # Extract category ID for subsequent tests
        TEST_CATEGORY_ID=$(echo "$response_body" | jq -r '.data[0].id' 2>/dev/null || echo "")
        if [ -n "$TEST_CATEGORY_ID" ] && [ "$TEST_CATEGORY_ID" != "null" ]; then
            print_success "Found test category ID: $TEST_CATEGORY_ID"
        else
            print_warning "No categories found in database"
        fi
    fi
    
    # Test get category by ID (if we have a category ID)
    if [ -n "$TEST_CATEGORY_ID" ] && [ "$TEST_CATEGORY_ID" != "null" ]; then
        print_test "Get Category By ID (Public)"
        response=$(curl_request "GET" "$BASE_URL/api/categories/$TEST_CATEGORY_ID")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Get Category By ID"; then
            test_json_field "$response_body" "success" "true" "Category By ID Success"
            test_json_field "$response_body" "data.id" "$TEST_CATEGORY_ID" "Category ID Match"
            test_json_not_null "$response_body" "data.name" "Category Name"
        fi
    fi
    
    # Test get non-existent category
    print_test "Get Non-Existent Category"
    response=$(curl_request "GET" "$BASE_URL/api/categories/nonexistent-id")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 404 "$status_code" "Non-Existent Category"; then
        test_json_field "$response_body" "success" "false" "Non-Existent Category Error"
    fi
}

test_public_api_structure() {
    print_subheader "Public API Structure Tests"
    
    # Test API response format consistency
    print_test "API Response Format Consistency"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/categories")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Response Format"; then
        # Check required fields exist
        local success_field
        success_field=$(echo "$response_body" | jq -r '.success' 2>/dev/null || echo "null")
        
        local data_field
        data_field=$(echo "$response_body" | jq -r '.data' 2>/dev/null || echo "null")
        
        if [ "$success_field" = "true" ] && [ "$data_field" != "null" ]; then
            print_success "API response has required fields (success, data)"
        else
            print_failure "API response missing required fields"
        fi
        
        # Check if data is array for categories endpoint
        local is_array
        is_array=$(echo "$response_body" | jq -r '.data | type' 2>/dev/null || echo "null")
        
        if [ "$is_array" = "array" ]; then
            print_success "Categories data is properly formatted as array"
        else
            print_failure "Categories data should be an array"
        fi
    fi
}

test_public_cors_headers() {
    print_subheader "CORS and Headers Tests"
    
    # Test CORS preflight request
    print_test "CORS Preflight Request"
    local response
    response=$(curl -s -w "%{http_code}" -X OPTIONS "$BASE_URL/api/categories" \
        -H "Origin: https://example.com" \
        -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: Content-Type")
    
    local status_code="${response: -3}"
    
    # CORS might return 200, 204, or 404 depending on configuration
    if [[ "$status_code" =~ ^(200|204|404)$ ]]; then
        print_success "CORS preflight handled (Status: $status_code)"
    else
        print_warning "CORS preflight returned unexpected status: $status_code"
    fi
}

test_public_error_handling() {
    print_subheader "Public API Error Handling"
    
    # Test invalid endpoint
    print_test "Invalid Endpoint"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/invalid-endpoint")
    
    local status_code="${response: -3}"
    
    # Should return 404 for non-existent endpoints
    if test_status_code 404 "$status_code" "Invalid Endpoint"; then
        print_success "Proper 404 handling for invalid endpoints"
    fi
    
    # Test malformed request
    print_test "Malformed Category ID"
    response=$(curl_request "GET" "$BASE_URL/api/categories/")
    
    status_code="${response: -3}"
    
    # Should handle malformed requests gracefully
    if [[ "$status_code" =~ ^(400|404)$ ]]; then
        print_success "Proper error handling for malformed requests (Status: $status_code)"
    else
        print_warning "Unexpected status for malformed request: $status_code"
    fi
}

test_public_performance() {
    print_subheader "Public API Performance Tests"
    
    # Test response time for categories endpoint
    print_test "Categories Response Time"
    local start_time
    start_time=$(date +%s%N)
    
    local response
    response=$(curl_request "GET" "$BASE_URL/api/categories")
    
    local end_time
    end_time=$(date +%s%N)
    
    local duration_ms
    duration_ms=$(( (end_time - start_time) / 1000000 ))
    
    local status_code="${response: -3}"
    
    if test_status_code 200 "$status_code" "Categories Performance"; then
        if [ $duration_ms -lt 1000 ]; then
            print_success "Categories response time: ${duration_ms}ms (Good)"
        elif [ $duration_ms -lt 3000 ]; then
            print_warning "Categories response time: ${duration_ms}ms (Acceptable)"
        else
            print_warning "Categories response time: ${duration_ms}ms (Slow)"
        fi
    fi
}

# =============================================================================
# Main Test Runner
# =============================================================================

run_public_tests() {
    print_header "PUBLIC API TESTS"
    
    # Check server health
    check_server_health || exit 1
    
    # Run test suites
    test_public_categories
    test_public_api_structure
    test_public_cors_headers
    test_public_error_handling
    test_public_performance
    
    # Print results
    print_test_summary "PUBLIC API"
    return $?
}

# =============================================================================
# Script Entry Point
# =============================================================================

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being run directly
    check_dependencies
    run_public_tests
    exit $?
fi
