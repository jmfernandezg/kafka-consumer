package com.jmfg.producer.service

import com.jmfg.core.model.Product
import com.jmfg.core.model.ProductCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = ["product-created"])
class ProductServiceImplIntegrationTest {

    @Autowired
    private lateinit var productServiceImpl: ProductServiceImpl

    @Autowired
    private lateinit var kafkaTemplateProductCreatedEvent: KafkaTemplate<String, ProductCreatedEvent>

    companion object {
        @Autowired
        lateinit var embeddedKafka: EmbeddedKafkaBroker
    }

    @Test
    fun `test createProduct sends ProductCreatedEvent to Kafka`() {
        val product = Product(id = "1", name = "Test Product", price = 100.0)
        val productCreatedEvent = productServiceImpl.createProduct(product)

        val consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka)
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
        val consumerFactory = DefaultKafkaConsumerFactory<String, ByteArray>(consumerProps)
        val consumer = consumerFactory.createConsumer()
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "product-created")

        val records = KafkaTestUtils.getRecords(consumer)
        val record = records.iterator().next()

        assertEquals(productCreatedEvent.id, record.key())
        assertEquals(productCreatedEvent, record.value())
    }
}