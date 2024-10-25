package com.jmfg.producer

import com.jmfg.core.ProductCreatedEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager

@Configuration
class KafkaProducerConfig {

    @Value("\${spring.kafka.producer.bootstrap-servers}")
    private lateinit var bootstrapServers: List<String>

    @Value("\${spring.kafka.producer.key-serializer}")
    private lateinit var keySerializer: String

    @Value("\${spring.kafka.producer.value-serializer}")
    private lateinit var valueSerializer: String

    @Value("\${spring.kafka.producer.acks}")
    private lateinit var acks: String

    @Value("\${spring.kafka.producer.properties.delivery.timeout.ms}")
    private lateinit var deliveryTimeoutMs: String

    @Value("\${spring.kafka.producer.properties.request.timeout.ms}")
    private lateinit var requestTimeoutMs: String

    @Value("\${spring.kafka.producer.properties.enable.idempotence}")
    private lateinit var enableIdempotence: String

    @Value("\${spring.kafka.producer.transaction-id-prefix}")
    private lateinit var transactionIdPrefix: String

    @Bean
    fun createTopic(): NewTopic {
        return TopicBuilder.name("product-created-events-topic")
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
            put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix) // Enable transactions
        }
    )

    @Bean
    fun kafkaTransactionManager(producerFactory: ProducerFactory<String, ProductCreatedEvent>): KafkaTransactionManager<String, ProductCreatedEvent> {
        return KafkaTransactionManager(producerFactory)
    }
}