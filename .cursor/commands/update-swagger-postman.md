# Update Swagger & Postman

## Overview
Quick guide for updating Swagger (OpenAPI) documentation and Postman collection when making API changes.

## Quick Update Checklist
- [ ] OpenAPI documentation updated
- [ ] Postman collection updated
- [ ] Examples tested and working
- [ ] Authentication requirements documented
- [ ] Error responses documented

## Steps

### 1. Update OpenAPI Documentation
```bash
# Edit OpenAPI file
code src/main/resources/openapi/documentation.yaml

# Validate YAML syntax
python -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" 2>/dev/null || echo "YAML validation failed"

# Test Swagger UI
curl -s http://localhost:8080/swagger | head -20
```

**For New Endpoints:**
```yaml
/api/your-endpoint:
  get:
    operationId: getYourEndpoint
    tags: [ YourTag ]
    summary: Brief description
    description: Detailed description
    security:
      - BearerAuth: []
    responses:
      "200":
        description: Success description
        content:
          application/json:
            schema:
              allOf:
                - $ref: "#/components/schemas/BasicApiResponse"
                - type: object
                  properties:
                    data:
                      $ref: "#/components/schemas/YourResponseModel"
            examples:
              success_example:
                summary: Success response
                value:
                  success: true
                  data:
                    id: "example_id"
                    name: "Example Name"
      "400":
        description: Bad request
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/BasicApiResponse"
            examples:
              error_example:
                summary: Error response
                value:
                  success: false
                  message: "Error description"
      "401":
        description: Unauthorized
      "403":
        description: Forbidden
```

**For Request Bodies:**
```yaml
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: "#/components/schemas/YourRequestModel"
      examples:
        request_example:
          summary: Request example
          value:
            field1: "value1"
            field2: "value2"
```

### 2. Update Postman Collection
```bash
# Edit Postman collection
code postman_collection.json

# Validate JSON syntax
python -c "import json; json.load(open('postman_collection.json'))" 2>/dev/null || echo "JSON validation failed"
```

**For New Requests:**
```json
{
  "name": "Your Endpoint Name",
  "request": {
    "method": "GET",
    "header": [
      {"key": "Authorization", "value": "Bearer {{auth_token}}", "type": "text"}
    ],
    "url": {
      "raw": "{{base_url}}/api/your-endpoint",
      "host": ["{{base_url}}"],
      "path": ["api", "your-endpoint"]
    },
    "description": "Description of what this endpoint does"
  }
}
```

**For POST/PUT Requests:**
```json
{
  "name": "Create/Update Resource",
  "request": {
    "method": "POST",
    "header": [
      {"key": "Authorization", "value": "Bearer {{auth_token}}", "type": "text"},
      {"key": "Content-Type", "value": "application/json", "type": "text"}
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"field1\": \"value1\",\n  \"field2\": \"value2\"\n}"
    },
    "url": {
      "raw": "{{base_url}}/api/your-endpoint",
      "host": ["{{base_url}}"],
      "path": ["api", "your-endpoint"]
    },
    "description": "Description of what this endpoint does"
  }
}
```

### 3. Test Your Updates
```bash
# Start server if not running
./gradlew run &

# Test endpoint manually
curl -X GET http://localhost:8080/api/your-endpoint \
  -H "Authorization: Bearer YOUR_TOKEN" | jq .

# Test with Postman
# Import updated collection
# Run the new request
# Verify response matches documentation
```

### 4. Validate Documentation
```bash
# Check Swagger UI
open http://localhost:8080/swagger

# Verify all endpoints are listed
# Check examples work
# Verify authentication requirements
# Test error responses
```

## Common Update Scenarios

### New GET Endpoint
1. **OpenAPI**: Add endpoint definition with response schema
2. **Postman**: Add GET request with proper headers
3. **Test**: Verify endpoint works and returns expected data

### New POST Endpoint
1. **OpenAPI**: Add endpoint with request/response schemas
2. **Postman**: Add POST request with body example
3. **Test**: Verify endpoint accepts data and returns response

### New Authentication Required
1. **OpenAPI**: Add security requirement to endpoint
2. **Postman**: Add Authorization header
3. **Test**: Verify authentication works and unauthorized requests fail

### New Validation Rules
1. **OpenAPI**: Add validation examples and error responses
2. **Postman**: Add test cases for validation errors
3. **Test**: Verify validation works and returns proper errors

## Validation Commands

### OpenAPI Validation
```bash
# Check YAML syntax
python -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))"

# Count endpoints
grep -c "operationId:" src/main/resources/openapi/documentation.yaml

# Check for missing examples
grep -c "example:" src/main/resources/openapi/documentation.yaml

# Verify all endpoints have descriptions
grep -c "description:" src/main/resources/openapi/documentation.yaml
```

### Postman Validation
```bash
# Check JSON syntax
python -c "import json; json.load(open('postman_collection.json'))"

# Count requests
grep -c '"name":' postman_collection.json

# Check for missing descriptions
grep -c '"description"' postman_collection.json

# Verify authentication setup
grep -c "Bearer" postman_collection.json
```

## Quick Reference

### OpenAPI Components
```yaml
# Add to components/schemas
YourRequestModel:
  type: object
  required:
    - field1
  properties:
    field1:
      type: string
      example: "example value"
    field2:
      type: integer
      example: 123

YourResponseModel:
  type: object
  properties:
    id:
      type: string
      example: "507f1f77bcf86cd799439011"
    name:
      type: string
      example: "Example Name"
    createdAt:
      type: integer
      format: int64
      example: 1640995200000
```

### Postman Environment Variables
```json
{
  "name": "Mom Care Platform",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "auth_token",
      "value": "your_jwt_token_here",
      "enabled": true
    }
  ]
}
```

## Best Practices

### OpenAPI Best Practices
- Use descriptive operation IDs
- Include comprehensive examples
- Document all possible response codes
- Use consistent naming conventions
- Group related endpoints with tags

### Postman Best Practices
- Use descriptive request names
- Include proper authentication
- Add request body examples
- Use environment variables
- Include test scripts for validation

### Testing Best Practices
- Test all examples manually
- Verify authentication works
- Test error scenarios
- Check response formats
- Validate against actual API behavior

## Troubleshooting

### Common Issues
- **YAML syntax errors**: Use YAML validator
- **JSON syntax errors**: Use JSON validator
- **Missing examples**: Add comprehensive examples
- **Broken authentication**: Verify token format
- **Outdated responses**: Update to match actual API

### Solutions
- Validate syntax before committing
- Test all examples manually
- Keep documentation in sync with code
- Use consistent naming conventions
- Regular documentation reviews

## Maintenance Schedule

### After Each API Change
- [ ] Update OpenAPI documentation
- [ ] Update Postman collection
- [ ] Test examples
- [ ] Validate syntax

### Weekly
- [ ] Review documentation accuracy
- [ ] Test all examples
- [ ] Check for missing endpoints
- [ ] Verify authentication

### Monthly
- [ ] Comprehensive documentation review
- [ ] Update all examples
- [ ] Check for outdated information
- [ ] Plan documentation improvements
