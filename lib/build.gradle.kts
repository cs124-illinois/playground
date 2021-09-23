plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
    id("org.jmailen.kotlinter")
}
dependencies {
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
    implementation("org.zeroturnaround:zt-process-killer:1.10")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    testImplementation("io.kotest:kotest-runner-junit5:4.6.3")
}
tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
    artifacts {
        add("archives", sourcesJar)
    }
}
publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components["java"])
        }
    }
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
