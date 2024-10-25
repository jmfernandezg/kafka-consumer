package com.jmfg.producer

import com.jmfg.core.Product
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.ProductService
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductServiceImpl(private val kafkaTemplate: KafkaTemplate<String, ProductCreatedEvent>) :
    ProductService {
    val logger: Logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun createProduct(product: Product): ProductCreatedEvent {
        return ProductCreatedEvent(id = product.id, product = product).apply {
            ProducerRecord(
                "product-created-events-topic",
                id,
                this
            ).run {
                headers().add("message-id", id.toByteArray())
                kafkaTemplate.send(this).get()
                    .also { sendResult ->
                        logger.info(
                            "Sent message with offset: ${sendResult.recordMetadata.offset()}, partition: ${sendResult.recordMetadata.partition()}, topic: ${sendResult.recordMetadata.topic()}"
                        )
                    }
            }
        }
    }
}
