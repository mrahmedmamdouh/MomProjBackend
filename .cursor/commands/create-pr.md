# Create Pull Request

## Overview
Step-by-step guide for creating a comprehensive pull request for the Mom Care Platform backend.

## Pre-PR Checklist
- [ ] All tests are passing (`./tests/run-all-tests.sh`)
- [ ] Code follows project standards (see `.cursorrules`)
- [ ] Documentation is updated (Swagger, Postman)
- [ ] No debugging code or comments (except actionable TODOs)
- [ ] Security review completed
- [ ] Performance impact assessed

## PR Creation Steps

### 1. Final Code Review
```bash
# Run comprehensive tests
./tests/run-all-tests.sh

# Check build
./gradlew build -x test

# Verify no linting issues
./gradlew ktlintCheck
```

### 2. Commit Final Changes
```bash
# Stage all changes
git add .

# Create descriptive commit message
git commit -m "feat: Add new feature description

- Brief description of changes
- Key improvements made
- Any breaking changes noted
- Related issue numbers"

# Push to feature branch
git push origin feature/your-branch-name
```

### 3. Create Pull Request

#### PR Title Format
```
feat: Add mom profile photo upload functionality
fix: Resolve doctor authorization check bug
docs: Update API documentation for new endpoints
test: Add comprehensive tests for e-commerce features
```

#### PR Description Template
```markdown
## Description
Brief description of what this PR accomplishes.

## Changes Made
- [ ] Added new API endpoint: `/api/moms/profile/photo`
- [ ] Updated request validation for photo uploads
- [ ] Enhanced error handling for file operations
- [ ] Added comprehensive tests for new functionality

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update
- [ ] Test improvements

## Testing
- [ ] All existing tests pass
- [ ] New tests added for new functionality
- [ ] Manual testing completed
- [ ] API tests run successfully

## Security Considerations
- [ ] Input validation implemented
- [ ] File upload security measures in place
- [ ] No sensitive data exposed
- [ ] Authentication/authorization properly implemented

## Documentation
- [ ] OpenAPI documentation updated
- [ ] Postman collection updated
- [ ] README updated if needed
- [ ] Code comments are minimal and appropriate

## Related Issues
Closes #123
Related to #456

## Screenshots/Examples
(If applicable, add screenshots or example API responses)

## Checklist
- [ ] Code follows project standards
- [ ] Tests are comprehensive
- [ ] Documentation is updated
- [ ] Security review completed
- [ ] Performance impact assessed
- [ ] Ready for review
```

### 4. PR Labels
Add appropriate labels:
- `enhancement` - New features
- `bug` - Bug fixes
- `documentation` - Documentation updates
- `tests` - Test improvements
- `security` - Security-related changes
- `performance` - Performance improvements

### 5. Assign Reviewers
- Assign appropriate team members
- Include security reviewer for sensitive changes
- Include documentation reviewer for API changes

## PR Review Process

### For Reviewers
1. **Code Quality Review**
   - Follow the code review checklist
   - Check for adherence to project standards
   - Verify test coverage

2. **Security Review**
   - Check for security vulnerabilities
   - Verify input validation
   - Review authentication/authorization logic

3. **Documentation Review**
   - Verify API documentation is updated
   - Check Postman collection updates
   - Ensure examples are accurate

### For Author
1. **Address Feedback**
   - Respond to all review comments
   - Make necessary changes
   - Update tests if needed

2. **Final Verification**
   - Run tests again after changes
   - Verify documentation is still accurate
   - Check for any new issues

## Merge Requirements
- [ ] All tests passing
- [ ] At least 2 approvals
- [ ] Security review completed (if applicable)
- [ ] Documentation review completed
- [ ] No merge conflicts
- [ ] Feature branch is up to date with main

## Post-Merge Tasks
- [ ] Delete feature branch
- [ ] Update related issues
- [ ] Notify team of deployment
- [ ] Monitor for any issues

## Common PR Patterns

### New API Endpoint
```markdown
## Description
Adds new endpoint for mom profile photo upload with multipart support.

## Changes Made
- [ ] New route: `PUT /api/moms/profile/photo`
- [ ] Multipart request handling
- [ ] File validation and security
- [ ] Updated mom service with photo handling
- [ ] Comprehensive tests for photo upload

## Testing
- [ ] Unit tests for service layer
- [ ] API tests for endpoint
- [ ] File upload validation tests
- [ ] Error handling tests
```

### Bug Fix
```markdown
## Description
Fixes critical bug where unauthorized moms couldn't check their authorization status.

## Changes Made
- [ ] Created `momRouteBasic` interceptor
- [ ] Updated check-authorization endpoint
- [ ] Added tests for unauthorized access
- [ ] Updated documentation

## Testing
- [ ] Verified unauthorized moms can check status
- [ ] All existing tests still pass
- [ ] Added regression tests
```

### Documentation Update
```markdown
## Description
Updates API documentation with new endpoints and examples.

## Changes Made
- [ ] Updated OpenAPI specification
- [ ] Enhanced Postman collection
- [ ] Added request/response examples
- [ ] Updated endpoint descriptions

## Testing
- [ ] Verified all examples work
- [ ] Tested Swagger UI display
- [ ] Validated Postman collection
```

## Best Practices
1. **Keep PRs focused** - One feature/fix per PR
2. **Write clear descriptions** - Help reviewers understand the changes
3. **Include tests** - Always add tests for new functionality
4. **Update documentation** - Keep docs in sync with code
5. **Review your own code** - Self-review before requesting review
6. **Respond promptly** - Address feedback quickly
7. **Test thoroughly** - Don't rely only on automated tests
