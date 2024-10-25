package com.jmfg.consumer.handler

import com.jmfg.consumer.db.ProductCreatedEventRepository
import com.jmfg.core.Product
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.RetryableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
@KafkaListener(topics = ["product-created-events-topic"])
class ProductCreatedEventHandler(
    @Autowired private val webClient: WebClient,
    @Autowired private val productCreatedEventRepository: ProductCreatedEventRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @KafkaHandler
    fun handle(@Payload event: ProductCreatedEvent, @Header("message-id") messageId: String) {
        productCreatedEventRepository
            .findById(messageId)
            .ifPresentOrElse(
                {
                    logger.info("Event already processed: $event, messageId: $messageId")
                },
                {
                    logger.info("Event: $event, messageId: $messageId")
                    productCreatedEventRepository.save(event)
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
                            logger.error("Error while fetching product details: ${it.message}, event: $event, messageId: $messageId")
                            RetryableException("Error while fetching product details")
                        }
                        .block()
                    productCreatedEventRepository.save(event)

                }
            )
    }
}
