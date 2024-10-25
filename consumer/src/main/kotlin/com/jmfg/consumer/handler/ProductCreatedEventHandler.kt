package com.jmfg.consumer.handler

import com.jmfg.consumer.repository.ProductCreatedEventRepository
import com.jmfg.core.model.Product
import com.jmfg.core.model.ProductCreatedEvent
import com.jmfg.core.RetryableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
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
    fun handle(@Payload event: ProductCreatedEvent, @Header("message-id") messageId: String) =
        if (isEventAlreadyProcessed(messageId)) {
            logger.info("Event already processed: $event, messageId: $messageId")
        } else {
            processEvent(event, messageId)
        }

    private fun isEventAlreadyProcessed(messageId: String) =
        productCreatedEventRepository.findById(messageId).isPresent

    private fun processEvent(event: ProductCreatedEvent, messageId: String) =
        event.run {
            logger.info("Event: $this, messageId: $messageId")
            saveEvent(this)
            fetchAndUpdateProductDetails(this)
            saveEvent(this)
        }

    private fun saveEvent(event: ProductCreatedEvent) {
        productCreatedEventRepository.save(event)
    }

    private fun fetchAndUpdateProductDetails(event: ProductCreatedEvent) {
        webClient
            .get()
            .uri("/products/${event.id}")
            .retrieve()
            .bodyToMono<Product>()
            .doOnSuccess {
                logger.info("Product details fetched: $it, event: $event")
                event.product?.apply {
                    name = it.name
                    description = it.description
                }
            }
            .onErrorMap {
                logger.error("Error while fetching product details: ${it.message}, event: $event, messageId: ${event.id}")
                throw RetryableException("Error while fetching product details")
            }
            .block()
    }
}
