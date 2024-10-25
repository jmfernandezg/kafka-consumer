plugins {
    kotlin("jvm") version "1.8.0" // Ensure you use a compatible Kotlin version
    id("org.springframework.boot") version "3.3.5"
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("net.datafaker:datafaker:2.4.0")
}
