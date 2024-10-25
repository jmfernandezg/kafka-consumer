package com.jmfg.producer.config

import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.ProductCreatedEvent
import com.jmfg.core.model.WithdrawalRequestedEvent
import jakarta.persistence.EntityManagerFactory
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableJpaRepositories(basePackages = ["com.jmfg.producer.repository"])
@EntityScan(basePackages = ["com.jmfg.core.model"])
class KafkaProducerConfiguration {

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

    @Value("\${spring.kafka.producer.properties.linger.ms}")
    private lateinit var linger: String

    @Value("\${spring.kafka.producer.properties.request.timeout.ms}")
    private lateinit var requestTimeoutMs: String

    @Value("\${spring.kafka.producer.properties.enable.idempotence}")
    private lateinit var enableIdempotence: String

    @Value("\${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
    private lateinit var inflightRequests: String

    @Value("\${spring.kafka.producer.transaction-id-prefix}")
    private lateinit var transactionIdPrefix: String

    @Value("\${spring.kafka.topic.product-created-events}")
    private lateinit var productCreatedEventsTopicName: String

    @Value("\${spring.kafka.topic.deposit-money}")
    private lateinit var depositMoneyTopicName: String

    @Value("\${spring.kafka.topic.withdraw-money}")
    private lateinit var withdrawMoneyTopicName: String

    @Value("\${spring.webflux.client.base-url}")
    private lateinit var baseUrl: String

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
    @Bean
    fun productCreatedEventsTopic(): NewTopic {
        return TopicBuilder.name(productCreatedEventsTopicName)
            .partitions(3)
            .replicas(3)
            .configs(mapOf("min.insync.replicas" to "2"))
            .build()
    }

    @Bean
    fun depositMoneyTopic(): NewTopic {
        return TopicBuilder.name(depositMoneyTopicName)
            .partitions(3)
            .replicas(3)
            .configs(mapOf("min.insync.replicas" to "2"))
            .build()
    }

    @Bean
    fun withdrawMoneyTopic(): NewTopic {
        return TopicBuilder.name(withdrawMoneyTopicName)
            .partitions(3)
            .replicas(3)
            .configs(mapOf("min.insync.replicas" to "2"))
            .build()
    }

    @Bean("kafkaTemplateProductCreatedEvent")
    fun kafkaTemplateProductCreatedEvent(): KafkaTemplate<String, ProductCreatedEvent> =
        KafkaTemplate(producerFactory(ProductCreatedEvent::class.java)).apply {
            defaultTopic = productCreatedEventsTopicName
        }

    @Bean("kafkaTemplateDepositMoney")
    fun kafkaTemplateDepositMoney(): KafkaTemplate<String, DepositRequestedEvent> =
        KafkaTemplate(producerFactory(DepositRequestedEvent::class.java)).apply {
            defaultTopic = depositMoneyTopicName
        }

    @Bean("kafkaTemplateWithdrawMoney")
    fun kafkaTemplateWithdrawMoney(): KafkaTemplate<String, WithdrawalRequestedEvent> =
        KafkaTemplate(producerFactory(WithdrawalRequestedEvent::class.java)).apply {
            defaultTopic = withdrawMoneyTopicName
        }

    fun <T> producerFactory(valueSerializerClass: Class<T>): ProducerFactory<String, T> = DefaultKafkaProducerFactory(
        HashMap<String, Any>().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Class.forName(keySerializer))
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Class.forName(valueSerializer))
            put(ProducerConfig.ACKS_CONFIG, acks)
            put(ProducerConfig.LINGER_MS_CONFIG, linger)
            put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs)
            put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence)
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, inflightRequests)
            put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix) // Enable transactions
        }
    )

    @Bean
    fun kafkaTransactionManager(producerFactory: ProducerFactory<String, Any>): KafkaTransactionManager<String, Any> {
        return KafkaTransactionManager(producerFactory)
    }

    @Bean("transactionManager")
    fun jpaTransactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
