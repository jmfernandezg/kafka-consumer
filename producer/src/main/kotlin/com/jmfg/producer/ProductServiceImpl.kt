package com.jmfg.producer

import com.jmfg.core.Product
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.ProductService
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class ProductServiceImpl(private val kafkaTemplate: KafkaTemplate<String, ProductCreatedEvent>) : ProductService {
    val logger: Logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    override fun createProduct(product: Product) : ProductCreatedEvent{

        val productCreatedEvent = ProductCreatedEvent(
            id = UUID.randomUUID().toString(),
            product = product,
            createdAt = System.currentTimeMillis()
        )

        CompletableFuture.runAsync {
            kafkaTemplate.send("test-topic", productCreatedEvent)
        }.whenComplete { t, u ->
            if (u != null) {
                logger.error("Error sending message", u)
            } else {
                logger.info("Message sent")
            }
        }.join()

        return productCreatedEvent
    }
}