#!/bin/bash

echo "🌱 Starting MongoDB data seeding..."

# Check if MongoDB is running
if ! pgrep -x "mongod" > /dev/null; then
    echo "❌ MongoDB is not running. Please start MongoDB first."
    exit 1
fi

echo "✅ MongoDB is running"

# Create a temporary main class for seeding
echo "📦 Creating seeding application..."

cat > src/main/kotlin/SeedApp.kt << 'EOF'
package com.evelolvetech

import com.evelolvetech.data.repository.*
import com.evelolvetech.util.*
import com.evelolvetech.di.mainModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() = runBlocking {
    println("🌱 Starting MongoDB data seeding...")
    
    try {
        // Initialize Koin DI container
        val koin = startKoin {
            modules(mainModule)
        }.koin
        
        // Get repositories from DI
        val categoryRepository = koin.get<CategoryRepository>()
        val sellerRepository = koin.get<SellerRepository>()
        val momRepository = koin.get<MomRepository>()
        val doctorRepository = koin.get<DoctorRepository>()
        val nidRepository = koin.get<NidRepository>()
        val userRepository = koin.get<UserRepository>()
        val productRepository = koin.get<ProductRepository>()
        val skuRepository = koin.get<SkuRepository>()
        val skuOfferRepository = koin.get<SkuOfferRepository>()
        val inventoryRepository = koin.get<InventoryRepository>()
        val cartRepository = koin.get<CartRepository>()
        val productRatingRepository = koin.get<ProductRatingRepository>()
        val orderRepository = koin.get<OrderRepository>()
        val paymentRepository = koin.get<PaymentRepository>()
        val hashingService = koin.get<HashingService>()
        
        // Initialize seeder
        val seeder = DataSeeder(
            categoryRepository = categoryRepository,
            sellerRepository = sellerRepository,
            momRepository = momRepository,
            doctorRepository = doctorRepository,
            nidRepository = nidRepository,
            userRepository = userRepository,
            productRepository = productRepository,
            skuRepository = skuRepository,
            skuOfferRepository = skuOfferRepository,
            inventoryRepository = inventoryRepository,
            cartRepository = cartRepository,
            productRatingRepository = productRatingRepository,
            orderRepository = orderRepository,
            paymentRepository = paymentRepository,
            hashingService = hashingService
        )
        
        // Run seeding
        seeder.seedAll()
        
        println("✅ Database seeding completed successfully!")
        
    } catch (e: Exception) {
        println("❌ Failed to seed database: ${e.message}")
        e.printStackTrace()
    }
}
EOF

# Build the project
echo "🔨 Building project..."
./gradlew build -x test

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

# Run the seeding application
echo "🚀 Running data seeding..."
./gradlew runSeed

# Clean up
echo "🧹 Cleaning up..."
rm -f src/main/kotlin/SeedApp.kt

echo "✅ Data seeding process completed!"
