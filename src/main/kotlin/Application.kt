package com.evelolvetech

import com.evelolvetech.di.mainModule
import com.evelolvetech.util.StartupSeeder
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        modules(mainModule)
    }
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()

    // Create upload directories
    com.evelolvetech.util.FileUploadUtil.init()

    log.info("MomCare Platform starting on port ${System.getenv("PORT") ?: "8080"}")

    // Seed in background so server can bind port immediately
    launch {
        try {
            StartupSeeder().seedIfEmpty()
        } catch (e: Exception) {
            log.warn("Startup seed skipped: ${e.message}")
        }
    }
}
