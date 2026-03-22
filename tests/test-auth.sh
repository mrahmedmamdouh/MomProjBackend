#!/bin/bash

# =============================================================================
# Authentication Tests - Comprehensive Auth Flow Testing
# =============================================================================
# Tests: Login, Registration, Token refresh, Logout, JWT validation
# Coverage: All user types (Admin, Mom, Doctor), Edge cases, Security
# =============================================================================

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# =============================================================================
# Authentication Core Tests
# =============================================================================

test_admin_authentication() {
    print_subheader "Admin Authentication Tests"
    
    # Test admin login
    print_test "Admin Login"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"admin@momcare.com","password":"admin123"}')
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Admin Login"; then
        ADMIN_TOKEN=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null || echo "")
        test_json_field "$response_body" "success" "true" "Admin Login Success"
        test_json_field "$response_body" "data.userType" "ADMIN" "Admin User Type"
        test_json_not_null "$response_body" "data.token" "Admin Access Token"
        test_json_not_null "$response_body" "data.refreshToken" "Admin Refresh Token"
    fi
    
    # Test invalid admin credentials
    print_test "Invalid Admin Credentials"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"admin@momcare.com","password":"wrongpassword"}')
    
    status_code="${response: -3}"
    response_body="${response%???}"
    if test_status_code 200 "$status_code" "Invalid Admin Credentials Status"; then
        test_json_field "$response_body" "success" "false" "Invalid Admin Credentials Response"
    fi
}

test_mom_authentication() {
    print_subheader "Mom Authentication Tests"
    
    # Test authorized mom login (Beth - 8+ sessions)
    print_test "Authorized Mom Login (Beth)"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"beth@example.com","password":"password123"}')
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Authorized Mom Login"; then
        MOM_TOKEN=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null || echo "")
        MOM_ID=$(echo "$response_body" | jq -r '.data.userId' 2>/dev/null || echo "")
        test_json_field "$response_body" "success" "true" "Mom Login Success"
        test_json_field "$response_body" "data.userType" "MOM" "Mom User Type"
        test_json_not_null "$response_body" "data.token" "Mom Access Token"
        test_json_not_null "$response_body" "data.refreshToken" "Mom Refresh Token"
    fi
    
    # Test unauthorized mom login (Alice - <8 sessions)
    print_test "Unauthorized Mom Login (Alice)"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Unauthorized Mom Login"; then
        test_json_field "$response_body" "success" "true" "Alice Login Success"
        test_json_field "$response_body" "data.userType" "MOM" "Alice User Type"
        
        # Alice should be able to login but not access mom-only endpoints
        print_info "Alice can login but will be denied access to mom-only endpoints"
    fi
    
    # Test invalid mom credentials
    print_test "Invalid Mom Credentials"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"wrongpassword"}')
    
    status_code="${response: -3}"
    response_body="${response%???}"
    if test_status_code 200 "$status_code" "Invalid Mom Credentials Status"; then
        test_json_field "$response_body" "success" "false" "Invalid Mom Credentials Response"
    fi
}

test_doctor_authentication() {
    print_subheader "Doctor Authentication Tests"
    
    # Test doctor login
    print_test "Doctor Login (Dr. Brown)"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"dr.brown@example.com","password":"password123"}')
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Doctor Login"; then
        DOCTOR_TOKEN=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null || echo "")
        DOCTOR_ID=$(echo "$response_body" | jq -r '.data.userId' 2>/dev/null || echo "")
        test_json_field "$response_body" "success" "true" "Doctor Login Success"
        test_json_field "$response_body" "data.userType" "DOCTOR" "Doctor User Type"
        test_json_not_null "$response_body" "data.token" "Doctor Access Token"
        test_json_not_null "$response_body" "data.refreshToken" "Doctor Refresh Token"
    fi
    
    # Test invalid doctor credentials
    print_test "Invalid Doctor Credentials"
    response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"dr.brown@example.com","password":"wrongpassword"}')
    
    status_code="${response: -3}"
    response_body="${response%???}"
    if test_status_code 200 "$status_code" "Invalid Doctor Credentials Status"; then
        test_json_field "$response_body" "success" "false" "Invalid Doctor Credentials Response"
    fi
}

# =============================================================================
# Registration Tests
# =============================================================================

test_mom_registration() {
    print_subheader "Mom Registration Tests"
    
    # Test successful mom registration
    print_test "Mom Registration"
    local timestamp
    timestamp=$(generate_timestamp)
    local mom_email="testmom${timestamp}@test.com"
    
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/mom" "" "" \
        "-F 'data={\"fullName\":\"Test Mom\",\"email\":\"$mom_email\",\"password\":\"testpass123\",\"phone\":\"+201234567890\",\"maritalStatus\":\"SINGLE\",\"numberOfSessions\":10}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Mom Registration"; then
        test_json_field "$response_body" "success" "true" "Mom Registration Success"
        test_json_not_null "$response_body" "data.userId" "User ID Generated"
        test_json_not_null "$response_body" "data.token" "Access Token Generated"
    fi
    
    # Test duplicate email registration
    print_test "Duplicate Mom Email Registration"
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/mom" "" "" \
        "-F 'data={\"fullName\":\"Test Mom 2\",\"email\":\"$mom_email\",\"password\":\"testpass123\",\"phone\":\"+201234567891\",\"maritalStatus\":\"SINGLE\",\"numberOfSessions\":10}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    if test_status_code 200 "$status_code" "Duplicate Mom Email"; then
        test_json_field "$response_body" "success" "false" "Duplicate Email Rejected"
    fi
    
    # Test mom registration without photos
    print_test "Mom Registration Without Photos"
    local timestamp_no_photo
    timestamp_no_photo=$(generate_timestamp)
    local mom_email_no_photo="testmomnophoto${timestamp_no_photo}@test.com"
    
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/mom" "" "" \
        "-F 'data={\"fullName\":\"Test Mom No Photo\",\"email\":\"$mom_email_no_photo\",\"password\":\"testpass123\",\"phone\":\"+201234567894\",\"maritalStatus\":\"SINGLE\"}'")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 400 "$status_code" "Mom Registration Without Photos"; then
        test_json_field "$response_body" "success" "false" "Mom Registration Without Photos Failed"
        print_info "Mom registration without photos correctly failed - photos are required"
    else
        print_error "Mom registration without photos should have failed but didn't"
        print_info "Response: $response_body"
    fi
    
    # Test invalid phone format
    print_test "Invalid Phone Format Registration"
    local invalid_email="testmominvalid${timestamp}@test.com"
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/mom" "" "" \
        "-F 'data={\"fullName\":\"Test Mom Invalid\",\"email\":\"$invalid_email\",\"password\":\"testpass123\",\"phone\":\"invalid-phone\",\"maritalStatus\":\"SINGLE\",\"numberOfSessions\":10}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Invalid Phone Format"
}

test_doctor_registration() {
    print_subheader "Doctor Registration Tests"
    
    # Test successful doctor registration
    print_test "Doctor Registration"
    local timestamp
    timestamp=$(generate_timestamp)
    local doctor_email="testdoctor${timestamp}@test.com"
    
    local response
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/doctor" "" "" \
        "-F 'data={\"name\":\"Dr. Test\",\"email\":\"$doctor_email\",\"password\":\"testpass123\",\"phone\":\"+201234567892\",\"specialization\":\"PSYCHIATRIST\"}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Doctor Registration"; then
        test_json_field "$response_body" "success" "true" "Doctor Registration Success"
        test_json_not_null "$response_body" "data.userId" "User ID Generated"
        test_json_not_null "$response_body" "data.token" "Access Token Generated"
    fi
    
    # Test duplicate email registration
    print_test "Duplicate Doctor Email Registration"
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/doctor" "" "" \
        "-F 'data={\"name\":\"Dr. Test 2\",\"email\":\"$doctor_email\",\"password\":\"testpass123\",\"phone\":\"+201234567893\",\"specialization\":\"General Practice\"}' \
         -F 'photo=@$TEST_FILES_DIR/test-photo.jpg' \
         -F 'nidFront=@$TEST_FILES_DIR/test-nid-front.jpg' \
         -F 'nidBack=@$TEST_FILES_DIR/test-nid-back.jpg'")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Duplicate Doctor Email"
    
    # Test doctor registration without photos
    print_test "Doctor Registration Without Photos"
    local timestamp_no_photo
    timestamp_no_photo=$(generate_timestamp)
    local doctor_email_no_photo="testdoctornophoto${timestamp_no_photo}@test.com"
    
    response=$(curl_request "POST" "$BASE_URL/api/auth/register/doctor" "" "" \
        "-F 'data={\"name\":\"Dr. Test No Photo\",\"email\":\"$doctor_email_no_photo\",\"password\":\"testpass123\",\"phone\":\"+201234567895\",\"specialization\":\"PSYCHIATRIST\"}'")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 400 "$status_code" "Doctor Registration Without Photos"; then
        test_json_field "$response_body" "success" "false" "Doctor Registration Without Photos Failed"
        print_info "Doctor registration without photos correctly failed - photos are required"
    else
        print_error "Doctor registration without photos should have failed but didn't"
        print_info "Response: $response_body"
    fi
}

# =============================================================================
# Token Management Tests
# =============================================================================

test_token_refresh() {
    print_subheader "Token Refresh Tests"
    
    # Ensure we have a valid refresh token from mom login
    if [ -z "$MOM_TOKEN" ]; then
        mom_login
    fi
    
    # Get refresh token from login response
    print_test "Token Refresh"
    local login_response
    login_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local refresh_token
    refresh_token=$(echo "${login_response%???}" | jq -r '.data.refreshToken' 2>/dev/null || echo "")
    
    if [ -n "$refresh_token" ] && [ "$refresh_token" != "null" ]; then
        # Test refresh token
        local refresh_response
        refresh_response=$(curl_request "POST" "$BASE_URL/api/auth/refresh" \
            "{\"refreshToken\":\"$refresh_token\"}")
        
        local status_code="${refresh_response: -3}"
        local response_body="${refresh_response%???}"
        
        if test_status_code 200 "$status_code" "Token Refresh"; then
            test_json_field "$response_body" "success" "true" "Token Refresh Success"
            test_json_not_null "$response_body" "data.token" "New Access Token"
            test_json_not_null "$response_body" "data.refreshToken" "New Refresh Token"
        fi
        
        # Test invalid refresh token
        print_test "Invalid Refresh Token"
        refresh_response=$(curl_request "POST" "$BASE_URL/api/auth/refresh" \
            '{"refreshToken":"invalid-token"}')
        
        status_code="${refresh_response: -3}"
        test_status_code 401 "$status_code" "Invalid Refresh Token"
    else
        print_failure "Could not obtain refresh token for testing"
    fi
}

test_logout() {
    print_subheader "Logout Tests"
    
    # Test logout
    print_test "User Logout"
    local login_response
    login_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local refresh_token
    refresh_token=$(echo "${login_response%???}" | jq -r '.data.refreshToken' 2>/dev/null || echo "")
    
    if [ -n "$refresh_token" ] && [ "$refresh_token" != "null" ]; then
        local logout_response
        logout_response=$(curl_request "POST" "$BASE_URL/api/auth/logout" \
            "{\"refreshToken\":\"$refresh_token\"}")
        
        local status_code="${logout_response: -3}"
        local response_body="${logout_response%???}"
        
        if test_status_code 200 "$status_code" "User Logout"; then
            test_json_field "$response_body" "success" "true" "Logout Success"
            
            # Verify refresh token is invalidated
            print_test "Refresh After Logout (Should Fail)"
            local refresh_after_logout
            refresh_after_logout=$(curl_request "POST" "$BASE_URL/api/auth/refresh" \
                "{\"refreshToken\":\"$refresh_token\"}")
            
            local refresh_status="${refresh_after_logout: -3}"
            test_status_code 401 "$refresh_status" "Refresh After Logout"
        fi
    else
        print_failure "Could not obtain refresh token for logout testing"
    fi
}

# =============================================================================
# JWT Security Tests
# =============================================================================

test_jwt_security() {
    print_subheader "JWT Security Tests"
    
    # Test malformed JWT
    print_test "Malformed JWT Token"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "invalid.jwt.token")
    
    local status_code="${response: -3}"
    test_status_code 401 "$status_code" "Malformed JWT"
    
    # Test expired JWT (simulate with invalid signature)
    print_test "Invalid JWT Signature"
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature")
    
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "Invalid JWT Signature"
    
    # Test no authorization header
    print_test "Missing Authorization Header"
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile")
    
    status_code="${response: -3}"
    test_status_code 401 "$status_code" "Missing Authorization Header"
}

# =============================================================================
# Main Test Runner
# =============================================================================

run_auth_tests() {
    print_header "AUTHENTICATION TESTS"
    
    # Check server health
    check_server_health || exit 1
    
    # Run test suites
    test_admin_authentication
    test_mom_authentication
    test_doctor_authentication
    test_mom_registration
    test_doctor_registration
    test_token_refresh
    test_logout
    test_jwt_security
    
    # Print results
    print_test_summary "AUTHENTICATION"
    return $?
}

# =============================================================================
# Script Entry Point
# =============================================================================

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being run directly
    check_dependencies
    run_auth_tests
    exit $?
fi
