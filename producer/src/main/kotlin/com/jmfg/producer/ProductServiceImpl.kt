package com.jmfg.producer

import com.jmfg.core.Product
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.ProductService
import java.util.*
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service

@Service
class ProductServiceImpl(private val kafkaTemplate: KafkaTemplate<String, ProductCreatedEvent>) : ProductService {
    val logger: Logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    override fun createProduct(product: Product): ProductCreatedEvent {

        val productCreatedEvent =
                ProductCreatedEvent(
                        id = UUID.randomUUID().toString(),
                        product = product,
                        createdAt = System.currentTimeMillis()
                )

        val producerRecord =
                org.apache.kafka.clients.producer.ProducerRecord(
                        "product-created-events-topic",
                        productCreatedEvent.id,
                        productCreatedEvent
                )
        producerRecord.headers().add("message-id", UUID.randomUUID().toString().toByteArray())

        val sendResult: SendResult<String, ProductCreatedEvent> =
                kafkaTemplate.send(producerRecord).get()

        logger.info(
                "Sent message with offset: ${sendResult.recordMetadata.offset()}, partition: ${sendResult.recordMetadata.partition()}, topic: ${sendResult.recordMetadata.topic()}"
        )

        return productCreatedEvent
    }
}
