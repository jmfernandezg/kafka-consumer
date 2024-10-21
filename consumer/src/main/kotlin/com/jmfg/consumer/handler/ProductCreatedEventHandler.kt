package com.jmfg.consumer.handler

import com.jmfg.core.ProductCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@KafkaListener(topics = ["test-topic"])
class ProductCreatedEventHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaHandler
    fun handle(event: ProductCreatedEvent) {
        logger.info("Handling event: $event")
    }
}
