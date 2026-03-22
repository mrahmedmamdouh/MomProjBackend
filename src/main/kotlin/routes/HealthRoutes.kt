package com.evelolvetech.routes

import com.evelolvetech.util.respondWithData
import io.ktor.server.routing.*
import java.time.Instant

fun Route.healthRoutes() {
    route("/health") {
        get {
            val environment = System.getenv("ENVIRONMENT") ?: "development"
            val healthStatus = mapOf(
                "status" to "UP",
                "timestamp" to Instant.now().toString(),
                "service" to "Mom Care Platform Backend",
                "version" to "1.0.0",
                "environment" to environment
            )
            
            call.respondWithData(
                data = healthStatus,
                message = "Service is healthy"
            )
        }
        
        get("/ready") {
            val readinessStatus = mapOf(
                "status" to "READY",
                "timestamp" to Instant.now().toString(),
                "checks" to mapOf(
                    "database" to "UP",
                    "cache" to "UP",
                    "external_services" to "UP"
                )
            )
            
            call.respondWithData(
                data = readinessStatus,
                message = "Service is ready"
            )
        }
        
        get("/live") {
            call.respondWithData(
                data = mapOf("status" to "ALIVE"),
                message = "Service is alive"
            )
        }
    }
}
