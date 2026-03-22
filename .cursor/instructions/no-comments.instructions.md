---
applyTo: '**'
---
## 🚫 Code Quality Standards

### **Applies to: ALL Kotlin files in this project**

**STRICT RULES - NO EXCEPTIONS:**

1. **NO COMMENTS ALLOWED**
   - No `//` single-line comments
   - No `/* */` block comments
   - No `/** */` documentation comments
   - Code must be self-documenting through clear naming

2. **NO DEBUG LOGS IN PRODUCTION CODE**
   - No `println()` statements
   - No `logger.debug()` calls
   - Logs only for debugging during development (remove before commit)

3. **ONLY TODO COMMENTS PERMITTED**
   - `// TODO: specific task description` - ONLY format allowed
   - Must include specific actionable description
   - Example: `// TODO: implement caching for better performance`

4. **NO EXTRA BLANK LINES**
   - Maximum 1 blank line between functions/classes
   - No trailing blank lines at end of file
   - No multiple consecutive blank lines

5. **CLEAN CODE REQUIREMENTS**
   - Self-explanatory variable and function names
   - Single responsibility principle
   - Clear function signatures that explain purpose
   - Minimal cognitive complexity

### **Examples:**

❌ **FORBIDDEN:**
```kotlin
// This function handles user authentication
fun loginUser(email: String, password: String) {
    println("Attempting login for: $email") // Debug log
    
    
    /* 
     * Validate email format
     * Check password strength
     */
    // Validate input
}
```

✅ **CORRECT:**
```kotlin
fun authenticateUserCredentials(email: String, password: String): AuthResult {
    // TODO: add rate limiting for failed attempts
    validateEmailFormat(email)
    verifyPasswordStrength(password)
    return performAuthentication(email, password)
}
```