package com.evelolvetech

import com.evelolvetech.di.mainModule
import com.evelolvetech.util.StartupSeeder
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

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

    // Auto-seed on first startup (checks if DB is empty)
    try {
        StartupSeeder().seedIfEmpty()
    } catch (e: Exception) {
        log.warn("Startup seed skipped: ${e.message}")
    }
}
