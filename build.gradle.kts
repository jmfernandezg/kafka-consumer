plugins {
    kotlin("jvm") version "1.8.0" // Ensure you use a compatible Kotlin version
    kotlin("plugin.spring") version "1.8.0"
    id("io.spring.dependency-management") version "1.1.0" // Ensure you use a compatible version
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
}