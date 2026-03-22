# Run Health Check

## Overview
Quick command to run the comprehensive health check for all project systems.

## Steps

### 1. Run Health Check
```bash
# Run the complete health check
./health-check.sh
```

### 2. Check Results
The health check will:
- ✅ **Validate Swagger/OpenAPI** documentation
- ✅ **Validate Postman collection** JSON syntax
- ✅ **Check test automation scripts** are executable
- ✅ **Run unit tests** and report status
- ✅ **Validate documentation** files
- ✅ **Check Cursor rules** and commands
- ✅ **Verify project structure** is complete
- ✅ **Check dependencies** and build system

### 3. Review Health Report
```bash
# View the latest health report
ls -la health-check-report-*.txt | tail -1 | xargs cat

# Clean up health check reports after analysis
rm -f health-check-report-*.txt
```

### 4. Fix Issues
Based on the health check results:
- **Failed checks** need immediate attention
- **Warning checks** should be addressed soon
- **Passed checks** are working correctly

## Health Check Categories

### Swagger/OpenAPI
- YAML syntax validation
- Endpoint count and examples
- Swagger UI accessibility

### Postman Collection
- JSON syntax validation
- Request count and descriptions
- Authentication setup

### Test Automation
- Script existence and executability
- Required functions availability
- Test coverage

### Unit Tests
- Test file count
- Test execution status
- Build integration

### Documentation
- README existence and content
- Outdated content detection
- File structure validation

### Cursor Configuration
- Rules validation and file references
- Commands structure and examples
- Configuration completeness

### Project Structure
- Key directories and files
- Build system integrity
- Dependency resolution

## Quick Health Check Commands

### Run All Checks
```bash
./health-check.sh
```

### Check Specific Systems
```bash
# Check Swagger only
curl -s http://localhost:8080/swagger | head -20

# Check Postman only
python -c "import json; json.load(open('postman_collection.json'))" && echo "✅ Postman valid"

# Check test scripts only
ls -la tests/*.sh

# Check unit tests only
./gradlew test --no-daemon

# Check documentation only
[ -f "README.md" ] && echo "✅ Documentation available"

# Check Cursor config only
[ -d ".cursor/rules" ] && [ -d ".cursor/commands" ] && echo "✅ Cursor config available"
```

## Health Check Results

### Exit Codes
- **0**: All systems healthy
- **1**: Mostly healthy with minor issues
- **2**: System needs attention

### Status Indicators
- **✅ PASS**: System working correctly
- **⚠️ WARN**: Minor issue, should be addressed
- **❌ FAIL**: Critical issue, needs immediate attention
- **📊 INFO**: Informational message

## Best Practices

### Regular Health Checks
- **Daily**: Quick validation of key systems
- **Weekly**: Full health check
- **Before releases**: Comprehensive validation
- **After major changes**: Full system check

### Health Check Triggers
- **Code changes**: Validate affected systems
- **Documentation updates**: Check all references
- **New features**: Validate all systems
- **Dependency updates**: Check compatibility

### Quality Standards
- **All systems must pass** before deployment
- **Documentation must be current** and accurate
- **Tests must pass** and be comprehensive
- **Examples must work** and be tested

## Troubleshooting

### Common Issues
- **YAML/JSON syntax errors**: Use validators
- **Missing files**: Check project structure
- **Broken references**: Update file paths
- **Test failures**: Fix test issues
- **Build failures**: Resolve dependencies

### Solutions
- **Validate syntax** before committing
- **Test examples** regularly
- **Keep documentation** in sync
- **Run health checks** frequently
- **Fix issues** immediately

## Success Metrics

### Health Check Success
- **All syntax validations pass**
- **All file references work**
- **All tests pass**
- **All examples work**
- **All documentation current**

### System Health
- **Swagger accessible** and valid
- **Postman collection** working
- **Test automation** functional
- **Unit tests** passing
- **Documentation** up-to-date

### Maintenance Efficiency
- **Quick health checks** available
- **Automated validation** working
- **Clear procedures** for fixes
- **Regular maintenance** schedule
- **Team participation** in health checks
