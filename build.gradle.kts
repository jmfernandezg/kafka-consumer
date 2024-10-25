plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "com.jmfg"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.0")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.0")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    ktlint {
        version.set("0.45.2") // Specify the ktlint version
        android.set(false) // Set to true if you are using Android
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)
        additionalEditorconfigFile.set(file(".editorconfig"))
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
        disabledRules.set(setOf("no-wildcard-imports")) // Allow wildcard imports
    }
}
