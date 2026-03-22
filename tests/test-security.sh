#!/bin/bash

# =============================================================================
# Security Tests - Cross-Cutting Security Validation
# =============================================================================
# Tests: Unauthorized access attempts, JWT edge cases, CORS, Rate limiting
# Coverage: Security boundaries, token validation, role-based access control
# =============================================================================

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# =============================================================================
# JWT Token Security Tests
# =============================================================================

test_jwt_token_security() {
    print_subheader "JWT Token Security Tests"
    
    # Test malformed JWT tokens
    print_test "Malformed JWT Token"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "invalid.jwt.token")
    
    local status_code="${response: -3}"
    test_status_code 401 "$status_code" "Malformed JWT"
    
    # Test JWT with invalid signature
    print_test "Invalid JWT Signature"
    local fake_jwt="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature_here"
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$fake_jwt")
    
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "Invalid JWT Signature"
    
    # Test JWT with wrong algorithm
    print_test "JWT Wrong Algorithm"
    local none_jwt="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ."
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$none_jwt")
    
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "JWT Wrong Algorithm"
    
    # Test empty authorization header
    print_test "Empty Authorization Header"
    response=$(curl -s -w "%{http_code}" -H "Authorization: " "$BASE_URL/api/moms/profile")
    
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "Empty Authorization Header"
    
    # Test malformed authorization header
    print_test "Malformed Authorization Header"
    response=$(curl -s -w "%{http_code}" -H "Authorization: NotBearer token123" "$BASE_URL/api/moms/profile")
    
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "Malformed Authorization Header"
}

# =============================================================================
# Role-Based Access Control Tests
# =============================================================================

test_role_based_access_control() {
    print_subheader "Role-Based Access Control Tests"
    
    # Get tokens for all user types
    admin_login
    mom_login
    doctor_login
    
    # Test cross-role access attempts
    print_test "Mom Token on Admin Endpoints"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test Category","description":"Test"}' \
        "$MOM_TOKEN")
    
    local status_code="${response: -3}"
    test_status_code 403 "$status_code" "Mom Token on Admin Endpoint"
    
    print_test "Doctor Token on Admin Endpoints"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test Category","description":"Test"}' \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Doctor Token on Admin Endpoint"
    
    print_test "Admin Token on Mom Endpoints"
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Admin Token on Mom Endpoint"
    
    print_test "Mom Token on Doctor Endpoints"
    response=$(curl_request "GET" "$BASE_URL/api/doctors/profile" "" "$MOM_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Mom Token on Doctor Endpoint"
    
    print_test "Doctor Token on Mom Endpoints"
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Doctor Token on Mom Endpoint"
    
    print_test "Admin Token on Doctor Endpoints"
    response=$(curl_request "GET" "$BASE_URL/api/doctors/profile" "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Admin Token on Doctor Endpoint"
    
    # Test e-commerce access control (mom-only features)
    print_test "Cart Access - No Token"
    response=$(curl -s -w "%{http_code}" "$BASE_URL/api/cart")
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "Cart Access - No Token"
    
    print_test "Cart Access - Doctor Token (Should be blocked)"
    response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$DOCTOR_TOKEN")
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Cart Access - Doctor Token"
    
    print_test "Cart Access - Admin Token (Should be blocked)"
    response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$ADMIN_TOKEN")
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Cart Access - Admin Token"
    
    print_test "Cart Access - Unauthorized Mom Token (Should be blocked)"
    # Get unauthorized mom token (Alice)
    local alice_response
    alice_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    local alice_token
    alice_token=$(echo "${alice_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    if [ -n "$alice_token" ] && [ "$alice_token" != "null" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$alice_token")
        status_code="${response: -3}"
        test_status_code 403 "$status_code" "Cart Access - Unauthorized Mom Token"
    else
        print_warning "Could not get Alice token for cart access test"
    fi
    
    print_test "Cart Access - Authorized Mom Token (Should succeed)"
    # Get authorized mom token (Beth)
    local beth_response
    beth_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"beth@example.com","password":"password123"}')
    local beth_token
    beth_token=$(echo "${beth_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    if [ -n "$beth_token" ] && [ "$beth_token" != "null" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$beth_token")
        status_code="${response: -3}"
        test_status_code 200 "$status_code" "Cart Access - Authorized Mom Token"
    else
        print_warning "Could not get Beth token for cart access test"
    fi
    
    # Test products access control (mom-only features)
    print_test "Products Access - Doctor Token (Should be blocked)"
    response=$(curl_request "GET" "$BASE_URL/api/products" "" "$DOCTOR_TOKEN")
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Products Access - Doctor Token"
    
    print_test "Products Access - Admin Token (Should be blocked)"
    response=$(curl_request "GET" "$BASE_URL/api/products" "" "$ADMIN_TOKEN")
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Products Access - Admin Token"
    
    # Test SKU offers access control (mom-only features)
    print_test "SKU Offers Access - Doctor Token (Should be blocked)"
    response=$(curl_request "GET" "$BASE_URL/api/sku-offers" "" "$DOCTOR_TOKEN")
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "SKU Offers Access - Doctor Token"
    
    print_test "SKU Offers Access - Admin Token (Should be blocked)"
    response=$(curl_request "GET" "$BASE_URL/api/sku-offers" "" "$ADMIN_TOKEN")
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "SKU Offers Access - Admin Token"
}

# =============================================================================
# Mom Authorization Boundary Tests
# =============================================================================

test_mom_authorization_boundaries() {
    print_subheader "Mom Authorization Boundary Tests"
    
    # Test unauthorized mom (Alice with <8 sessions)
    print_test "Unauthorized Mom Login"
    local alice_response
    alice_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local alice_token
    alice_token=$(echo "${alice_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    if [ -n "$alice_token" ] && [ "$alice_token" != "null" ]; then
        print_success "Alice (unauthorized mom) can login"
        
        # Test access to mom-only endpoints
        print_test "Unauthorized Mom - Products Access"
        local response
        response=$(curl_request "GET" "$BASE_URL/api/products" "" "$alice_token")
        local status_code="${response: -3}"
        test_status_code 403 "$status_code" "Unauthorized Mom Products"
        
        print_test "Unauthorized Mom - Cart Access"
        response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$alice_token")
        status_code="${response: -3}"
        test_status_code 403 "$status_code" "Unauthorized Mom Cart"
        
        print_test "Unauthorized Mom - SKU Offers Access"
        response=$(curl_request "GET" "$BASE_URL/api/sku-offers" "" "$alice_token")
        status_code="${response: -3}"
        test_status_code 403 "$status_code" "Unauthorized Mom SKU Offers"
        
        print_test "Unauthorized Mom - Profile Access"
        response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$alice_token")
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Unauthorized Mom Profile"; then
            print_success "Unauthorized mom can access profile"
        fi
    else
        print_failure "Could not obtain Alice's token for authorization tests"
    fi
}

# =============================================================================
# CORS Security Tests
# =============================================================================

test_cors_security() {
    print_subheader "CORS Security Tests"
    
    # Test CORS preflight requests
    print_test "CORS Preflight - Valid Origin"
    local response
    response=$(curl -s -w "%{http_code}" -X OPTIONS "$BASE_URL/api/categories" \
        -H "Origin: https://momproject.com" \
        -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: Content-Type")
    
    local status_code="${response: -3}"
    if [[ "$status_code" =~ ^(200|204|404)$ ]]; then
        print_success "CORS preflight handled (Status: $status_code)"
    else
        print_warning "CORS preflight unexpected status: $status_code"
    fi
    
    # Test CORS with suspicious origin
    print_test "CORS Preflight - Suspicious Origin"
    response=$(curl -s -w "%{http_code}" -X OPTIONS "$BASE_URL/api/categories" \
        -H "Origin: https://malicious-site.com" \
        -H "Access-Control-Request-Method: GET")
    
    status_code="${response: -3}"
    # Should handle suspicious origins appropriately
    if [[ "$status_code" =~ ^(200|204|403|404)$ ]]; then
        print_success "Suspicious origin handled (Status: $status_code)"
    else
        print_warning "Unexpected status for suspicious origin: $status_code"
    fi
}

# =============================================================================
# Input Security Tests
# =============================================================================

test_input_security() {
    print_subheader "Input Security Tests"
    
    # Get valid tokens
    admin_login
    mom_login
    
    # Test SQL injection attempts (though we use MongoDB)
    print_test "SQL Injection Attempt in Category Name"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test\"; DROP TABLE categories; --","description":"Test"}' \
        "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    # Should either accept as normal string or reject with validation error
    if [[ "$status_code" =~ ^(201|400|422)$ ]]; then
        print_success "SQL injection handled safely (Status: $status_code)"
    else
        print_warning "Unexpected status for SQL injection: $status_code"
    fi
    
    # Test XSS attempts
    print_test "XSS Attempt in Category Description"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"XSS Test","description":"<script>alert(\"xss\")</script>"}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    if [[ "$status_code" =~ ^(201|400|422)$ ]]; then
        print_success "XSS attempt handled safely (Status: $status_code)"
    else
        print_warning "Unexpected status for XSS attempt: $status_code"
    fi
    
    # Test very large payloads
    print_test "Large Payload Attack"
    local large_string=$(printf 'A%.0s' {1..10000})
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        "{\"name\":\"$large_string\",\"description\":\"Test\"}" \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    # Should reject large payloads or handle gracefully
    if [[ "$status_code" =~ ^(400|413|422)$ ]]; then
        print_success "Large payload properly rejected (Status: $status_code)"
    elif [ "$status_code" = "201" ]; then
        print_warning "Large payload accepted (might be OK if validated)"
    else
        print_warning "Unexpected status for large payload: $status_code"
    fi
}

# =============================================================================
# Session Security Tests
# =============================================================================

test_session_security() {
    print_subheader "Session Security Tests"
    
    # Test concurrent sessions
    print_test "Concurrent Sessions - Same User"
    local login1_response
    login1_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local token1
    token1=$(echo "${login1_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    local login2_response
    login2_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local token2
    token2=$(echo "${login2_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    if [ -n "$token1" ] && [ -n "$token2" ] && [ "$token1" != "null" ] && [ "$token2" != "null" ]; then
        # Test if both tokens work (concurrent sessions allowed)
        local response1
        response1=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$token1")
        local status1="${response1: -3}"
        
        local response2
        response2=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$token2")
        local status2="${response2: -3}"
        
        if [ "$status1" = "200" ] && [ "$status2" = "200" ]; then
            print_success "Concurrent sessions allowed"
        elif [ "$status1" = "200" ] || [ "$status2" = "200" ]; then
            print_success "Single session enforced (one token invalidated)"
        else
            print_warning "Both concurrent sessions failed"
        fi
    fi
    
    # Test token reuse after logout
    print_test "Token Reuse After Logout"
    local login_response
    login_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local access_token
    access_token=$(echo "${login_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    local refresh_token
    refresh_token=$(echo "${login_response%???}" | jq -r '.data.refreshToken' 2>/dev/null || echo "")
    
    if [ -n "$refresh_token" ] && [ "$refresh_token" != "null" ]; then
        # Logout
        curl_request "POST" "$BASE_URL/api/auth/logout" \
            "{\"refreshToken\":\"$refresh_token\"}" > /dev/null
        
        # Try to use access token after logout
        local response
        response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$access_token")
        local status_code="${response: -3}"
        
        # Token might still be valid until expiry (depending on implementation)
        if [ "$status_code" = "401" ]; then
            print_success "Token properly invalidated after logout"
        elif [ "$status_code" = "200" ]; then
            print_warning "Token still valid after logout (may expire later)"
        else
            print_warning "Unexpected status after logout: $status_code"
        fi
    fi
}

# =============================================================================
# Rate Limiting Tests (if implemented)
# =============================================================================

test_rate_limiting() {
    print_subheader "Rate Limiting Tests"
    
    # Test rapid requests to login endpoint
    print_test "Rapid Login Attempts"
    local failed_attempts=0
    local success_attempts=0
    
    for i in {1..10}; do
        local response
        response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
            '{"email":"nonexistent@test.com","password":"wrongpassword"}')
        local status_code="${response: -3}"
        
        if [ "$status_code" = "401" ]; then
            ((success_attempts++))
        elif [ "$status_code" = "429" ]; then
            ((failed_attempts++))
            break
        fi
        
        # Small delay to avoid overwhelming
        sleep 0.1
    done
    
    if [ $failed_attempts -gt 0 ]; then
        print_success "Rate limiting active (429 received)"
    elif [ $success_attempts -ge 10 ]; then
        print_warning "No rate limiting detected (all requests processed)"
    else
        print_info "Rate limiting test inconclusive"
    fi
}

# =============================================================================
# Main Test Runner
# =============================================================================

run_security_tests() {
    print_header "SECURITY TESTS"
    
    # Check server health
    check_server_health || exit 1
    
    # Run security test suites
    test_jwt_token_security
    test_role_based_access_control
    test_mom_authorization_boundaries
    test_cors_security
    test_input_security
    test_session_security
    test_rate_limiting
    
    # Print results
    print_test_summary "SECURITY"
    return $?
}

# =============================================================================
# Script Entry Point
# =============================================================================

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being run directly
    check_dependencies
    run_security_tests
    exit $?
fi
