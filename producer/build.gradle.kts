plugins {
    kotlin("jvm") version "1.8.0" // Ensure you use a compatible Kotlin version
    id("org.springframework.boot") version "3.3.5"
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
