# Cleanup Log Files

## Overview
Clean up temporary log files, reports, and analysis files to maintain a clean workspace.

## Quick Cleanup
```bash
# Clean all log files
rm -f *.log

# Clean health check reports  
rm -f health-check-report-*.txt

# Clean analysis files
rm -f *-analysis.* *-report.*

# Clean all temporary files at once
rm -f *.log health-check-report-*.txt *-analysis.* *-report.*
```

## Detailed Cleanup
```bash
# Check what log files exist
find . -name "*.log" -type f
find . -name "*report*.txt" -type f
find . -name "*analysis*" -type f

# Remove specific log types
rm -f test-results.log
rm -f test_results_final.log
rm -f comprehensive-test-results.log
rm -f test-failures-analysis.log
rm -f test-failures-analysis.md

# Remove health check reports
rm -f health-check-report-*.txt
```

## Verification
```bash
# Verify cleanup
ls -la *.log 2>/dev/null || echo "No log files found"
ls -la *report*.txt 2>/dev/null || echo "No report files found"
ls -la *analysis* 2>/dev/null || echo "No analysis files found"
```

## Best Practices
- **Clean after each development session**
- **Never commit log files** to version control
- **Use .gitignore** to prevent accidental commits
- **Keep workspace clean** for better performance
- **Document important findings** before cleanup

## Integration with Development Workflow
1. **Run tests/health checks** as needed
2. **Analyze results** from generated files
3. **Document important findings** in permanent files
4. **Run cleanup** to remove temporary files
5. **Commit only essential changes**

## Related Commands
- `run-health-check` - Generates health check reports
- `run-test-suite` - Generates test logs
- `update-documentation` - Updates permanent documentation
