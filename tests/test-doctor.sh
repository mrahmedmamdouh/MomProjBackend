#!/bin/bash

# =============================================================================
# Doctor API Tests - Doctor Profile Management
# =============================================================================
# Tests: Doctor-only endpoints requiring Doctor authentication
# Coverage: Profile GET/PUT operations with multipart support
# =============================================================================

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# =============================================================================
# Doctor Profile Management Tests
# =============================================================================

test_doctor_profile_management() {
    print_subheader "Doctor Profile Management"
    
    # Ensure doctor is logged in and authorized
    if [ -z "$DOCTOR_TOKEN" ]; then
        doctor_login || return 1
    fi
    
    # Ensure doctor is authorized
    ensure_doctor_authorized
    
    # Test get doctor profile
    print_test "Get Doctor Profile"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/doctors/profile" "" "$DOCTOR_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get Doctor Profile"; then
        test_json_field "$response_body" "success" "true" "Profile Success"
        test_json_not_null "$response_body" "data.id" "Doctor ID"
        test_json_not_null "$response_body" "data.name" "Doctor Name"
        test_json_not_null "$response_body" "data.email" "Doctor Email"
        test_json_not_null "$response_body" "data.specialization" "Doctor Specialization"
    fi
    
    # Test update doctor profile (JSON)
    print_test "Update Doctor Profile (JSON)"
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        '{"name":"Dr. Brown Updated","specialization":"CLINICAL_PSYCHOLOGIST","phone":"+201234567899"}' \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Update Doctor Profile"; then
        test_json_field "$response_body" "success" "true" "Profile Update Success"
        test_json_field "$response_body" "data.name" "Dr. Brown Updated" "Updated Name"
        test_json_field "$response_body" "data.specialization" "CLINICAL_PSYCHOLOGIST" "Updated Specialization"
    fi
    
    # Test update doctor profile (Multipart) - only if test files exist
    if [ -d "$TEST_FILES_DIR" ] && [ -f "$TEST_FILES_DIR/test-photo.jpg" ]; then
        print_test "Update Doctor Profile (Multipart)"
        response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" "" "$DOCTOR_TOKEN" \
            "-F 'data={\"name\":\"Dr. Brown Multipart\",\"specialization\":\"PSYCHIATRIST\"}' \
             -F 'photo=@$TEST_FILES_DIR/test-photo.jpg'")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Update Doctor Profile Multipart"; then
            test_json_field "$response_body" "success" "true" "Multipart Update Success"
            test_json_field "$response_body" "data.name" "Dr. Brown Multipart" "Multipart Name Update"
        fi
    else
        print_warning "Test files not found, skipping multipart update test"
    fi
}

# =============================================================================
# Doctor Authorization Tests
# =============================================================================

test_doctor_authorization() {
    print_subheader "Doctor Authorization Tests"
    
    # Test doctor endpoints without token
    print_test "Doctor Endpoint Without Token"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/doctors/profile")
    
    local status_code="${response: -3}"
    test_status_code 401 "$status_code" "Doctor Endpoint Without Token"
    
    # Test doctor endpoints with mom token
    if [ -z "$MOM_TOKEN" ]; then
        mom_login
    fi
    
    print_test "Doctor Endpoint With Mom Token"
    response=$(curl_request "GET" "$BASE_URL/api/doctors/profile" "" "$MOM_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Doctor Endpoint With Mom Token"
    
    # Test doctor endpoints with admin token
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login
    fi
    
    print_test "Doctor Endpoint With Admin Token"
    response=$(curl_request "GET" "$BASE_URL/api/doctors/profile" "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 403 "$status_code" "Doctor Endpoint With Admin Token"
}

# =============================================================================
# Doctor Profile Validation Tests
# =============================================================================

test_doctor_profile_validation() {
    print_subheader "Doctor Profile Validation Tests"
    
    # Ensure doctor is logged in
    if [ -z "$DOCTOR_TOKEN" ]; then
        doctor_login || return 1
    fi
    
    # Test invalid specialization
    print_test "Update Doctor Profile - Invalid Specialization"
    local response
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        '{"name":"Dr. Test","specialization":"INVALID_SPECIALTY"}' \
        "$DOCTOR_TOKEN")
    
    local status_code="${response: -3}"
    # Should return 400 for invalid specialization
    if [[ "$status_code" =~ ^(400|422)$ ]]; then
        print_success "Invalid specialization properly rejected (Status: $status_code)"
    else
        print_failure "Expected 400/422 for invalid specialization, got: $status_code"
    fi
    
    # Test empty required fields
    print_test "Update Doctor Profile - Empty Name"
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        '{"name":"","specialization":"PSYCHIATRIST"}' \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Empty Name Validation"
    
    # Test invalid phone format
    print_test "Update Doctor Profile - Invalid Phone"
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        '{"name":"Dr. Test","phone":"invalid-phone","specialization":"PSYCHIATRIST"}' \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Invalid Phone Validation"
}

# =============================================================================
# Doctor Profile Edge Cases
# =============================================================================

test_doctor_profile_edge_cases() {
    print_subheader "Doctor Profile Edge Cases"
    
    # Ensure doctor is logged in
    if [ -z "$DOCTOR_TOKEN" ]; then
        doctor_login || return 1
    fi
    
    # Test malformed JSON
    print_test "Update Doctor Profile - Malformed JSON"
    local response
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        '{"name":"Dr. Test","specialization":}' \
        "$DOCTOR_TOKEN")
    
    local status_code="${response: -3}"
    test_status_code 400 "$status_code" "Malformed JSON"
    
    # Test very long name
    print_test "Update Doctor Profile - Very Long Name"
    local long_name="Dr. $(printf 'A%.0s' {1..300})"
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        "{\"name\":\"$long_name\",\"specialization\":\"PSYCHIATRIST\"}" \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    # Should handle long names gracefully (either accept or reject with 400)
    if [[ "$status_code" =~ ^(200|400)$ ]]; then
        print_success "Long name handled gracefully (Status: $status_code)"
    else
        print_warning "Unexpected status for long name: $status_code"
    fi
    
    # Test special characters in name
    print_test "Update Doctor Profile - Special Characters"
    response=$(curl_request "PUT" "$BASE_URL/api/doctors/profile" \
        '{"name":"Dr. José María","specialization":"PSYCHIATRIST"}' \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    if [[ "$status_code" =~ ^(200|400)$ ]]; then
        print_success "Special characters handled (Status: $status_code)"
    else
        print_warning "Unexpected status for special characters: $status_code"
    fi
}

# =============================================================================
# Unauthorized Doctor Authorization Check Test
# =============================================================================

test_unauthorized_doctor_authorization_check() {
    print_subheader "Unauthorized Doctor Authorization Check"
    
    # First, ensure Dr. Brown is deauthorized for this test
    print_test "Deauthorize Dr. Brown for Test"
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    local deauth_response
    deauth_response=$(curl_request "PUT" "$BASE_URL/api/admin/doctors/doc_brown/authorize" \
        '{"isAuthorized": false}' "$ADMIN_TOKEN")
    local deauth_status="${deauth_response: -3}"
    if test_status_code 200 "$deauth_status" "Doctor Deauthorization"; then
        print_success "Dr. Brown deauthorized for test"
    else
        print_warning "Could not deauthorize Dr. Brown (Status: $deauth_status)"
    fi
    
    # Login as unauthorized doctor (Dr. Brown - not authorized by admin)
    print_test "Login as Unauthorized Doctor (Dr. Brown)"
    local doctor_response
    doctor_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"dr.brown@example.com","password":"password123"}' "")
    
    local doctor_status="${doctor_response: -3}"
    local doctor_body="${doctor_response%???}"
    
    if test_status_code 200 "$doctor_status" "Dr. Brown Login"; then
        local doctor_token
        doctor_token=$(echo "$doctor_body" | jq -r '.data.token // empty')
        
        if [ -n "$doctor_token" ] && [ "$doctor_token" != "null" ]; then
            print_test "Check Authorization Status (Unauthorized Doctor)"
            local auth_response
            auth_response=$(curl_request "GET" "$BASE_URL/api/doctors/check-authorization" "" "$doctor_token")
            
            local auth_status="${auth_response: -3}"
            local auth_body="${auth_response%???}"
            
            if test_status_code 200 "$auth_status" "Unauthorized Doctor Authorization Check"; then
                test_json_field "$auth_body" "success" "true" "Authorization Check Success"
                test_json_field "$auth_body" "data.isAuthorized" "false" "Should be unauthorized"
                test_json_field "$auth_body" "data.doctorId" "doc_brown" "Correct Doctor ID"
                print_success "✅ Unauthorized doctor can successfully check his authorization status"
            else
                print_error "❌ Unauthorized doctor cannot check authorization status (Status: $auth_status)"
            fi
        else
            print_error "❌ Failed to get Dr. Brown's token"
        fi
    else
        print_error "❌ Failed to login as Dr. Brown (Status: $doctor_status)"
    fi
}

# =============================================================================
# Main Test Runner
# =============================================================================

run_doctor_tests() {
    print_header "DOCTOR API TESTS"
    
    # Check server health
    check_server_health || exit 1
    
    # Ensure doctor login
    doctor_login || exit 1
    
    # Run test suites
    test_doctor_profile_management
    test_doctor_authorization
    test_doctor_profile_validation
    test_doctor_profile_edge_cases
    test_unauthorized_doctor_authorization_check
    
    # Print results
    print_test_summary "DOCTOR API"
    return $?
}

# =============================================================================
# Script Entry Point
# =============================================================================

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being run directly
    check_dependencies
    run_doctor_tests
    exit $?
fi
