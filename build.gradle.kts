plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("checkstyle")
    id("jacoco")
    alias(libs.plugins.dependencycheck)
}

group = "com.evelolvetech"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.mongodb.driver.core)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.bson)
    implementation(libs.kmongo)
    implementation(libs.kmongo.coroutine)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockk)
}

tasks.register<JavaExec>("runSeed") {
    group = "application"
    description = "Run database seeding"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.evelolvetech.scripts.SeedDatabaseKt"
}

tasks.register<Exec>("health-check-all") {
    group = "verification"
    description = "Run comprehensive health check for all project systems"
    commandLine("bash", "-c", "./health-check.sh")
    dependsOn("build")
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("checkstyle.xml")
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    ignoreFailures = false
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

dependencyCheck {
    failBuildOnCVSS = 10.0f
    outputDirectory = "build/reports/dependency-check"
    suppressionFile = "dependency-check-suppressions.xml"
    format = "ALL"
    autoUpdate = false
    skip = false
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
