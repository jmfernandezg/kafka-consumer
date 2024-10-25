package com.jmfg.consumer.handler

import com.jmfg.consumer.db.ProductCreatedEventRepository
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
                { logger.info("Event already processed: $event, messageId: $messageId") },
                {
                    webClient
                        .get()
                        .uri("/products/${event.id}")
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .doOnError {
                            logger.error(
                                "Error while fetching product details: ${it.message}, event: $event, messageId: $messageId"
                            )
                            throw RetryableException(
                                "Error while fetching product details"
                            )
                        }
                        .block()
                        .also {
                            logger.info(
                                "Product details: $it, event: $event, messageId: $messageId"
                            )
                            productCreatedEventRepository.save(event)
                        }
                }
            )
    }
}
