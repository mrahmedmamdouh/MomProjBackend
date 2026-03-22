---
applyTo: '**'
---
# Transaction Atomicity and Database Architecture

## Overview

This document describes the transaction atomicity implementation for the Mom Care Platform backend, ensuring ACID compliance for critical operations like user registration.

## Problem Statement

The original implementation of `createMomMultipart` and `createDoctorMultipart` methods lacked transaction atomicity, creating several critical issues:

1. **Partial Data Creation**: Failed operations mid-process left inconsistent database states
2. **JWT Token Issues**: Tokens could be generated for incomplete user profiles
3. **File System Inconsistency**: Uploaded files remained even after database failures
4. **Data Integrity Problems**: Orphaned records (e.g., NID records without User records)

## Solution Architecture

### 1. Repository Layer Enhancements

All repository interfaces now include transactional method overloads:

```kotlin
// Example: NidRepository
suspend fun createNid(nid: Nid): Boolean
suspend fun createNid(nid: Nid, session: ClientSession): Boolean
suspend fun deleteNid(id: String): Boolean  
suspend fun deleteNid(id: String, session: ClientSession): Boolean
```

**Repositories with transactional support:**
- `NidRepository`: NID document management
- `MomRepository`: Mom profile and authentication
- `DoctorRepository`: Doctor profile and authentication
- `UserRepository`: Universal user records

### 2. Transaction Service

The `TransactionService` class provides centralized transaction management:

```kotlin
class TransactionService(
    private val mongoClient: MongoClient,
    private val database: MongoDatabase
) {
    suspend fun <T> withTransaction(
        operation: suspend (ClientSession) -> T
    ): T
    
    suspend fun <T> withTransactionAndRollback(
        operation: suspend (ClientSession) -> T,
        rollback: suspend () -> Unit
    ): T
}
```

**Features:**
- Automatic session creation and cleanup
- Transaction start/commit/abort handling
- Proper read/write concerns for consistency
- Optional rollback callbacks for external resources

### 3. Service Layer Implementation

#### Registration Flow (Mom/Doctor)

```kotlin
suspend fun createMomMultipart(...): String? {
    return try {
        transactionService.withTransaction { session ->
            // All operations within single transaction
            val nidSuccess = nidRepository.createNid(nid, session)
            if (!nidSuccess) throw Exception("Failed to create NID record")
            
            val momSuccess = momRepository.createMom(mom, session)
            if (!momSuccess) throw Exception("Failed to create Mom record")
            
            val momAuthSuccess = momRepository.createMomAuth(momAuth, session)
            if (!momAuthSuccess) throw Exception("Failed to create MomAuth record")
            
            val userSuccess = userRepository.createUserEntry(user, session)
            if (!userSuccess) throw Exception("Failed to create User record")
            
            momId // Return success
        }
    } catch (e: Exception) {
        null // All database changes automatically rolled back
    }
}
```

### 4. Route Layer Integration

The authentication routes handle file cleanup on transaction failures:

```kotlin
if (momId != null) {
    // Success: Generate JWT and respond
    val token = jwtService.createAccessToken(...)
    call.respond(HttpStatusCode.OK, RegistrationResponse(...))
} else {
    // Failure: Clean up uploaded files
    FileUploadUtil.deleteFile("nids/$nidFrontPath")
    FileUploadUtil.deleteFile("nids/$nidBackPath") 
    FileUploadUtil.deleteFile("profiles/$photoPath")
    
    call.respond(
        HttpStatusCode.InternalServerError,
        BasicApiResponse<Unit>(
            success = false,
            message = "Registration failed. Please try again."
        )
    )
}
```

## ACID Compliance Benefits

### Atomicity
- All operations succeed or all fail - no partial states
- Prevents orphaned records and inconsistent data

### Consistency  
- Database constraints maintained across all operations
- Referential integrity preserved

### Isolation
- Concurrent registrations don't interfere with each other
- Read committed isolation level prevents dirty reads

### Durability
- Committed transactions are permanently stored
- Majority write concern ensures persistence

## Testing Strategy

### Unit Tests
- Mock `TransactionService` for isolated service testing
- Verify individual repository transaction methods
- Test error scenarios and rollback behavior

### Integration Tests
- Test complete registration flows with simulated failures
- Verify proper cleanup of files and database records
- Test concurrent registration scenarios

### MockTransactionService
```kotlin
class MockTransactionService : TransactionService(null, null) {
    override suspend fun <T> withTransaction(operation: suspend (ClientSession) -> T): T {
        val fakeSession = Proxy.newProxyInstance(...)
        return operation(fakeSession)
    }
}
```

## MongoDB Requirements

### Infrastructure
- **MongoDB 4.0+**: Required for multi-document transactions
- **Replica Set**: Transactions require replica set (even single-node)
- **Write Concern**: Uses majority write concern for durability
- **Read Concern**: Uses local read concern for performance

### Configuration
```yaml
# application.yaml
mongodb:
  uri: "mongodb://localhost:27017/momcare?replicaSet=rs0"
  database: "momcare"
```

## Error Handling and Monitoring

### Error Scenarios
1. **Network failures**: Automatic retry with exponential backoff
2. **Database constraints**: Clear error messages for validation failures  
3. **Transaction conflicts**: Retry logic for write conflicts
4. **File system errors**: Cleanup tracking and retry mechanisms

### Monitoring Considerations
- Transaction success/failure rates
- Transaction duration metrics
- Error logging with full context
- File cleanup operation tracking

## Future Enhancements

### Performance Optimizations
1. **Connection Pooling**: Optimize MongoDB connection management
2. **Bulk Operations**: Batch processing for multiple registrations
3. **Async File Processing**: Move file operations outside transactions
4. **Caching**: Cache frequently accessed data

### Reliability Improvements
1. **Retry Logic**: Exponential backoff for transient failures
2. **Circuit Breakers**: Prevent cascade failures
3. **Event Sourcing**: Audit trail for all operations
4. **Distributed Tracing**: End-to-end request tracking

## Implementation Guidelines

### When to Use Transactions
- Multi-document operations that must be atomic
- Operations involving multiple collections
- Critical business operations (user registration, payments)

### When NOT to Use Transactions
- Single document operations (MongoDB is atomic by default)
- Read-only operations
- Operations that can tolerate eventual consistency

### Best Practices
1. Keep transactions short and focused
2. Avoid external API calls within transactions
3. Use appropriate isolation levels
4. Monitor transaction performance
5. Implement proper error handling and rollback logic
