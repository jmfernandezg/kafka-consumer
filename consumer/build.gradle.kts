plugins {
    kotlin("jvm") version "1.8.0" // Ensure you use a compatible Kotlin version
    id("org.springframework.boot") version "3.3.5"
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.h2database:h2") // Add this line
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Add this line
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}