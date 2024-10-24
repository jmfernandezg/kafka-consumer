package com.jmfg.producer

import com.jmfg.core.Product
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.ProductService
import java.util.*
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.apache.kafka.clients.producer.ProducerRecord

@Service
class ProductServiceImpl(private val kafkaTemplate: KafkaTemplate<String, ProductCreatedEvent>) :
        ProductService {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

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
