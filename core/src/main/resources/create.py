import os

def create_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as file:
        file.write(content)

project_root = "kafka-multi-module-project"

# Create settings.gradle.kts
create_file(os.path.join(project_root, "settings.gradle.kts"), """
rootProject.name = "kafka-multi-module-project"
include("core", "consumer", "producer")
""".strip())

# Create root build.gradle.kts
create_file(os.path.join(project_root, "build.gradle.kts"), """
plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
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
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
""".strip())

# Create core module
create_file(os.path.join(project_root, "core/build.gradle.kts"), """
plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
""".strip())

create_file(os.path.join(project_root, "core/src/main/kotlin/com/jmfg/core/Product.kt"), """
package com.jmfg.core

data class Product(val id: String, val name: String)
""".strip())

# Create consumer module
create_file(os.path.join(project_root, "consumer/build.gradle.kts"), """
plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
""".strip())

create_file(os.path.join(project_root, "consumer/src/main/kotlin/com/jmfg/consumer/KafkaConsumerApplication.kt"), """
package com.jmfg.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaConsumerApplication

fun main(args: Array<String>) {
    runApplication<KafkaConsumerApplication>(*args)
}
""".strip())

# Create producer module
create_file(os.path.join(project_root, "producer/build.gradle.kts"), """
plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
""".strip())

create_file(os.path.join(project_root, "producer/src/main/kotlin/com/jmfg/producer/KafkaProducerApplication.kt"), """
package com.jmfg.producer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaProducerApplication

fun main(args: Array<String>) {
    runApplication<KafkaProducerApplication>(*args)
}
""".strip())

create_file(os.path.join(project_root, "producer/src/main/kotlin/com/jmfg/producer/KafkaConfig.kt"), """
package com.jmfg.producer

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig {

    @Value("\\${spring.kafka.producer.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\\${spring.kafka.producer.key-serializer}")
    private lateinit var keySerializer: String

    @Value("\\${spring.kafka.producer.value-serializer}")
    private lateinit var valueSerializer: String

    @Value("\\${spring.kafka.producer.acks}")
    private lateinit var acks: String

    @Value("\\${spring.kafka.producer.properties.delivery.timeout.ms}")
    private lateinit var deliveryTimeoutMs: String

    @Value("\\${spring.kafka.producer.properties.request.timeout.ms}")
    private lateinit var requestTimeoutMs: String

    @Value("\\${spring.kafka.producer.properties.enable.idempotence}")
    private lateinit var enableIdempotence: String

    @Bean
    fun createTopic(): NewTopic {
        return TopicBuilder.name("test-topic")
            .partitions(3)
            .replicas(3)
            .configs(mapOf("min.insync.replicas" to "2"))
            .build()
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, ProductCreatedEvent> = KafkaTemplate(producerFactory())

    @Bean
    fun producerFactory(): ProducerFactory<String, ProductCreatedEvent> = DefaultKafkaProducerFactory(
        HashMap<String, Any>().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Class.forName(keySerializer))
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Class.forName(valueSerializer))
            put(ProducerConfig.ACKS_CONFIG, acks)
            put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs)
            put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence)
        }
    )
}
""".strip())