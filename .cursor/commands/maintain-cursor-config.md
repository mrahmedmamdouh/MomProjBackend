# Maintain Cursor Configuration

## Overview
Guide for maintaining and updating Cursor rules and commands to keep them current with project changes.

## Maintenance Checklist
- [ ] Review Cursor rules for accuracy
- [ ] Update Cursor commands with new workflows
- [ ] Verify file references are correct
- [ ] Test rules and commands functionality
- [ ] Update examples and code snippets

## Steps

### 1. Review Cursor Rules
```bash
# Check all rule files
ls -la .cursor/rules/

# Review rule content
grep -r "alwaysApply\|globs" .cursor/rules/

# Check file references
grep -r "mdc:" .cursor/rules/
```

**Rule Review Checklist:**
- [ ] Rules apply to correct file types (globs)
- [ ] File references use correct paths
- [ ] Code examples are current and accurate
- [ ] Rules match current project structure
- [ ] No outdated information

### 2. Update Cursor Commands
```bash
# Check all command files
ls -la .cursor/commands/

# Review command structure
head -10 .cursor/commands/*.md
```

**Command Update Checklist:**
- [ ] Step-by-step instructions are accurate
- [ ] Code examples work with current codebase
- [ ] Checklists are comprehensive
- [ ] File paths are correct
- [ ] Commands reflect current workflows

### 3. Verify File References
```bash
# Check if referenced files exist
find .cursor/rules/ -name "*.mdc" -exec grep -l "mdc:" {} \;

# Verify file paths
grep -r "mdc:" .cursor/rules/ | while read line; do
  file=$(echo "$line" | sed 's/.*\[\([^]]*\)\](mdc:\([^)]*\)).*/\2/')
  if [ ! -f "$file" ]; then
    echo "Missing file: $file"
  fi
done
```

### 4. Test Rules and Commands
```bash
# Test rule application
# Create test files matching rule globs
touch test.kt test.sh test.yaml

# Open in Cursor and verify rules appear
# Test command functionality
# Verify examples work
```

## Common Update Scenarios

### New File Types Added
1. **Update Rules**: Add new glob patterns
2. **Update Commands**: Add new file type handling
3. **Test**: Verify rules apply to new file types

### Project Structure Changes
1. **Update Rules**: Update file references
2. **Update Commands**: Update file paths
3. **Test**: Verify all references work

### New Development Patterns
1. **Update Rules**: Add new patterns and examples
2. **Update Commands**: Add new workflow steps
3. **Test**: Verify patterns work correctly

### Technology Stack Changes
1. **Update Rules**: Update technology-specific rules
2. **Update Commands**: Update setup and usage instructions
3. **Test**: Verify all technology references are accurate

## Rule Maintenance

### Check Rule Applicability
```bash
# Review alwaysApply rules
grep -r "alwaysApply: true" .cursor/rules/

# Review glob-based rules
grep -r "globs:" .cursor/rules/

# Check for conflicts or overlaps
```

### Update File References
```bash
# Find all file references
grep -r "mdc:" .cursor/rules/

# Verify each reference exists
for file in $(grep -r "mdc:" .cursor/rules/ | sed 's/.*mdc:\([^)]*\).*/\1/' | sort -u); do
  if [ -f "$file" ]; then
    echo "✅ $file exists"
  else
    echo "❌ $file missing"
  fi
done
```

### Update Code Examples
```bash
# Check for outdated code patterns
grep -r "kotlinx.serialization" .cursor/rules/
grep -r "Gson()" .cursor/rules/
grep -r "println" .cursor/rules/
```

## Command Maintenance

### Review Command Accuracy
```bash
# Check for outdated file paths
grep -r "src/main/kotlin" .cursor/commands/
grep -r "tests/" .cursor/commands/
grep -r "build.gradle" .cursor/commands/
```

### Update Workflow Steps
```bash
# Review step-by-step instructions
grep -r "Step [0-9]" .cursor/commands/
grep -r "## Steps" .cursor/commands/
```

### Verify Code Examples
```bash
# Check for working code examples
grep -r "curl" .cursor/commands/
grep -r "gradlew" .cursor/commands/
grep -r "git" .cursor/commands/
```

## Automated Maintenance Scripts

### Rule Validation Script
```bash
#!/bin/bash
# validate-rules.sh

echo "Validating Cursor rules..."

# Check rule files exist
if [ ! -d ".cursor/rules" ]; then
  echo "❌ Rules directory missing"
  exit 1
fi

# Check for proper frontmatter
for file in .cursor/rules/*.mdc; do
  if ! grep -q "alwaysApply\|globs" "$file"; then
    echo "❌ $file missing frontmatter"
  fi
done

# Check file references
grep -r "mdc:" .cursor/rules/ | while read line; do
  file=$(echo "$line" | sed 's/.*\[\([^]]*\)\](mdc:\([^)]*\)).*/\2/')
  if [ ! -f "$file" ]; then
    echo "❌ Missing file reference: $file"
  fi
done

echo "✅ Rule validation completed"
```

### Command Validation Script
```bash
#!/bin/bash
# validate-commands.sh

echo "Validating Cursor commands..."

# Check command files exist
if [ ! -d ".cursor/commands" ]; then
  echo "❌ Commands directory missing"
  exit 1
fi

# Check for proper structure
for file in .cursor/commands/*.md; do
  if ! grep -q "## Overview" "$file"; then
    echo "❌ $file missing Overview section"
  fi
  if ! grep -q "## Steps" "$file"; then
    echo "❌ $file missing Steps section"
  fi
done

# Check for outdated references
grep -r "src/main/kotlin" .cursor/commands/ | while read line; do
  file=$(echo "$line" | sed 's/.*src\/main\/kotlin\/\([^[:space:]]*\).*/\1/')
  if [ ! -f "src/main/kotlin/$file" ]; then
    echo "❌ Missing file reference: src/main/kotlin/$file"
  fi
done

echo "✅ Command validation completed"
```

## Maintenance Schedule

### Daily
- [ ] Check for new files that need rule coverage
- [ ] Verify rules apply to new file types
- [ ] Test command functionality

### Weekly
- [ ] Review rule accuracy
- [ ] Update command examples
- [ ] Check file references

### Monthly
- [ ] Comprehensive rule review
- [ ] Update all commands
- [ ] Test all functionality
- [ ] Archive outdated content

### Quarterly
- [ ] Full configuration audit
- [ ] Update all examples
- [ ] Review maintenance procedures
- [ ] Plan improvements

## Quality Checks

### Rule Quality
```bash
# Check for proper formatting
grep -r "```" .cursor/rules/

# Check for code examples
grep -r "✅\|❌" .cursor/rules/

# Check for file references
grep -r "mdc:" .cursor/rules/
```

### Command Quality
```bash
# Check for step-by-step structure
grep -r "## Steps" .cursor/commands/

# Check for checklists
grep -r "## Checklist" .cursor/commands/

# Check for code examples
grep -r "```" .cursor/commands/
```

## Best Practices

### Rule Maintenance
1. **Keep rules focused** and specific
2. **Update immediately** when project changes
3. **Test rules** regularly
4. **Remove outdated** information
5. **Add new patterns** as they emerge

### Command Maintenance
1. **Keep commands current** with workflows
2. **Test all examples** regularly
3. **Update step-by-step** instructions
4. **Verify file paths** are correct
5. **Add new commands** for new workflows

### Quality Assurance
1. **Validate syntax** of all files
2. **Test functionality** regularly
3. **Review accuracy** of all information
4. **Check completeness** of coverage
5. **Maintain consistency** across all files

## Troubleshooting

### Common Issues
- **Rules not applying**: Check glob patterns
- **File references broken**: Verify file paths
- **Commands outdated**: Update step-by-step instructions
- **Examples not working**: Test and update code
- **Missing coverage**: Add new rules/commands

### Solutions
- Use validation scripts
- Test rules and commands regularly
- Keep documentation in sync with code
- Use consistent naming conventions
- Regular maintenance reviews

## Success Metrics

### Rule Quality
- All file types covered
- All examples working
- No broken references
- Consistent formatting
- Up-to-date information

### Command Quality
- All workflows covered
- All examples working
- Clear instructions
- Comprehensive checklists
- Current information

### Maintenance Efficiency
- Quick update process
- Automated validation
- Clear procedures
- Regular schedule
- Team participation
