# 🗄️ Database Migration Strategy for Microservices

## 📊 Current Database Structure

### **Single MongoDB Database: `momcare_db`**
```javascript
// Current Collections
momcare_db.users
momcare_db.refresh_tokens
momcare_db.moms
momcare_db.doctors
momcare_db.nids
momcare_db.categories
momcare_db.products
momcare_db.skus
momcare_db.sku_offers
momcare_db.sellers
momcare_db.carts
momcare_db.orders
momcare_db.payments
momcare_db.product_ratings
momcare_db.inventory
```

## 🎯 Target Database Architecture

### **Database per Service Pattern**

#### **1. Authentication Service Database: `auth_db`**
```javascript
auth_db.users
auth_db.refresh_tokens
auth_db.auth_sessions
auth_db.login_attempts
```

#### **2. User Management Service Database: `user_db`**
```javascript
user_db.moms
user_db.doctors
user_db.nids
user_db.user_profiles
user_db.profile_photos
```

#### **3. E-commerce Service Database: `ecommerce_db`**
```javascript
ecommerce_db.categories
ecommerce_db.products
ecommerce_db.skus
ecommerce_db.sku_offers
ecommerce_db.sellers
ecommerce_db.product_ratings
```

#### **4. Shopping Service Database: `shopping_db`**
```javascript
shopping_db.carts
shopping_db.orders
shopping_db.order_items
shopping_db.order_status_history
```

#### **5. Payment Service Database: `payment_db`**
```javascript
payment_db.payments
payment_db.transactions
payment_db.payment_methods
payment_db.refunds
```

#### **6. File Service Database: `file_db`**
```javascript
file_db.file_metadata
file_db.upload_sessions
file_db.file_access_logs
```

#### **7. Admin Service Database: `admin_db`**
```javascript
admin_db.admin_operations
admin_db.audit_logs
admin_db.system_config
admin_db.user_authorizations
```

#### **8. Analytics Service Database: `analytics_db`**
```javascript
analytics_db.user_analytics
analytics_db.ecommerce_metrics
analytics_db.system_metrics
analytics_db.performance_logs
```

## 🔄 Migration Strategy

### **Phase 1: Database Creation & Setup**

#### **1.1 Create Separate MongoDB Databases**
```bash
# Create databases for each service
mongo --eval "db = db.getSiblingDB('auth_db'); db.createCollection('users');"
mongo --eval "db = db.getSiblingDB('user_db'); db.createCollection('moms');"
mongo --eval "db = db.getSiblingDB('ecommerce_db'); db.createCollection('products');"
mongo --eval "db = db.getSiblingDB('shopping_db'); db.createCollection('orders');"
mongo --eval "db = db.getSiblingDB('payment_db'); db.createCollection('payments');"
mongo --eval "db = db.getSiblingDB('file_db'); db.createCollection('file_metadata');"
mongo --eval "db = db.getSiblingDB('admin_db'); db.createCollection('admin_operations');"
mongo --eval "db = db.getSiblingDB('analytics_db'); db.createCollection('user_analytics');"
```

#### **1.2 Set Up Database Users & Permissions**
```javascript
// Authentication Service User
use auth_db
db.createUser({
  user: "auth_service_user",
  pwd: "auth_service_password",
  roles: [
    { role: "readWrite", db: "auth_db" }
  ]
})

// User Management Service User
use user_db
db.createUser({
  user: "user_service_user",
  pwd: "user_service_password",
  roles: [
    { role: "readWrite", db: "user_db" }
  ]
})

// E-commerce Service User
use ecommerce_db
db.createUser({
  user: "ecommerce_service_user",
  pwd: "ecommerce_service_password",
  roles: [
    { role: "readWrite", db: "ecommerce_db" }
  ]
})

// Shopping Service User
use shopping_db
db.createUser({
  user: "shopping_service_user",
  pwd: "shopping_service_password",
  roles: [
    { role: "readWrite", db: "shopping_db" }
  ]
})

// Payment Service User
use payment_db
db.createUser({
  user: "payment_service_user",
  pwd: "payment_service_password",
  roles: [
    { role: "readWrite", db: "payment_db" }
  ]
})

// File Service User
use file_db
db.createUser({
  user: "file_service_user",
  pwd: "file_service_password",
  roles: [
    { role: "readWrite", db: "file_db" }
  ]
})

// Admin Service User
use admin_db
db.createUser({
  user: "admin_service_user",
  pwd: "admin_service_password",
  roles: [
    { role: "readWrite", db: "admin_db" }
  ]
})

// Analytics Service User
use analytics_db
db.createUser({
  user: "analytics_service_user",
  pwd: "analytics_service_password",
  roles: [
    { role: "readWrite", db: "analytics_db" }
  ]
})
```

### **Phase 2: Data Migration Scripts**

#### **2.1 Authentication Service Migration**
```javascript
// migrate_auth_data.js
use momcare_db;

// Migrate users collection
db.users.find().forEach(function(doc) {
  db.getSiblingDB('auth_db').users.insertOne(doc);
});

// Migrate refresh_tokens collection
db.refresh_tokens.find().forEach(function(doc) {
  db.getSiblingDB('auth_db').refresh_tokens.insertOne(doc);
});

print("Authentication data migrated successfully");
```

#### **2.2 User Management Service Migration**
```javascript
// migrate_user_data.js
use momcare_db;

// Migrate moms collection
db.moms.find().forEach(function(doc) {
  db.getSiblingDB('user_db').moms.insertOne(doc);
});

// Migrate doctors collection
db.doctors.find().forEach(function(doc) {
  db.getSiblingDB('user_db').doctors.insertOne(doc);
});

// Migrate nids collection
db.nids.find().forEach(function(doc) {
  db.getSiblingDB('user_db').nids.insertOne(doc);
});

print("User management data migrated successfully");
```

#### **2.3 E-commerce Service Migration**
```javascript
// migrate_ecommerce_data.js
use momcare_db;

// Migrate categories collection
db.categories.find().forEach(function(doc) {
  db.getSiblingDB('ecommerce_db').categories.insertOne(doc);
});

// Migrate products collection
db.products.find().forEach(function(doc) {
  db.getSiblingDB('ecommerce_db').products.insertOne(doc);
});

// Migrate skus collection
db.skus.find().forEach(function(doc) {
  db.getSiblingDB('ecommerce_db').skus.insertOne(doc);
});

// Migrate sku_offers collection
db.sku_offers.find().forEach(function(doc) {
  db.getSiblingDB('ecommerce_db').sku_offers.insertOne(doc);
});

// Migrate sellers collection
db.sellers.find().forEach(function(doc) {
  db.getSiblingDB('ecommerce_db').sellers.insertOne(doc);
});

// Migrate product_ratings collection
db.product_ratings.find().forEach(function(doc) {
  db.getSiblingDB('ecommerce_db').product_ratings.insertOne(doc);
});

print("E-commerce data migrated successfully");
```

#### **2.4 Shopping Service Migration**
```javascript
// migrate_shopping_data.js
use momcare_db;

// Migrate carts collection
db.carts.find().forEach(function(doc) {
  db.getSiblingDB('shopping_db').carts.insertOne(doc);
});

// Migrate orders collection
db.orders.find().forEach(function(doc) {
  db.getSiblingDB('shopping_db').orders.insertOne(doc);
});

print("Shopping data migrated successfully");
```

#### **2.5 Payment Service Migration**
```javascript
// migrate_payment_data.js
use momcare_db;

// Migrate payments collection
db.payments.find().forEach(function(doc) {
  db.getSiblingDB('payment_db').payments.insertOne(doc);
});

print("Payment data migrated successfully");
```

### **Phase 3: Data Validation & Integrity**

#### **3.1 Data Count Validation**
```javascript
// validate_migration.js
use momcare_db;

print("=== Original Data Counts ===");
print("Users: " + db.users.count());
print("Moms: " + db.moms.count());
print("Doctors: " + db.doctors.count());
print("Products: " + db.products.count());
print("Orders: " + db.orders.count());

print("\n=== Migrated Data Counts ===");
print("Auth Users: " + db.getSiblingDB('auth_db').users.count());
print("User Moms: " + db.getSiblingDB('user_db').moms.count());
print("User Doctors: " + db.getSiblingDB('user_db').doctors.count());
print("Ecommerce Products: " + db.getSiblingDB('ecommerce_db').products.count());
print("Shopping Orders: " + db.getSiblingDB('shopping_db').orders.count());
```

#### **3.2 Data Integrity Checks**
```javascript
// integrity_check.js
use momcare_db;

// Check for orphaned references
print("=== Checking for Orphaned References ===");

// Check if all mom references in orders exist in moms collection
var orphanedOrders = db.orders.find({
  momId: { $nin: db.moms.distinct("id") }
}).count();
print("Orphaned orders: " + orphanedOrders);

// Check if all product references in orders exist in products collection
var orphanedOrderItems = db.orders.aggregate([
  { $unwind: "$items" },
  { $match: { "items.productId": { $nin: db.products.distinct("id") } } },
  { $count: "orphaned_items" }
]).toArray();
print("Orphaned order items: " + (orphanedOrderItems[0] ? orphanedOrderItems[0].orphaned_items : 0));
```

### **Phase 4: Cross-Service Data Access**

#### **4.1 Data Synchronization Strategy**

##### **Synchronous Data Access (API Calls)**
```kotlin
// Example: Order Service needs user data
class OrderService(
    private val orderRepository: OrderRepository,
    private val userServiceClient: UserServiceClient
) {
    suspend fun createOrder(orderRequest: CreateOrderRequest): Order {
        // Get user data from User Service
        val user = userServiceClient.getUser(orderRequest.momId)
        
        // Create order with user data
        val order = Order(
            momId = orderRequest.momId,
            momName = user.fullName,
            momEmail = user.email,
            // ... other fields
        )
        
        return orderRepository.createOrder(order)
    }
}
```

##### **Asynchronous Data Synchronization (Events)**
```kotlin
// Example: User profile update event
data class UserProfileUpdatedEvent(
    val userId: String,
    val fullName: String,
    val email: String,
    val timestamp: Long
)

// Order Service listens to user updates
@EventListener
class OrderServiceEventListener {
    suspend fun handleUserProfileUpdate(event: UserProfileUpdatedEvent) {
        // Update cached user data or denormalized data
        orderRepository.updateUserInfo(event.userId, event.fullName, event.email)
    }
}
```

#### **4.2 Caching Strategy**

##### **Redis Cache for Cross-Service Data**
```kotlin
// User data cache in Order Service
class CachedUserService(
    private val userServiceClient: UserServiceClient,
    private val redisClient: RedisClient
) {
    suspend fun getUser(userId: String): User {
        // Try cache first
        val cachedUser = redisClient.get("user:$userId")
        if (cachedUser != null) {
            return Gson().fromJson(cachedUser, User::class.java)
        }
        
        // Fetch from User Service
        val user = userServiceClient.getUser(userId)
        
        // Cache for 1 hour
        redisClient.setex("user:$userId", 3600, Gson().toJson(user))
        
        return user
    }
}
```

### **Phase 5: Rollback Strategy**

#### **5.1 Database Rollback Plan**
```bash
#!/bin/bash
# rollback_databases.sh

echo "Rolling back to monolithic database..."

# Stop all microservices
kubectl scale deployment auth-service --replicas=0
kubectl scale deployment user-service --replicas=0
kubectl scale deployment ecommerce-service --replicas=0
kubectl scale deployment shopping-service --replicas=0
kubectl scale deployment payment-service --replicas=0

# Restore original database
mongorestore --db momcare_db --drop /backup/momcare_db_backup/

# Start monolithic application
kubectl scale deployment momcare-monolith --replicas=3

echo "Rollback completed successfully"
```

#### **5.2 Data Consistency Rollback**
```javascript
// rollback_data_consistency.js
use momcare_db;

// Merge data back from microservices
print("Merging data back to monolithic database...");

// Merge users from auth_db
db.getSiblingDB('auth_db').users.find().forEach(function(doc) {
  db.users.replaceOne({id: doc.id}, doc, {upsert: true});
});

// Merge moms from user_db
db.getSiblingDB('user_db').moms.find().forEach(function(doc) {
  db.moms.replaceOne({id: doc.id}, doc, {upsert: true});
});

// Merge orders from shopping_db
db.getSiblingDB('shopping_db').orders.find().forEach(function(doc) {
  db.orders.replaceOne({id: doc.id}, doc, {upsert: true});
});

print("Data consistency rollback completed");
```

## 🔧 **Migration Tools & Scripts**

### **Automated Migration Script**
```bash
#!/bin/bash
# migrate_to_microservices.sh

set -e

echo "Starting microservices database migration..."

# Create databases
echo "Creating databases..."
mongo --eval "db = db.getSiblingDB('auth_db'); db.createCollection('users');"
mongo --eval "db = db.getSiblingDB('user_db'); db.createCollection('moms');"
mongo --eval "db = db.getSiblingDB('ecommerce_db'); db.createCollection('products');"
mongo --eval "db = db.getSiblingDB('shopping_db'); db.createCollection('orders');"
mongo --eval "db = db.getSiblingDB('payment_db'); db.createCollection('payments');"

# Run migration scripts
echo "Migrating data..."
mongo momcare_db migrate_auth_data.js
mongo momcare_db migrate_user_data.js
mongo momcare_db migrate_ecommerce_data.js
mongo momcare_db migrate_shopping_data.js
mongo momcare_db migrate_payment_data.js

# Validate migration
echo "Validating migration..."
mongo momcare_db validate_migration.js

# Run integrity checks
echo "Running integrity checks..."
mongo momcare_db integrity_check.js

echo "Migration completed successfully!"
```

### **Data Backup Script**
```bash
#!/bin/bash
# backup_before_migration.sh

BACKUP_DIR="/backup/$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR

echo "Creating backup in $BACKUP_DIR..."

# Backup original database
mongodump --db momcare_db --out $BACKUP_DIR

# Backup configuration files
cp -r src/main/resources $BACKUP_DIR/
cp build.gradle.kts $BACKUP_DIR/
cp docker-compose.yml $BACKUP_DIR/

echo "Backup completed: $BACKUP_DIR"
```

## 📊 **Monitoring & Validation**

### **Migration Progress Tracking**
```javascript
// migration_progress.js
use momcare_db;

// Track migration progress
db.migration_progress.insertOne({
  phase: "database_creation",
  status: "completed",
  timestamp: new Date(),
  details: "All 8 databases created successfully"
});

db.migration_progress.insertOne({
  phase: "data_migration",
  status: "in_progress",
  timestamp: new Date(),
  details: "Migrating authentication data..."
});
```

### **Data Quality Metrics**
```javascript
// data_quality_metrics.js
use momcare_db;

// Calculate data quality metrics
var totalRecords = db.users.count() + db.moms.count() + db.products.count() + db.orders.count();
var migratedRecords = db.getSiblingDB('auth_db').users.count() + 
                     db.getSiblingDB('user_db').moms.count() + 
                     db.getSiblingDB('ecommerce_db').products.count() + 
                     db.getSiblingDB('shopping_db').orders.count();

var migrationPercentage = (migratedRecords / totalRecords) * 100;

print("Migration Progress: " + migrationPercentage.toFixed(2) + "%");
print("Total Records: " + totalRecords);
print("Migrated Records: " + migratedRecords);
```

## ⚠️ **Migration Risks & Mitigation**

### **1. Data Loss Risk**
**Risk**: Data corruption or loss during migration
**Mitigation**: 
- Complete database backup before migration
- Test migration on staging environment
- Validate data integrity after each phase
- Keep original database until migration is verified

### **2. Service Downtime**
**Risk**: Extended downtime during migration
**Mitigation**:
- Blue-green deployment strategy
- Gradual migration with service-by-service approach
- Maintain read-only access during migration
- Rollback plan ready

### **3. Data Consistency Issues**
**Risk**: Inconsistent data across services
**Mitigation**:
- Implement eventual consistency patterns
- Use event-driven synchronization
- Add data validation and reconciliation jobs
- Monitor data consistency continuously

### **4. Performance Impact**
**Risk**: Performance degradation during migration
**Mitigation**:
- Load testing before migration
- Gradual traffic shifting
- Monitor performance metrics
- Scale services as needed

## 🎯 **Success Criteria**

### **Migration Success Metrics**
- [ ] **100% data migration** without loss
- [ ] **Zero downtime** during migration
- [ ] **All services operational** after migration
- [ ] **Performance maintained** or improved
- [ ] **Data integrity verified** across all services
- [ ] **Rollback plan tested** and ready

### **Post-Migration Validation**
- [ ] **All APIs functional** in microservices
- [ ] **Cross-service communication** working
- [ ] **Data synchronization** operational
- [ ] **Monitoring and alerting** active
- [ ] **Performance benchmarks** met
- [ ] **Security measures** in place

---

**Note**: This migration should be performed during low-traffic periods with a complete rollback plan ready. Each phase should be thoroughly tested before proceeding to the next phase.
