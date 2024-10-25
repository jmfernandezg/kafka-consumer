package com.jmfg.consumer.config

import com.jmfg.core.NonRetryableException
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.RetryableException
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableJpaRepositories(basePackages = ["com.jmfg.core", "com.jmfg.consumer.repository"])
@EntityScan(basePackages = ["com.jmfg.core", "com.jmfg.consumer.data"])
class KafkaConsumerConfiguration {
    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Value("\${spring.kafka.consumer.key-deserializer}")
    private lateinit var keyDeserializer: String

    @Value("\${spring.kafka.consumer.value-deserializer}")
    private lateinit var valueDeserializer: String

    // JSon trusted packages
    @Value("\${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private lateinit var trustedPackages: String

    @Value("\${spring.kafka.consumer.properties.spring.deserializer.key.delegate.class}")
    private lateinit var keyDelegateClass: String

    @Value("\${spring.kafka.consumer.properties.spring.deserializer.value.delegate.class}")
    private lateinit var valueDelegateClass: String

    @Value("\${spring.kafka.producer.key-serializer}")
    private lateinit var keySerializer: String

    @Value("\${spring.kafka.producer.value-serializer}")
    private lateinit var valueSerializer: String

    @Value("\${spring.webflux.client.base-url}")
    private lateinit var baseUrl: String

    @Value("\${spring.kafka.consumer.isolation-level}")
    private lateinit var isolationLevel: String

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> = DefaultKafkaConsumerFactory(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to Class.forName(keyDeserializer),
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to Class.forName(valueDeserializer),
            JsonDeserializer.TRUSTED_PACKAGES to trustedPackages,
            "spring.deserializer.key.delegate.class" to keyDelegateClass,
            "spring.deserializer.value.delegate.class" to valueDelegateClass,
            ConsumerConfig.ISOLATION_LEVEL_CONFIG to isolationLevel
        )
    )

    @Bean
    fun kafkaListenerContainerFactory() =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            val errorHandler = DefaultErrorHandler(DeadLetterPublishingRecoverer(kafkaTemplate()))
            errorHandler.addNotRetryableExceptions(NonRetryableException::class.java)
            errorHandler.addRetryableExceptions(RetryableException::class.java)
            setCommonErrorHandler(errorHandler)
            consumerFactory = consumerFactory()
        }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, ProductCreatedEvent> = KafkaTemplate(producerFactory())

    @Bean
    fun producerFactory(): ProducerFactory<String, ProductCreatedEvent> = DefaultKafkaProducerFactory(
        HashMap<String, Any>().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Class.forName(keySerializer))
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Class.forName(valueSerializer))
        }
    )

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}
