package com.jmfg.consumer.handler

import com.jmfg.consumer.repository.ProductCreatedEventRepository
import com.jmfg.core.RetryableException
import com.jmfg.core.model.Product
import com.jmfg.core.model.ProductCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
@KafkaListener(topics = ["product-created-events-topic"])
class ProductCreatedEventHandler(
    @Autowired private val webClient: WebClient,
    @Autowired private val productCreatedEventRepository: ProductCreatedEventRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaHandler
    fun handle(@Payload event: ProductCreatedEvent) {
        if (productCreatedEventRepository.findById(event.id).isEmpty) {
            processEvent(event)?.let {
                logger.info("Event process success for $it")
                productCreatedEventRepository.save(event)
            }
        } else {
            logger.info("Event already processed: $event")
        }
    }

    private fun processEvent(event: ProductCreatedEvent) =
        webClient
            .get()
            .uri("/products/${event.id}")
            .retrieve()
            .bodyToMono<Product>()
            .doOnSuccess {
                logger.info("Product details fetched: $it, event: $event")
                event.product.apply {
                    name = it.name
                    description = it.description
                }
            }
            .onErrorMap {
                logger.error("Error while fetching product details: ${it.message}, event: $event")
                throw RetryableException("Error while fetching product details")
            }.block()
}
