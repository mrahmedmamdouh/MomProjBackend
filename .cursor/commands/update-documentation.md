# Update Documentation

## Overview
Comprehensive guide for keeping all project documentation updated and synchronized with code changes.

## Documentation Update Checklist
- [ ] OpenAPI/Swagger documentation updated
- [ ] Postman collection updated
- [ ] Cursor rules reviewed and updated
- [ ] Cursor commands reviewed and updated
- [ ] README.md updated if needed
- [ ] Code comments updated (only actionable TODOs)

## Steps

### 1. Update OpenAPI Documentation
```bash
# Check current OpenAPI file
head -50 src/main/resources/openapi/documentation.yaml

# Validate YAML syntax
python3 -c "import yaml; yaml.safe_load(open('src/main/resources/openapi/documentation.yaml'))" 2>/dev/null || echo "YAML validation failed"

# Test Swagger UI
curl -s http://localhost:8080/swagger | head -20
```

**OpenAPI Update Requirements:**
- [ ] Add new endpoints with proper operation IDs
- [ ] Update request/response schemas
- [ ] Include examples for success and error responses
- [ ] Document authorization requirements
- [ ] Update tags and descriptions
- [ ] Verify YAML syntax is correct

### 2. Update Postman Collection
```bash
# Check current Postman collection
head -50 postman_collection.json

# Validate JSON syntax
python3 -c "import json; json.load(open('postman_collection.json'))" 2>/dev/null || echo "JSON validation failed"
```

**Postman Update Requirements:**
- [ ] Add new request examples
- [ ] Update endpoint URLs and methods
- [ ] Include proper headers and authentication
- [ ] Add request body examples
- [ ] Update descriptions and documentation
- [ ] Verify JSON syntax is correct

### 3. Update Cursor Rules
```bash
# Check all rule files
ls -la .cursor/rules/

# Review rule content
grep -r "alwaysApply\|globs" .cursor/rules/
```

**Cursor Rules Update Requirements:**
- [ ] Review rule applicability (alwaysApply vs globs)
- [ ] Update file references using [filename](mdc:path) format
- [ ] Add new patterns and examples
- [ ] Remove outdated information
- [ ] Ensure rules match current project structure

### 4. Update Cursor Commands
```bash
# Check all command files
ls -la .cursor/commands/

# Review command structure
head -10 .cursor/commands/*.md
```

**Cursor Commands Update Requirements:**
- [ ] Update step-by-step instructions
- [ ] Add new commands for new workflows
- [ ] Update examples and code snippets
- [ ] Verify command accuracy
- [ ] Add new checklist items

### 5. Update Project Documentation
```bash
# Check README
head -20 README.md

# Check for outdated information
grep -r "TODO\|FIXME\|OUTDATED" docs/ README.md
```

**Project Documentation Update Requirements:**
- [ ] Update README.md with current setup instructions
- [ ] Update API documentation
- [ ] Update deployment instructions
- [ ] Update development guidelines
- [ ] Remove outdated information

## Automated Update Scripts

### Update OpenAPI from Code
```bash
#!/bin/bash
# update-openapi.sh

echo "Updating OpenAPI documentation..."

# Extract endpoints from route files
grep -r "route(\|get(\|post(\|put(\|delete(" src/main/kotlin/routes/ > endpoints.tmp

# Generate basic OpenAPI structure
echo "Generated endpoints:"
cat endpoints.tmp

# Clean up
rm endpoints.tmp

echo "OpenAPI update completed. Please review and update manually."
```

### Update Postman from OpenAPI
```bash
#!/bin/bash
# update-postman.sh

echo "Updating Postman collection from OpenAPI..."

# Convert OpenAPI to Postman (requires openapi-to-postman tool)
# npm install -g openapi-to-postman
# openapi-to-postman -s src/main/resources/openapi/documentation.yaml -o postman_collection.json

echo "Postman collection updated. Please review and test."
```

## Documentation Standards

### OpenAPI Standards
- Use proper operation IDs (camelCase)
- Include comprehensive examples
- Document all response codes
- Use consistent naming conventions
- Include security requirements

### Postman Standards
- Use descriptive request names
- Include proper authentication
- Add request body examples
- Use environment variables
- Include test scripts

### Cursor Rules Standards
- Use proper frontmatter (alwaysApply, globs)
- Include file references with [filename](mdc:path)
- Provide code examples
- Keep rules focused and specific
- Update when project structure changes

### Cursor Commands Standards
- Use clear step-by-step instructions
- Include comprehensive checklists
- Provide code examples
- Keep commands up-to-date
- Test commands regularly

## Regular Maintenance Schedule

### Daily
- [ ] Check for new endpoints that need documentation
- [ ] Verify Swagger UI is accessible
- [ ] Test Postman collection

### Weekly
- [ ] Review and update OpenAPI documentation
- [ ] Update Postman collection
- [ ] Check Cursor rules for accuracy

### Monthly
- [ ] Comprehensive documentation review
- [ ] Update Cursor commands
- [ ] Review and update project README
- [ ] Archive outdated documentation

### Quarterly
- [ ] Full documentation audit
- [ ] Update all examples and code snippets
- [ ] Review documentation standards
- [ ] Plan documentation improvements

## Quality Checks

### OpenAPI Quality
```bash
# Validate OpenAPI syntax
swagger-codegen validate -i src/main/resources/openapi/documentation.yaml

# Check for missing examples
grep -c "example:" src/main/resources/openapi/documentation.yaml

# Verify all endpoints have descriptions
grep -c "description:" src/main/resources/openapi/documentation.yaml
```

### Postman Quality
```bash
# Validate JSON syntax
python3 -c "import json; json.load(open('postman_collection.json'))"

# Check for missing descriptions
grep -c "description" postman_collection.json

# Verify authentication setup
grep -c "Bearer" postman_collection.json
```

### Cursor Rules Quality
```bash
# Check for proper frontmatter
grep -r "alwaysApply\|globs" .cursor/rules/

# Verify file references
grep -r "mdc:" .cursor/rules/

# Check for broken references
find .cursor/rules/ -name "*.mdc" -exec grep -l "mdc:" {} \;
```

## Common Update Scenarios

### New API Endpoint Added
1. **Update OpenAPI**: Add endpoint definition
2. **Update Postman**: Add request example
3. **Update Rules**: Add validation patterns if needed
4. **Update Commands**: Add testing steps if needed

### Authentication Changes
1. **Update OpenAPI**: Modify security requirements
2. **Update Postman**: Update authentication headers
3. **Update Rules**: Update authentication patterns
4. **Update Commands**: Update security audit steps

### New Validation Rules
1. **Update Rules**: Add validation patterns
2. **Update OpenAPI**: Add validation examples
3. **Update Postman**: Add validation test cases
4. **Update Commands**: Update testing procedures

### Project Structure Changes
1. **Update Rules**: Update file references
2. **Update Commands**: Update file paths
3. **Update README**: Update setup instructions
4. **Update OpenAPI**: Update server URLs if needed

## Documentation Testing

### Test OpenAPI
```bash
# Start server
./gradlew run &

# Test Swagger UI
curl -s http://localhost:8080/swagger | grep -i "swagger"

# Test API endpoints
curl -s http://localhost:8080/api/categories | jq .
```

### Test Postman
```bash
# Import collection into Postman
# Run collection tests
# Verify all requests work
# Check authentication
```

### Test Cursor Rules
```bash
# Open Cursor
# Create new file matching rule globs
# Verify rules appear
# Test rule guidance
```

## Best Practices

### Documentation Maintenance
1. **Update immediately** when making code changes
2. **Test documentation** regularly
3. **Keep examples current** and working
4. **Review documentation** during code reviews
5. **Automate where possible** but verify manually

### Quality Assurance
1. **Validate syntax** of all documentation files
2. **Test examples** to ensure they work
3. **Review accuracy** of all information
4. **Check completeness** of documentation
5. **Maintain consistency** across all docs

### Team Collaboration
1. **Assign documentation** responsibilities
2. **Review documentation** in pull requests
3. **Train team members** on documentation standards
4. **Regular documentation** reviews and updates
5. **Share documentation** improvements

## Troubleshooting

### Common Issues
- **YAML syntax errors** in OpenAPI files
- **JSON syntax errors** in Postman collections
- **Broken file references** in Cursor rules
- **Outdated examples** in documentation
- **Missing authentication** in Postman requests

### Solutions
- Use YAML/JSON validators
- Test all examples manually
- Verify file references exist
- Update examples regularly
- Test authentication flows

## Success Metrics

### Documentation Quality
- All endpoints documented
- All examples working
- No syntax errors
- Consistent formatting
- Up-to-date information

### Maintenance Efficiency
- Quick update process
- Automated validation
- Clear update procedures
- Regular maintenance schedule
- Team participation
