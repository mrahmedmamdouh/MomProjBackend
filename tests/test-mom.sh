#!/bin/bash

# =============================================================================
# Mom-Only API Tests - Authorized Mom Endpoints Testing
# =============================================================================
# Tests: All mom-only endpoints requiring Mom authentication + 8+ sessions
# Coverage: Profile, Sessions, Products, SKU Offers, Shopping Cart
# =============================================================================

# Source common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# =============================================================================
# Mom Profile Management Tests
# =============================================================================

test_mom_profile_management() {
    print_subheader "Mom Profile Management"
    
    # Ensure authorized mom is logged in (Alice with 8+ sessions)
    if [ -z "$MOM_TOKEN" ]; then
        mom_login || return 1
    fi
    
    # Test get mom profile
    print_test "Get Mom Profile"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$MOM_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get Mom Profile"; then
        test_json_field "$response_body" "success" "true" "Profile Success"
        test_json_not_null "$response_body" "data.id" "Mom ID"
        test_json_not_null "$response_body" "data.fullName" "Mom Full Name"
        test_json_not_null "$response_body" "data.email" "Mom Email"
    fi
    
    # Test update mom profile (JSON)
    print_test "Update Mom Profile (JSON)"
    response=$(curl_request "PUT" "$BASE_URL/api/moms/profile" \
        '{"fullName":"Alice Updated","maritalStatus":"MARRIED","numberOfSessions":12}' \
        "$MOM_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Update Mom Profile"; then
        test_json_field "$response_body" "success" "true" "Profile Update Success"
        test_json_field "$response_body" "data.fullName" "Alice Updated" "Updated Name"
    fi
    
    # Test update mom profile (Multipart with photo)
    print_test "Update Mom Profile (Multipart)"
    if [ -f "$SCRIPT_DIR/../test-files/test-photo.jpg" ]; then
        response=$(curl_request "PUT" "$BASE_URL/api/moms/profile" "" "$MOM_TOKEN" \
            "-F 'data={\"fullName\":\"Alice Multipart Updated\",\"maritalStatus\":\"SINGLE\"}'" \
            "-F 'photo=@$SCRIPT_DIR/../test-files/test-photo.jpg'")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Update Mom Profile (Multipart)"; then
            test_json_field "$response_body" "success" "true" "Multipart Profile Update Success"
        fi
    else
        print_warning "Test photo file not found, skipping multipart profile update test"
    fi
    
    # Test unauthorized mom profile access (Alice with <8 sessions should be able to view profile)
    print_test "Unauthorized Mom Profile Access"
    local alice_response
    alice_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local alice_token
    alice_token=$(echo "${alice_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    if [ -n "$alice_token" ] && [ "$alice_token" != "null" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$alice_token")
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Unauthorized Mom Profile Access"; then
            test_json_field "$response_body" "success" "true" "Unauthorized Mom Profile Success"
            print_success "Alice (unauthorized mom) can access her profile"
        fi
    fi
}

# =============================================================================
# Mom Sessions Management Tests
# =============================================================================

test_mom_sessions_management() {
    print_subheader "Mom Sessions Management"
    
    # Ensure authorized mom is logged in
    if [ -z "$MOM_TOKEN" ]; then
        mom_login || return 1
    fi
    
    # Test update mom sessions
    print_test "Update Mom Sessions"
    local response
    response=$(curl_request "PUT" "$BASE_URL/api/moms/sessions" \
        '{"numberOfSessions":10}' \
        "$MOM_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Update Mom Sessions"; then
        test_json_field "$response_body" "success" "true" "Sessions Update Success"
    fi
    
    # Test check authorization status
    print_test "Check Mom Authorization Status"
    response=$(curl_request "GET" "$BASE_URL/api/moms/check-authorization" "" "$MOM_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Check Authorization"; then
        test_json_field "$response_body" "success" "true" "Authorization Check Success"
        test_json_not_null "$response_body" "data.isAuthorized" "Authorization Status"
        test_json_not_null "$response_body" "data.momId" "Mom ID"
    fi
}

# =============================================================================
# Products Browsing Tests (Mom-Authorized)
# =============================================================================

test_mom_products_browsing() {
    print_subheader "Products Browsing (Mom-Authorized)"
    
    # Ensure authorized mom is logged in
    if [ -z "$MOM_TOKEN" ]; then
        mom_login || return 1
    fi
    
    # Test get all products
    print_test "Get All Products (Mom)"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/products?page=0&size=10" "" "$MOM_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get All Products"; then
        test_json_field "$response_body" "success" "true" "Products Success"
        
        # Extract product ID for subsequent tests
        local product_id
        product_id=$(echo "$response_body" | jq -r '.data[0].id' 2>/dev/null || echo "")
        if [ -n "$product_id" ] && [ "$product_id" != "null" ]; then
            print_success "Found test product ID: $product_id"
            
            # Test get product by ID
            print_test "Get Product By ID (Mom)"
            response=$(curl_request "GET" "$BASE_URL/api/products/$product_id" "" "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Get Product By ID"; then
                test_json_field "$response_body" "success" "true" "Product By ID Success"
                test_json_field "$response_body" "data.id" "$product_id" "Product ID Match"
            fi
        fi
    fi
    
    # Test products by category
    print_test "Get Products By Category (Mom)"
    # Get a category first
    local categories_response
    categories_response=$(curl_request "GET" "$BASE_URL/api/categories")
    local category_id
    category_id=$(echo "${categories_response%???}" | jq -r '.data[0].id' 2>/dev/null || echo "")
    
    if [ -n "$category_id" ] && [ "$category_id" != "null" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/products/category/$category_id?page=0&size=5" "" "$MOM_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Get Products By Category"; then
            test_json_field "$response_body" "success" "true" "Products By Category Success"
        fi
    fi
    
    # Test product search
    print_test "Search Products (Mom)"
    response=$(curl_request "GET" "$BASE_URL/api/products/search?query=baby&page=0&size=5" "" "$MOM_TOKEN")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Search Products"; then
        test_json_field "$response_body" "success" "true" "Product Search Success"
    fi
    
    # Test session-based product access (if products have minSessionsToPurchase)
    print_test "Session-Based Product Access Validation"
    response=$(curl_request "GET" "$BASE_URL/api/products?page=0&size=5" "" "$MOM_TOKEN")
    status_code="${response: -3}"
    response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Session-Based Product Access"; then
        # Check if any products have session requirements
        local has_session_requirements
        has_session_requirements=$(echo "$response_body" | jq -r '.data[] | select(.minSessionsToPurchase != null and .minSessionsToPurchase > 0) | .id' 2>/dev/null | head -1)
        
        if [ -n "$has_session_requirements" ] && [ "$has_session_requirements" != "null" ]; then
            print_success "Found products with session requirements - session-based access working"
        else
            print_info "No products with session requirements found - this is normal"
        fi
    fi
}

# =============================================================================
# SKU Offers Browsing Tests (Mom-Authorized)
# =============================================================================

test_mom_sku_offers_browsing() {
    print_subheader "SKU Offers Browsing (Mom-Authorized)"
    
    # Ensure authorized mom is logged in
    if [ -z "$MOM_TOKEN" ]; then
        mom_login || return 1
    fi
    
    # Test get all SKU offers
    print_test "Get All SKU Offers (Mom)"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/sku-offers" "" "$MOM_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get All SKU Offers"; then
        test_json_field "$response_body" "success" "true" "SKU Offers Success"
        
        # Extract offer ID for subsequent tests
        local offer_id
        offer_id=$(echo "$response_body" | jq -r '.data[0].id' 2>/dev/null || echo "")
        if [ -n "$offer_id" ] && [ "$offer_id" != "null" ]; then
            print_success "Found test offer ID: $offer_id"
            
            # Test get SKU offer by ID
            print_test "Get SKU Offer By ID (Mom)"
            response=$(curl_request "GET" "$BASE_URL/api/sku-offers/$offer_id" "" "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Get SKU Offer By ID"; then
                test_json_field "$response_body" "success" "true" "SKU Offer By ID Success"
                test_json_field "$response_body" "data.id" "$offer_id" "Offer ID Match"
            fi
        fi
        
        # Extract SKU ID for SKU-specific tests
        local sku_id
        sku_id=$(echo "$response_body" | jq -r '.data[0].skuId' 2>/dev/null || echo "")
        if [ -n "$sku_id" ] && [ "$sku_id" != "null" ]; then
            # Test get offers by SKU
            print_test "Get SKU Offers By SKU ID (Mom)"
            response=$(curl_request "GET" "$BASE_URL/api/sku-offers/sku/$sku_id" "" "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Get Offers By SKU"; then
                test_json_field "$response_body" "success" "true" "Offers By SKU Success"
            fi
            
            # Test get best offer for SKU
            print_test "Get Best Offer For SKU (Mom)"
            response=$(curl_request "GET" "$BASE_URL/api/sku-offers/sku/$sku_id/best" "" "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Get Best Offer For SKU"; then
                test_json_field "$response_body" "success" "true" "Best Offer Success"
            fi
        fi
        
        # Extract seller ID for seller-specific tests
        local seller_id
        seller_id=$(echo "$response_body" | jq -r '.data[0].sellerId' 2>/dev/null || echo "")
        if [ -n "$seller_id" ] && [ "$seller_id" != "null" ]; then
            # Test get offers by seller
            print_test "Get SKU Offers By Seller ID (Mom)"
            response=$(curl_request "GET" "$BASE_URL/api/sku-offers/seller/$seller_id" "" "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Get Offers By Seller"; then
                test_json_field "$response_body" "success" "true" "Offers By Seller Success"
            fi
        fi
    fi
}

# =============================================================================
# Shopping Cart Operations Tests
# =============================================================================

test_mom_shopping_cart() {
    print_subheader "Shopping Cart Operations (Mom-Authorized)"
    
    # Ensure authorized mom is logged in
    if [ -z "$MOM_TOKEN" ]; then
        mom_login || return 1
    fi
    
    # Test get cart (initially empty or existing)
    print_test "Get Shopping Cart (Mom)"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$MOM_TOKEN")
    
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if test_status_code 200 "$status_code" "Get Shopping Cart"; then
        test_json_field "$response_body" "success" "true" "Cart Success"
    fi
    
    # Get a SKU ID for cart operations
    local sku_offers_response
    sku_offers_response=$(curl_request "GET" "$BASE_URL/api/sku-offers" "" "$MOM_TOKEN")
    local test_sku_id
    test_sku_id=$(echo "${sku_offers_response%???}" | jq -r '.data[0].skuId' 2>/dev/null || echo "")
    
    if [ -n "$test_sku_id" ] && [ "$test_sku_id" != "null" ]; then
        # Test add item to cart
        print_test "Add Item to Cart (Mom)"
        # Get an offer ID for the SKU
        local offer_response
        offer_response=$(curl_request "GET" "$BASE_URL/api/sku-offers/sku/$test_sku_id" "" "$MOM_TOKEN")
        local offer_id
        offer_id=$(echo "${offer_response%???}" | jq -r '.data[0].id' 2>/dev/null || echo "")
        
        if [ -n "$offer_id" ] && [ "$offer_id" != "null" ]; then
            response=$(curl_request "POST" "$BASE_URL/api/cart/add" \
                "{\"skuId\":\"$test_sku_id\",\"qty\":2,\"offerId\":\"$offer_id\"}" \
                "$MOM_TOKEN")
        else
            print_warning "No offer found for SKU $test_sku_id, skipping cart add test"
            return
        fi
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 201 "$status_code" "Add Item to Cart"; then
            test_json_field "$response_body" "success" "true" "Add to Cart Success"
            
            # Test update cart item
            print_test "Update Cart Item (Mom)"
            response=$(curl_request "PUT" "$BASE_URL/api/cart/item/$test_sku_id" \
                '{"qty":3}' \
                "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Update Cart Item"; then
                test_json_field "$response_body" "success" "true" "Update Cart Success"
            fi
            
            # Test remove item from cart
            print_test "Remove Item from Cart (Mom)"
            response=$(curl_request "DELETE" "$BASE_URL/api/cart/item/$test_sku_id" "" "$MOM_TOKEN")
            
            status_code="${response: -3}"
            response_body="${response%???}"
            
            if test_status_code 200 "$status_code" "Remove Item from Cart"; then
                test_json_field "$response_body" "success" "true" "Remove from Cart Success"
            fi
        fi
        
        # Add item back and test clear cart
        print_test "Add Item and Clear Cart (Mom)"
        curl_request "POST" "$BASE_URL/api/cart/add" \
            "{\"skuId\":\"$test_sku_id\",\"qty\":1,\"offerId\":\"$offer_id\"}" \
            "$MOM_TOKEN" > /dev/null
        
        response=$(curl_request "DELETE" "$BASE_URL/api/cart" "" "$MOM_TOKEN")
        
        status_code="${response: -3}"
        response_body="${response%???}"
        
        if test_status_code 200 "$status_code" "Clear Cart"; then
            test_json_field "$response_body" "success" "true" "Clear Cart Success"
        fi
    else
        print_warning "No SKU offers found, skipping cart operation tests"
    fi
}

# =============================================================================
# Authorization Boundary Tests
# =============================================================================

test_mom_authorization_boundaries() {
    print_subheader "Mom Authorization Boundary Tests"
    
    # Test unauthorized mom trying to access mom-only endpoints
    print_test "Unauthorized Mom Access to Products"
    local alice_response
    alice_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local alice_token
    alice_token=$(echo "${alice_response%???}" | jq -r '.data.token' 2>/dev/null || echo "")
    
    if [ -n "$alice_token" ] && [ "$alice_token" != "null" ]; then
        # Test products access (should be forbidden)
        local response
        response=$(curl_request "GET" "$BASE_URL/api/products" "" "$alice_token")
        local status_code="${response: -3}"
        
        if test_status_code 403 "$status_code" "Unauthorized Products Access"; then
            print_success "Alice properly denied access to products"
        fi
        
        # Test cart access (should be forbidden)
        response=$(curl_request "GET" "$BASE_URL/api/cart" "" "$alice_token")
        status_code="${response: -3}"
        
        if test_status_code 403 "$status_code" "Unauthorized Cart Access"; then
            print_success "Alice properly denied access to cart"
        fi
    fi
    
    # Test no token access
    print_test "No Token Access to Mom Endpoints"
    local response
    response=$(curl_request "GET" "$BASE_URL/api/products")
    local status_code="${response: -3}"
    
    if test_status_code 401 "$status_code" "No Token Products Access"; then
        print_success "Proper 401 for missing authentication"
    fi
    
    # Test admin token access to mom endpoints (should fail)
    print_test "Admin Token Access to Mom Endpoints"
    if [ -n "$ADMIN_TOKEN" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$ADMIN_TOKEN")
        status_code="${response: -3}"
        
        if test_status_code 403 "$status_code" "Admin Access to Mom Profile"; then
            print_success "Admin properly denied access to mom profile"
        fi
    fi
    
    # Test doctor token access to mom endpoints (should fail)
    print_test "Doctor Token Access to Mom Endpoints"
    if [ -n "$DOCTOR_TOKEN" ]; then
        response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "$DOCTOR_TOKEN")
        status_code="${response: -3}"
        
        if test_status_code 403 "$status_code" "Doctor Access to Mom Profile"; then
            print_success "Doctor properly denied access to mom profile"
        fi
    fi
    
    # Test invalid token access
    print_test "Invalid Token Access to Mom Endpoints"
    response=$(curl_request "GET" "$BASE_URL/api/moms/profile" "" "invalid_token_12345")
    status_code="${response: -3}"
    
    if test_status_code 401 "$status_code" "Invalid Token Access"; then
        print_success "Proper 401 for invalid token"
    fi
}

# =============================================================================
# Unauthorized Mom Authorization Check Test
# =============================================================================

test_unauthorized_mom_authorization_check() {
    print_subheader "Unauthorized Mom Authorization Check"
    
    # Login as unauthorized mom (Alice with < 8 sessions)
    print_test "Login as Unauthorized Mom (Alice)"
    local alice_response
    alice_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}' "")
    
    local alice_status="${alice_response: -3}"
    local alice_body="${alice_response%???}"
    
    if test_status_code 200 "$alice_status" "Alice Login"; then
        local alice_token
        alice_token=$(echo "$alice_body" | jq -r '.data.token // empty')
        
        if [ -n "$alice_token" ] && [ "$alice_token" != "null" ]; then
            print_test "Check Authorization Status (Unauthorized Mom)"
            local auth_response
            auth_response=$(curl_request "GET" "$BASE_URL/api/moms/check-authorization" "" "$alice_token")
            
            local auth_status="${auth_response: -3}"
            local auth_body="${auth_response%???}"
            
            if test_status_code 200 "$auth_status" "Unauthorized Mom Authorization Check"; then
                test_json_field "$auth_body" "success" "true" "Authorization Check Success"
                test_json_field "$auth_body" "data.isAuthorized" "false" "Should be unauthorized"
                test_json_field "$auth_body" "data.momId" "mom_alice" "Correct Mom ID"
                print_success "✅ Unauthorized mom can successfully check her authorization status"
            else
                print_error "❌ Unauthorized mom cannot check authorization status (Status: $auth_status)"
            fi
        else
            print_error "❌ Failed to get Alice's token"
        fi
    else
        print_error "❌ Failed to login as Alice (Status: $alice_status)"
    fi
}

# =============================================================================
# Main Test Runner
# =============================================================================

run_mom_tests() {
    print_header "MOM-ONLY API TESTS"
    
    # Check server health
    check_server_health || exit 1
    
    # Ensure authorized mom login
    mom_login || exit 1
    
    # Run test suites
    test_mom_profile_management
    test_mom_sessions_management
    test_mom_products_browsing
    test_mom_sku_offers_browsing
    test_mom_shopping_cart
    test_mom_authorization_boundaries
    test_unauthorized_mom_authorization_check
    
    # Print results
    print_test_summary "MOM-ONLY API"
    return $?
}

# =============================================================================
# Script Entry Point
# =============================================================================

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Script is being run directly
    check_dependencies
    run_mom_tests
    exit $?
fi
