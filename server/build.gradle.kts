@file:Suppress("SpellCheckingInspection")

import java.io.File
import java.io.StringWriter
import java.util.Properties

plugins {
    kotlin("jvm")
    kotlin("kapt")
    application
    id("org.jmailen.kotlinter")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("com.palantir.docker") version "0.30.0"
    id("co.uzzu.dotenv.gradle") version "1.2.0"
}
dependencies {
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation(project(":lib"))

    implementation("io.ktor:ktor-server-netty:1.6.5")
    implementation("io.ktor:ktor-client-core:1.6.5")
    implementation("io.ktor:ktor-client-cio:1.6.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
    implementation("com.github.cs125-illinois:ktor-moshi:1.0.3")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("io.github.microutils:kotlin-logging:2.0.11")
    implementation("com.uchuhimo:konf-core:1.1.2")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.3")
    testImplementation("io.kotest:kotest-assertions-ktor:4.4.3")
    testImplementation("io.ktor:ktor-server-test-host:1.6.5")
}
task("createProperties") {
    doLast {
        val properties = Properties().also {
            it["version"] = project.version.toString()
        }
        File(projectDir, "src/main/resources/edu.illinois.cs.cs124.playground.server.version")
            .printWriter().use { printWriter ->
                printWriter.print(
                    StringWriter().also { properties.store(it, null) }.buffer.toString()
                        .lines().drop(1).joinToString(separator = "\n").trim()
                )
            }
    }
}
tasks.processResources {
    dependsOn("createProperties")
}
application {
    @Suppress("DEPRECATION")
    mainClassName = "edu.illinois.cs.cs124.playground.server.MainKt"
}
docker {
    name = "cs124/playground"
    tag("latest", "cs124/playground:latest")
    tag(version.toString(), "cs124/playground:$version")
    files(tasks["shadowJar"].outputs)
}
kapt {
    includeCompileClasspath = false
    javacOptions {
        option("--illegal-access", "permit")
    }
}
kotlin {
    kotlinDaemonJvmArgs = listOf("-Dfile.encoding=UTF-8", "--illegal-access=permit")
}
tasks.test {
    environment["DIRECTORY"] = "/tmp/grading/"
    environment["DOCKER_USER"] = env.fetch("DOCKER_USER", "")
    environment["DOCKER_PASSWORD"] = env.fetch("DOCKER_PASSWORD", "")
}

