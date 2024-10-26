package com.jmfg.producer.service

import com.jmfg.core.NonRetryableException
import com.jmfg.core.RetryableException
import com.jmfg.core.model.Product
import com.jmfg.core.model.ProductCreatedEvent
import com.jmfg.core.service.ProductService
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient

@Service
class ProductServiceImpl(
    @Autowired private val webClient: WebClient,
    private val kafkaTemplateProductCreatedEvent: KafkaTemplate<String, ProductCreatedEvent>
) :
    ProductService {
    val logger: Logger = getLogger(this::class.java)

    @Transactional(
        transactionManager = "kafkaTransactionManagerProductCreatedEvent",
        rollbackFor = [NonRetryableException::class],
        noRollbackFor = [RetryableException::class]
    )
    override fun createProduct(product: Product): ProductCreatedEvent {
        val event = ProductCreatedEvent(product.id, product)

        kafkaTemplateProductCreatedEvent.executeInTransaction {
            it.sendDefault(event)
        }.whenComplete { result, exception ->
            if (exception != null) {
                logger.error("Failed to send message", exception)
                throw NonRetryableException(exception.message)
            }
            logger.info("Sent message to topic ${result.recordMetadata.topic()} with offset ${result.recordMetadata.offset()}")
        }.join()

        return event
    }
}
