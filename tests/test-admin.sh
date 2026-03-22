#!/bin/bash

# =============================================================================
# Admin API Tests - Complete Admin CRUD Operations
# =============================================================================
# Tests: All admin endpoints requiring ADMIN authentication
# Coverage: Categories, Products, SKU Offers, Doctor Management
# =============================================================================

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# =============================================================================
# Admin Category CRUD Tests
# =============================================================================

test_admin_category_crud() {
    print_subheader "Admin Category CRUD Operations"
    
    # Ensure admin is logged in
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Test create category
    print_test "Create Category (Admin)"
    local timestamp
    timestamp=$(generate_timestamp)
    local category_name="Test Category ${timestamp}"
    
    local category_slug="test-category-${timestamp}"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        "{\"name\":\"$category_name\",\"slug\":\"$category_slug\"}" \
        "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "$status_code" =~ ^(200|201)$ ]]; then
        print_success "Create Category successful (Status: $status_code)"
        test_json_field "$response_body" "success" "true" "Category Creation Success"
        # Note: Admin endpoints return success confirmation without data object
        print_info "Category created successfully (API returns success confirmation only)"
    fi
    
    # Test update category (if we have a category ID)
    if [ -n "$TEST_CATEGORY_ID" ] && [ "$TEST_CATEGORY_ID" != "null" ]; then
        print_test "Update Category (Admin)"
        local updated_name="Updated ${category_name}"
        
        response=$(curl_request "PUT" "$BASE_URL/api/admin/categories/$TEST_CATEGORY_ID" \
            "{\"name\":\"$updated_name\",\"description\":\"Updated description\"}" \
            "$ADMIN_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Update Category"; then
            test_json_field "$response_body" "success" "true" "Category Update Success"
            test_json_field "$response_body" "data.name" "$updated_name" "Updated Name Match"
        fi
        
        # Test delete category
        print_test "Delete Category (Admin)"
        response=$(curl_request "DELETE" "$BASE_URL/api/admin/categories/$TEST_CATEGORY_ID" \
            "" "$ADMIN_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Delete Category"; then
            test_json_field "$response_body" "success" "true" "Category Deletion Success"
            
            # Verify category is deleted
            print_test "Verify Category Deleted"
            response=$(curl_request "GET" "$BASE_URL/api/categories/$TEST_CATEGORY_ID")
            
            status_code="${response: -3}"
            test_status_code 404 "$status_code" "Category Deleted Verification"
        fi
    fi
    
    # Test validation errors
    print_test "Create Category - Empty Name (Validation Error)"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"","description":"Test"}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Empty Category Name Validation"
    
    # Test duplicate category name
    print_test "Create Category - Duplicate Name"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Electronics","slug":"electronics"}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    # Should be 400 or 409 for duplicate
    if [[ "$status_code" =~ ^(400|409)$ ]]; then
        print_success "Duplicate category name properly rejected (Status: $status_code)"
    else
        print_failure "Expected 400/409 for duplicate category, got: $status_code"
    fi
}

# =============================================================================
# Admin Product CRUD Tests
# =============================================================================

test_admin_product_crud() {
    print_subheader "Admin Product CRUD Operations"
    
    # Ensure admin is logged in
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Get a category ID for product creation
    local category_response
    category_response=$(curl_request "GET" "$BASE_URL/api/categories")
    local category_id
    category_id=$(echo "${category_response%???}" | jq -r '.data[0].id' 2>/dev/null || echo "")
    
    if [ -z "$category_id" ] || [ "$category_id" = "null" ]; then
        print_warning "No categories found, skipping product tests"
        return 0
    fi
    
    # Test create product
    print_test "Create Product (Admin)"
    local timestamp
    timestamp=$(generate_timestamp)
    local product_name="Test Product ${timestamp}"
    
    local product_slug="test-product-${timestamp}"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/admin/products" \
        "{\"name\":\"$product_name\",\"slug\":\"$product_slug\",\"description\":\"Test product description\",\"defaultSellerId\":\"seller_main\",\"categoryIds\":[\"$category_id\"],\"minSessionsToPurchase\":0}" \
        "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "$status_code" =~ ^(200|201)$ ]]; then
        print_success "Create Product successful (Status: $status_code)"
        test_json_field "$response_body" "success" "true" "Product Creation Success"
        # Note: Admin endpoints return success confirmation without data object
        print_info "Product created successfully (API returns success confirmation only)"
    fi
    
    # Test update product (if we have a product ID)
    if [ -n "$TEST_PRODUCT_ID" ] && [ "$TEST_PRODUCT_ID" != "null" ]; then
        print_test "Update Product (Admin)"
        local updated_name="Updated ${product_name}"
        
        response=$(curl_request "PUT" "$BASE_URL/api/admin/products/$TEST_PRODUCT_ID" \
            "{\"name\":\"$updated_name\",\"description\":\"Updated description\",\"categoryId\":\"$category_id\",\"basePrice\":149.99}" \
            "$ADMIN_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Update Product"; then
            test_json_field "$response_body" "success" "true" "Product Update Success"
            test_json_field "$response_body" "data.name" "$updated_name" "Updated Product Name"
        fi
        
        # Test delete product
        print_test "Delete Product (Admin)"
        response=$(curl_request "DELETE" "$BASE_URL/api/admin/products/$TEST_PRODUCT_ID" \
            "" "$ADMIN_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Delete Product"; then
            test_json_field "$response_body" "success" "true" "Product Deletion Success"
        fi
    fi
    
    # Test validation errors
    print_test "Create Product - Invalid Seller ID (Validation Error)"
    response=$(curl_request "POST" "$BASE_URL/api/admin/products" \
        "{\"name\":\"Test Product\",\"slug\":\"test-invalid\",\"description\":\"Test\",\"defaultSellerId\":\"invalid-seller\",\"categoryIds\":[\"$category_id\"]}" \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Invalid Seller ID Validation"
    
    # Test invalid category ID
    print_test "Create Product - Invalid Category ID"
    response=$(curl_request "POST" "$BASE_URL/api/admin/products" \
        '{"name":"Test Product","slug":"test-invalid-cat","description":"Test","defaultSellerId":"seller_main","categoryIds":["invalid-category-id"]}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Invalid Category ID Validation"
}

# =============================================================================
# Admin SKU Offer CRUD Tests
# =============================================================================

test_admin_sku_offer_crud() {
    print_subheader "Admin SKU Offer CRUD Operations"
    
    # Ensure admin is logged in
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Get a product for SKU offer creation
    local products_response
    products_response=$(curl_request "GET" "$BASE_URL/api/categories")
    
    # For simplicity, we'll create a test offer with a known structure
    print_test "Create SKU Offer (Admin)"
    local timestamp
    timestamp=$(generate_timestamp)
    local offer_name="Test Offer ${timestamp}"
    
    # We need a valid SKU ID from the product we created earlier
    local sku_id="sku_baby_formula_powder_400g"  # Using a known SKU from seeded data
    local response
    response=$(curl_request "POST" "$BASE_URL/api/admin/sku-offers" \
        "{\"skuId\":\"$sku_id\",\"sellerId\":\"seller_main\",\"listPrice\":99.99,\"salePrice\":84.99,\"currency\":\"USD\"}" \
        "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "$status_code" =~ ^(200|201)$ ]]; then
        print_success "Create SKU Offer successful (Status: $status_code)"
        test_json_field "$response_body" "success" "true" "SKU Offer Creation Success"
        # Note: Admin endpoints return success confirmation without data object
        print_info "SKU Offer created successfully (API returns success confirmation only)"
    fi
    
    # Test update SKU offer (if we have an offer ID)
    if [ -n "$TEST_SKU_OFFER_ID" ] && [ "$TEST_SKU_OFFER_ID" != "null" ]; then
        print_test "Update SKU Offer (Admin)"
        local updated_name="Updated ${offer_name}"
        
        response=$(curl_request "PUT" "$BASE_URL/api/admin/sku-offers/$TEST_SKU_OFFER_ID" \
            "{\"name\":\"$updated_name\",\"description\":\"Updated description\",\"discountPercentage\":20.0,\"validFrom\":\"2024-01-01T00:00:00Z\",\"validUntil\":\"2024-12-31T23:59:59Z\",\"isActive\":true}" \
            "$ADMIN_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Update SKU Offer"; then
            test_json_field "$response_body" "success" "true" "SKU Offer Update Success"
            test_json_field "$response_body" "data.name" "$updated_name" "Updated SKU Offer Name"
        fi
        
        # Test delete SKU offer
        print_test "Delete SKU Offer (Admin)"
        response=$(curl_request "DELETE" "$BASE_URL/api/admin/sku-offers/$TEST_SKU_OFFER_ID" \
            "" "$ADMIN_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Delete SKU Offer"; then
            test_json_field "$response_body" "success" "true" "SKU Offer Deletion Success"
        fi
    fi
    
    # Test validation errors
    print_test "Create SKU Offer - Invalid SKU ID (Validation Error)"
    response=$(curl_request "POST" "$BASE_URL/api/admin/sku-offers" \
        '{"skuId":"invalid-sku-id","sellerId":"seller_main","listPrice":99.99,"salePrice":84.99}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 400 "$status_code" "Invalid SKU ID Validation"
}

# =============================================================================
# Admin Doctor Management Tests
# =============================================================================

test_admin_doctor_management() {
    print_subheader "Admin Doctor Management Operations"
    
    # Ensure admin is logged in
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Get a doctor ID for testing
    # First, let's try to get doctor info from login if available
    if [ -n "$DOCTOR_ID" ]; then
        local test_doctor_id="$DOCTOR_ID"
    else
        # Try to find a doctor from seeded data
        local test_doctor_id="doc_brown"
    fi
    
    # Test get doctor status
    print_test "Get Doctor Status (Admin)"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/admin/doctors/$test_doctor_id/status" \
        "" "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get Doctor Status"; then
        test_json_field "$response_body" "success" "true" "Doctor Status Success"
        test_json_not_null "$response_body" "data.doctorId" "Doctor ID Present"
        test_json_not_null "$response_body" "data.isAuthorized" "Authorization Status Present"
    fi
    
    # Test authorize doctor
    print_test "Authorize Doctor (Admin)"
    response=$(curl_request "PUT" "$BASE_URL/api/admin/doctors/$test_doctor_id/authorize" \
        '{"isAuthorized":true}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Authorize Doctor"; then
        test_json_field "$response_body" "success" "true" "Doctor Authorization Success"
        
        # Verify authorization status changed
        print_test "Verify Doctor Authorization Status"
        local verify_response
        verify_response=$(curl_request "GET" "$BASE_URL/api/admin/doctors/$test_doctor_id/status" \
            "" "$ADMIN_TOKEN")
        
        local verify_status="${verify_response: -3}"
        local verify_body="${verify_response%???}"
        
        if test_status_code 200 "$verify_status" "Verify Authorization"; then
            test_json_field "$verify_body" "data.isAuthorized" "true" "Doctor Authorized Status"
        fi
    fi
    
    # Test unauthorize doctor
    print_test "Unauthorize Doctor (Admin)"
    response=$(curl_request "PUT" "$BASE_URL/api/admin/doctors/$test_doctor_id/authorize" \
        '{"isAuthorized":false}' \
        "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Unauthorize Doctor"; then
        test_json_field "$response_body" "success" "true" "Doctor Unauthorization Success"
    fi
    
    # Test invalid doctor ID
    print_test "Get Status - Invalid Doctor ID"
    response=$(curl_request "GET" "$BASE_URL/api/admin/doctors/invalid-doctor-id/status" \
        "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    test_status_code 404 "$status_code" "Invalid Doctor ID"
}

# =============================================================================
# Admin Authorization Tests
# =============================================================================

test_admin_authorization() {
    print_subheader "Admin Authorization Security Tests"
    
    # Test admin endpoints without token
    print_test "Admin Endpoint Without Token"
    local response
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test","description":"Test"}')
    
    local status_code="${response: -3}"
    test_status_code 401 "$status_code" "Admin Endpoint Without Token"
    
    # Test admin endpoints with mom token
    if [ -z "$MOM_TOKEN" ]; then
        mom_login
    fi
    
    print_test "Admin Endpoint With Mom Token"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test","slug":"test-slug"}' \
        "$MOM_TOKEN")
    
    status_code="${response: -3}"
    if [[ "$status_code" =~ ^(401|403)$ ]]; then
        print_success "Admin endpoint properly denied mom access (Status: $status_code)"
    else
        print_failure "Expected 401/403 for mom token on admin endpoint, got: $status_code"
    fi
    
    # Test admin endpoints with doctor token
    if [ -z "$DOCTOR_TOKEN" ]; then
        doctor_login
    fi
    
    print_test "Admin Endpoint With Doctor Token"
    response=$(curl_request "POST" "$BASE_URL/api/admin/categories" \
        '{"name":"Test","slug":"test-slug"}' \
        "$DOCTOR_TOKEN")
    
    status_code="${response: -3}"
    if [[ "$status_code" =~ ^(401|403)$ ]]; then
        print_success "Admin endpoint properly denied doctor access (Status: $status_code)"
    else
        print_failure "Expected 401/403 for doctor token on admin endpoint, got: $status_code"
    fi
}

# =============================================================================
# Admin Mom Session Monitoring Tests
# =============================================================================

test_admin_mom_session_monitoring() {
    print_subheader "Admin Mom Session Monitoring"
    
    # Ensure admin is logged in
    if [ -z "$ADMIN_TOKEN" ]; then
        admin_login || return 1
    fi
    
    # Test get mom session data with valid mom ID
    print_test "Get Mom Session Data (Admin)"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/admin/moms/mom_alice/sessions" "" "$ADMIN_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get Mom Session Data"; then
        test_json_field "$response_body" "success" "true" "Mom Session Data Success"
        test_json_not_null "$response_body" "data.momId" "Mom ID"
        test_json_not_null "$response_body" "data.sessionCount" "Session Count"
        test_json_not_null "$response_body" "data.lastActive" "Last Active"
        test_json_not_null "$response_body" "data.isAuthorized" "Authorization Status"
        print_success "✅ Admin can successfully retrieve mom session data"
    fi
    
    # Test get mom session data with invalid mom ID
    print_test "Get Mom Session Data with Invalid ID (Admin)"
    response=$(curl_request "GET" "$BASE_URL/api/admin/moms/invalid-id/sessions" "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 404 "$status_code" "Get Mom Session Data with Invalid ID"; then
        test_json_field "$response_body" "success" "false" "Invalid ID Response"
        test_json_field "$response_body" "message" "Mom not found" "Correct Error Message"
        print_success "✅ Admin properly handles invalid mom ID"
    fi
    
    # Test get mom session data with non-existent mom ID
    print_test "Get Mom Session Data with Non-existent ID (Admin)"
    response=$(curl_request "GET" "$BASE_URL/api/admin/moms/non_existent_mom/sessions" "" "$ADMIN_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 404 "$status_code" "Get Mom Session Data with Non-existent ID"; then
        test_json_field "$response_body" "success" "false" "Not Found Response"
        test_json_field "$response_body" "message" "Mom not found" "Correct Error Message"
        print_success "✅ Admin properly handles non-existent mom ID"
    fi
    
    # Test unauthorized access (mom token)
    print_test "Unauthorized Access to Mom Session Data (Mom Token)"
    if [ -n "$MOM_TOKEN" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/admin/moms/mom_alice/sessions" "" "$MOM_TOKEN")
        status_code="${response: -3}"
        
        if test_status_code 403 "$status_code" "Unauthorized Mom Access"; then
            print_success "✅ Mom properly denied access to admin mom session endpoint"
        fi
    fi
    
    # Test unauthorized access (doctor token)
    print_test "Unauthorized Access to Mom Session Data (Doctor Token)"
    if [ -n "$DOCTOR_TOKEN" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/admin/moms/mom_alice/sessions" "" "$DOCTOR_TOKEN")
        status_code="${response: -3}"
        
        if test_status_code 403 "$status_code" "Unauthorized Doctor Access"; then
            print_success "✅ Doctor properly denied access to admin mom session endpoint"
        fi
    fi
    
    # Test no token access
    print_test "No Token Access to Mom Session Data"
    response=$(curl_request "GET" "$BASE_URL/api/admin/moms/mom_alice/sessions")
    status_code="${response: -3}"
    
    if test_status_code 401 "$status_code" "No Token Access"; then
        print_success "✅ Proper 401 for missing authentication"
    fi
}

# =============================================================================
# Main Test Runner
# =============================================================================

run_admin_tests() {
    print_header "ADMIN API TESTS"
    
    # Check server health
    check_server_health || exit 1
    
    # Ensure admin login
    admin_login || exit 1
    
    # Run test suites
    test_admin_category_crud
    test_admin_product_crud
    test_admin_sku_offer_crud
    test_admin_doctor_management
    test_admin_mom_session_monitoring
    test_admin_authorization
    
    # Print results
    print_test_summary "ADMIN API"
    return $?
}

# =============================================================================
# Script Entry Point
# =============================================================================

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being run directly
    check_dependencies
    run_admin_tests
    exit $?
fi
