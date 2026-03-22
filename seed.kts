#!/usr/bin/env kotlin

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Simple seeding script to populate MongoDB
 * This script runs the DataSeeder through the main application
 */

fun main() = runBlocking {
    println("🌱 Starting MongoDB data seeding...")
    
    // Run gradle task to start the application with seeding
    val projectDir = File(System.getProperty("user.dir"))
    val gradlew = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "gradlew.bat"
    } else {
        "./gradlew"
    }
    
    try {
        println("📦 Starting Ktor application with data seeding...")
        
        val process = ProcessBuilder(gradlew, "run", "--args='--seed'")
            .directory(projectDir)
            .inheritIO()
            .start()
        
        // Wait for a moment to let the application start and seed
        Thread.sleep(10000) // 10 seconds should be enough for seeding
        
        // Kill the process
        process.destroyForcibly()
        
        println("✅ Data seeding completed!")
        
    } catch (e: Exception) {
        println("❌ Error during seeding: ${e.message}")
        e.printStackTrace()
    }
}
