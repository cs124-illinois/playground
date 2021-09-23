import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31" apply false
    kotlin("kapt") version "1.5.31" apply false
    id("org.jmailen.kotlinter") version "3.6.0" apply false
    id("com.github.ben-manes.versions") version "0.39.0"
}
subprojects {
    group = "com.github.cs124-illinois.playground"
    version = "2021.9.0"
    tasks.withType<KotlinCompile> {
        val javaVersion = JavaVersion.VERSION_16.toString()
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        kotlinOptions {
            jvmTarget = javaVersion
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-ea", "-Xmx1G", "-Xss256k", "--illegal-access=permit")
    }
}
allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://dl.bintray.com/jetbrains/markdown")
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
