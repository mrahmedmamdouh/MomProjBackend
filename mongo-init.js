db = db.getSiblingDB('momcare');

db.createCollection('users');
db.createCollection('moms');
db.createCollection('doctors');
db.createCollection('categories');
db.createCollection('products');
db.createCollection('sku_offers');
db.createCollection('carts');
db.createCollection('orders');
db.createCollection('refresh_tokens');

db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "authUid": 1 }, { unique: true });

db.moms.createIndex({ "email": 1 }, { unique: true });
db.moms.createIndex({ "authUid": 1 }, { unique: true });
db.moms.createIndex({ "isAuthorized": 1 });

db.doctors.createIndex({ "email": 1 }, { unique: true });
db.doctors.createIndex({ "authUid": 1 }, { unique: true });
db.doctors.createIndex({ "isAuthorized": 1 });
db.doctors.createIndex({ "specialization": 1 });

db.categories.createIndex({ "name": 1 }, { unique: true });
db.categories.createIndex({ "slug": 1 }, { unique: true });

db.products.createIndex({ "name": 1 });
db.products.createIndex({ "slug": 1 }, { unique: true });
db.products.createIndex({ "categoryIds": 1 });
db.products.createIndex({ "defaultSellerId": 1 });

db.sku_offers.createIndex({ "skuId": 1 });
db.sku_offers.createIndex({ "sellerId": 1 });
db.sku_offers.createIndex({ "isActive": 1 });

db.carts.createIndex({ "momId": 1 }, { unique: true });

db.orders.createIndex({ "momId": 1 });
db.orders.createIndex({ "status": 1 });
db.orders.createIndex({ "createdAt": -1 });

db.refresh_tokens.createIndex({ "token": 1 }, { unique: true });
db.refresh_tokens.createIndex({ "userId": 1 });
db.refresh_tokens.createIndex({ "expiresAt": 1 }, { expireAfterSeconds: 0 });

db.categories.insertMany([
  {
    _id: "cat_fitness",
    name: "Fitness & Wellness",
    slug: "fitness-wellness",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: "cat_nutrition",
    name: "Nutrition & Supplements",
    slug: "nutrition-supplements",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: "cat_baby",
    name: "Baby Care",
    slug: "baby-care",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: "cat_mom",
    name: "Mom Care",
    slug: "mom-care",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: "cat_health",
    name: "Health & Medical",
    slug: "health-medical",
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print('✅ Mom Care Platform database initialized successfully!');
print('📊 Collections created: users, moms, doctors, categories, products, sku_offers, carts, orders, refresh_tokens');
print('🔍 Indexes created for optimal performance');
print('📦 Initial categories inserted');
