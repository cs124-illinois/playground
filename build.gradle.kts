import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0" apply false
    kotlin("plugin.serialization") version "1.8.0" apply false
    id("org.jmailen.kotlinter") version "3.13.0" apply false
    id("com.github.ben-manes.versions") version "0.44.0"
    id("co.uzzu.dotenv.gradle") version "2.0.0"
}
subprojects {
    group = "com.github.cs124-illinois.playground"
    version = "2023.1.0"
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-ea", "-Xmx1G", "-Xss256k")
    }
}
allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
    tasks.withType<Test> {
        enableAssertions = true
    }
}
tasks.dependencyUpdates {
    fun String.isNonStable() = !(
        listOf("RELEASE", "FINAL", "GA", "JRE").any { toUpperCase().contains(it) }
            || "^[0-9,.v-]+(-r)?$".toRegex().matches(this)
        )
    rejectVersionIf { candidate.version.isNonStable() }
    gradleReleaseChannel = "current"
}
