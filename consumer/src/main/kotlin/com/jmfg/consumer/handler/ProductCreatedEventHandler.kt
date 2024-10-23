package com.jmfg.consumer.handler

import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.RetryableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Component
@KafkaListener(topics = ["test-topic"])
class ProductCreatedEventHandler(@Autowired private val webClient: WebClient) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaHandler
    fun handle(event: ProductCreatedEvent) {
        logger.info("Handling event: $event")
        val result =
            webClient.get()
                .uri("/products/${event.product.name}")
                .retrieve()
                .bodyToMono(String::class.java)
                .doOnError {
                    logger.error("Error while fetching product details", it)
                    throw RetryableException("Error while fetching product details")
                }
                .block().also {
                    logger.info("Product details: $it")
                }
    }
}
