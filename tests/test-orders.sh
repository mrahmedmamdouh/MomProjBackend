#!/bin/bash

# Order Management API Integration Tests
# Tests all order endpoints with proper authentication and authorization

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test-common.sh"

# Test configuration
BASE_URL="http://localhost:8080"
MOM_TOKEN=""
ADMIN_TOKEN=""

# Test data
TEST_ORDER_ID=""
TEST_SKU_ID="sku_prenatal_batchA"
TEST_OFFER_ID="offer_prenatal_batchA_001"

# =============================================================================
# Authentication Setup
# =============================================================================

setup_authentication() {
    print_subheader "Setting up authentication for order tests"
    
    # Get authorized mom token (Beth - 8+ sessions)
    print_test "Getting authorized mom token (Beth)"
    local mom_response
    mom_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"beth@example.com","password":"password123"}')
    
    local mom_status="${mom_response: -3}"
    local mom_body="${mom_response%???}"
    
    if test_status_code 200 "$mom_status" "Mom Login"; then
        MOM_TOKEN=$(echo "$mom_body" | jq -r '.data.token' 2>/dev/null || echo "")
        if [ -n "$MOM_TOKEN" ] && [ "$MOM_TOKEN" != "null" ]; then
            print_success "✅ Mom token obtained"
        else
            print_error "❌ Failed to extract mom token"
            return 1
        fi
    else
        print_error "❌ Mom login failed (Status: $mom_status)"
        return 1
    fi
    
    # Get admin token
    print_test "Getting admin token"
    local admin_response
    admin_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"admin@momcare.com","password":"admin123"}')
    
    local admin_status="${admin_response: -3}"
    local admin_body="${admin_response%???}"
    
    if test_status_code 200 "$admin_status" "Admin Login"; then
        ADMIN_TOKEN=$(echo "$admin_body" | jq -r '.data.token' 2>/dev/null || echo "")
        if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "null" ]; then
            print_success "✅ Admin token obtained"
        else
            print_error "❌ Failed to extract admin token"
            return 1
        fi
    else
        print_error "❌ Admin login failed (Status: $admin_status)"
        return 1
    fi
}

# =============================================================================
# Order Creation Tests
# =============================================================================

test_create_order_from_request() {
    print_subheader "Order Creation Tests"
    
    print_test "Create Order from Request (with offerId)"
    local order_response
    order_response=$(curl_request "POST" "$BASE_URL/api/orders" \
        '{"items":[{"skuId":"'$TEST_SKU_ID'","qty":2,"offerId":"'$TEST_OFFER_ID'"}]}' "$MOM_TOKEN")
    
    local status_code="${order_response: -3}"
    local response_body="${order_response%???}"
    
    if test_status_code 201 "$status_code" "Create Order from Request"; then
        test_json_field "$response_body" "success" "true" "Order Creation Success"
        test_json_not_null "$response_body" "data.id" "Order ID Generated"
        test_json_not_null "$response_body" "data.orderNo" "Order Number Generated"
        test_json_field "$response_body" "data.momId" "mom_beth" "Correct Mom ID"
        test_json_field "$response_body" "data.status" "PENDING" "Initial Status"
        test_json_not_null "$response_body" "data.items" "Order Items Present"
        
        # Store order ID for later tests
        TEST_ORDER_ID=$(echo "$response_body" | jq -r '.data.id' 2>/dev/null || echo "")
        if [ -n "$TEST_ORDER_ID" ] && [ "$TEST_ORDER_ID" != "null" ]; then
            print_success "✅ Order created with ID: $TEST_ORDER_ID"
        else
            print_error "❌ Failed to extract order ID"
        fi
    else
        print_error "❌ Order creation failed (Status: $status_code)"
        print_error "Response: $response_body"
    fi
    
    print_test "Create Order from Request (without offerId - use best offer)"
    local order_response2
    order_response2=$(curl_request "POST" "$BASE_URL/api/orders" \
        '{"items":[{"skuId":"'$TEST_SKU_ID'","qty":1}]}' "$MOM_TOKEN")
    
    local status_code2="${order_response2: -3}"
    local response_body2="${order_response2%???}"
    
    if test_status_code 201 "$status_code2" "Create Order from Request (best offer)"; then
        test_json_field "$response_body2" "success" "true" "Order Creation Success (best offer)"
        test_json_not_null "$response_body2" "data.id" "Order ID Generated (best offer)"
        test_json_field "$response_body2" "data.momId" "mom_beth" "Correct Mom ID (best offer)"
        print_success "✅ Order created using best offer successfully"
    else
        print_error "❌ Order creation with best offer failed (Status: $status_code2)"
        print_error "Response: $response_body2"
    fi
}

test_create_order_from_cart() {
    print_test "Add Items to Cart First"
    local cart_response
    cart_response=$(curl_request "POST" "$BASE_URL/api/cart/add" \
        '{"skuId":"'$TEST_SKU_ID'","qty":1}' "$MOM_TOKEN")
    
    local cart_status="${cart_response: -3}"
    if test_status_code 201 "$cart_status" "Add to Cart"; then
        print_success "✅ Item added to cart"
    else
        print_error "❌ Failed to add item to cart"
        return 1
    fi
    
    print_test "Create Order from Cart"
    local order_response
    order_response=$(curl_request "POST" "$BASE_URL/api/orders/from-cart" \
        "" "$MOM_TOKEN")
    
    local status_code="${order_response: -3}"
    local response_body="${order_response%???}"
    
    if test_status_code 201 "$status_code" "Create Order from Cart"; then
        test_json_field "$response_body" "success" "true" "Order Creation Success"
        test_json_not_null "$response_body" "data.id" "Order ID Generated"
        test_json_field "$response_body" "data.momId" "mom_beth" "Correct Mom ID"
        test_json_field "$response_body" "data.status" "PENDING" "Initial Status"
        test_json_not_null "$response_body" "data.items" "Order Items Present"
        print_success "✅ Order created from cart successfully"
    else
        print_error "❌ Order creation from cart failed (Status: $status_code)"
        print_error "Response: $response_body"
    fi
}

test_create_order_validation() {
    print_test "Create Order with Empty Items (Should Fail)"
    local order_response
    order_response=$(curl_request "POST" "$BASE_URL/api/orders" \
        '{"items":[]}' "$MOM_TOKEN")
    
    local status_code="${order_response: -3}"
    local response_body="${order_response%???}"
    
    if test_status_code 400 "$status_code" "Create Order with Empty Items"; then
        test_json_field "$response_body" "success" "false" "Order Creation Failed"
        print_success "✅ Empty items validation working"
    else
        print_error "❌ Empty items validation failed (Status: $status_code)"
    fi
    
    print_test "Create Order with Invalid SKU (Should Fail)"
    local order_response2
    order_response2=$(curl_request "POST" "$BASE_URL/api/orders" \
        '{"items":[{"skuId":"invalid_sku","qty":1}]}' "$MOM_TOKEN")
    
    local status_code2="${order_response2: -3}"
    local response_body2="${order_response2%???}"
    
    if test_status_code 200 "$status_code2" "Create Order with Invalid SKU"; then
        test_json_field "$response_body2" "success" "false" "Order Creation Failed"
        print_success "✅ Invalid SKU validation working"
    else
        print_error "❌ Invalid SKU validation failed (Status: $status_code2)"
    fi
    
    print_test "Create Order with Invalid offerId (Should Fail)"
    local order_response3
    order_response3=$(curl_request "POST" "$BASE_URL/api/orders" \
        '{"items":[{"skuId":"'$TEST_SKU_ID'","qty":1,"offerId":"invalid_offer_id"}]}' "$MOM_TOKEN")
    
    local status_code3="${order_response3: -3}"
    local response_body3="${order_response3%???}"
    
    if test_status_code 200 "$status_code3" "Create Order with Invalid offerId"; then
        test_json_field "$response_body3" "success" "false" "Order Creation Failed"
        print_success "✅ Invalid offerId validation working"
    else
        print_error "❌ Invalid offerId validation failed (Status: $status_code3)"
    fi
    
    print_test "Create Order with Mismatched offerId (Security Test - Should Fail)"
    local order_response4
    order_response4=$(curl_request "POST" "$BASE_URL/api/orders" \
        '{"items":[{"skuId":"'$TEST_SKU_ID'","qty":1,"offerId":"offer_ess_acme"}]}' "$MOM_TOKEN")
    
    local status_code4="${order_response4: -3}"
    local response_body4="${order_response4%???}"
    
    if test_status_code 400 "$status_code4" "Create Order with Mismatched offerId"; then
        test_json_field "$response_body4" "success" "false" "Order Creation Failed"
        print_success "✅ Mismatched offerId security validation working"
    else
        print_error "❌ Mismatched offerId security validation failed (Status: $status_code4)"
    fi
}

# =============================================================================
# Order Retrieval Tests
# =============================================================================

test_get_orders() {
    print_subheader "Order Retrieval Tests"
    
    print_test "Get Orders List"
    local orders_response
    orders_response=$(curl_request "GET" "$BASE_URL/api/orders" "" "$MOM_TOKEN")
    
    local status_code="${orders_response: -3}"
    local response_body="${orders_response%???}"
    
    if test_status_code 200 "$status_code" "Get Orders List"; then
        test_json_field "$response_body" "success" "true" "Orders Retrieved Successfully"
        test_json_not_null "$response_body" "data" "Orders Data Present"
        print_success "✅ Orders list retrieved successfully"
    else
        print_error "❌ Orders list retrieval failed (Status: $status_code)"
        print_error "Response: $response_body"
    fi
    
    print_test "Get Orders with Pagination"
    local orders_paged_response
    orders_paged_response=$(curl_request "GET" "$BASE_URL/api/orders?page=0&size=5" "" "$MOM_TOKEN")
    
    local paged_status="${orders_paged_response: -3}"
    local paged_body="${orders_paged_response%???}"
    
    if test_status_code 200 "$paged_status" "Get Orders with Pagination"; then
        test_json_field "$paged_body" "success" "true" "Paginated Orders Retrieved"
        print_success "✅ Paginated orders retrieved successfully"
    else
        print_error "❌ Paginated orders retrieval failed (Status: $paged_status)"
    fi
}

test_get_order_by_id() {
    if [ -z "$TEST_ORDER_ID" ] || [ "$TEST_ORDER_ID" = "null" ]; then
        print_error "❌ No test order ID available for get order by ID test"
        return 1
    fi
    
    print_test "Get Order by ID"
    local order_response
    order_response=$(curl_request "GET" "$BASE_URL/api/orders/$TEST_ORDER_ID" "" "$MOM_TOKEN")
    
    local status_code="${order_response: -3}"
    local response_body="${order_response%???}"
    
    if test_status_code 200 "$status_code" "Get Order by ID"; then
        test_json_field "$response_body" "success" "true" "Order Retrieved Successfully"
        test_json_field "$response_body" "data.id" "$TEST_ORDER_ID" "Correct Order ID"
        test_json_field "$response_body" "data.momId" "mom_beth" "Correct Mom ID"
        test_json_not_null "$response_body" "data.items" "Order Items Present"
        print_success "✅ Order retrieved by ID successfully"
    else
        print_error "❌ Order retrieval by ID failed (Status: $status_code)"
        print_error "Response: $response_body"
    fi
    
    print_test "Get Non-existent Order (Should Fail)"
    local order_response2
    order_response2=$(curl_request "GET" "$BASE_URL/api/orders/nonexistent_order_id" "" "$MOM_TOKEN")
    
    local status_code2="${order_response2: -3}"
    local response_body2="${order_response2%???}"
    
    if test_status_code 200 "$status_code2" "Get Non-existent Order"; then
        test_json_field "$response_body2" "success" "false" "Order Not Found"
        print_success "✅ Non-existent order handling working"
    else
        print_error "❌ Non-existent order handling failed (Status: $status_code2)"
    fi
}

# =============================================================================
# Order Status Update Tests
# =============================================================================

test_update_order_status() {
    if [ -z "$TEST_ORDER_ID" ] || [ "$TEST_ORDER_ID" = "null" ]; then
        print_error "❌ No test order ID available for status update test"
        return 1
    fi
    
    print_subheader "Order Status Update Tests"
    
    print_test "Update Order Status to CONFIRMED"
    local status_response
    status_response=$(curl_request "PUT" "$BASE_URL/api/orders/$TEST_ORDER_ID/status" \
        '{"status":"CONFIRMED"}' "$MOM_TOKEN")
    
    local status_code="${status_response: -3}"
    local response_body="${status_response%???}"
    
    if test_status_code 200 "$status_code" "Update Order Status"; then
        test_json_field "$response_body" "success" "true" "Status Updated Successfully"
        print_success "✅ Order status updated to CONFIRMED"
    else
        print_error "❌ Order status update failed (Status: $status_code)"
        print_error "Response: $response_body"
    fi
    
    print_test "Update Order Status to SHIPPED"
    local status_response2
    status_response2=$(curl_request "PUT" "$BASE_URL/api/orders/$TEST_ORDER_ID/status" \
        '{"status":"SHIPPED"}' "$MOM_TOKEN")
    
    local status_code2="${status_response2: -3}"
    local response_body2="${status_response2%???}"
    
    if test_status_code 200 "$status_code2" "Update Order Status to SHIPPED"; then
        test_json_field "$response_body2" "success" "true" "Status Updated Successfully"
        print_success "✅ Order status updated to SHIPPED"
    else
        print_error "❌ Order status update to SHIPPED failed (Status: $status_code2)"
    fi
    
    print_test "Update Order Status with Invalid Status (Should Fail)"
    local status_response3
    status_response3=$(curl_request "PUT" "$BASE_URL/api/orders/$TEST_ORDER_ID/status" \
        '{"status":"INVALID_STATUS"}' "$MOM_TOKEN")
    
    local status_code3="${status_response3: -3}"
    local response_body3="${status_response3%???}"
    
    if test_status_code 200 "$status_code3" "Update Order Status with Invalid Status"; then
        test_json_field "$response_body3" "success" "false" "Invalid Status Rejected"
        print_success "✅ Invalid status validation working"
    else
        print_error "❌ Invalid status validation failed (Status: $status_code3)"
    fi
}

# =============================================================================
# Authorization Tests
# =============================================================================

test_authorization() {
    print_subheader "Order Authorization Tests"
    
    # Test unauthorized mom (Alice - 3 sessions)
    print_test "Unauthorized Mom Access (Should Fail)"
    local alice_response
    alice_response=$(curl_request "POST" "$BASE_URL/api/auth/login" \
        '{"email":"alice@example.com","password":"password123"}')
    
    local alice_status="${alice_response: -3}"
    local alice_body="${alice_response%???}"
    
    if test_status_code 200 "$alice_status" "Alice Login"; then
        local alice_token
        alice_token=$(echo "$alice_body" | jq -r '.data.token' 2>/dev/null || echo "")
        
        if [ -n "$alice_token" ] && [ "$alice_token" != "null" ]; then
            local unauthorized_response
            unauthorized_response=$(curl_request "GET" "$BASE_URL/api/orders" "" "$alice_token")
            
            local unauthorized_status="${unauthorized_response: -3}"
            if test_status_code 403 "$unauthorized_status" "Unauthorized Mom Access"; then
                print_success "✅ Unauthorized mom correctly denied access"
            else
                print_error "❌ Unauthorized mom access control failed (Status: $unauthorized_status)"
            fi
        else
            print_error "❌ Failed to get Alice token"
        fi
    else
        print_error "❌ Alice login failed"
    fi
    
    # Test without authentication
    print_test "Access Without Authentication (Should Fail)"
    local no_auth_response
    no_auth_response=$(curl_request "GET" "$BASE_URL/api/orders" "" "")
    
    local no_auth_status="${no_auth_response: -3}"
    if test_status_code 401 "$no_auth_status" "Access Without Authentication"; then
        print_success "✅ Unauthenticated access correctly denied"
    else
        print_error "❌ Unauthenticated access control failed (Status: $no_auth_status)"
    fi
}

# =============================================================================
# Main Test Execution
# =============================================================================

main() {
    print_header "ORDER MANAGEMENT API TESTS"
    
    # Check server health
    if ! check_server_health; then
        print_error "❌ Server is not running. Please start the server first."
        exit 1
    fi
    
    # Setup authentication
    if ! setup_authentication; then
        print_error "❌ Authentication setup failed"
        exit 1
    fi
    
    # Run order tests
    test_create_order_from_request
    test_create_order_from_cart
    test_create_order_validation
    test_get_orders
    test_get_order_by_id
    test_update_order_status
    test_authorization
    
    print_header "ORDER MANAGEMENT API - TEST RESULTS"
    print_success "🎉 All order management tests completed!"
}

# Run tests if script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
