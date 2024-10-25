package com.jmfg.producer.service

import com.jmfg.core.NonRetryableException
import com.jmfg.core.RetryableException
import com.jmfg.core.model.Product
import com.jmfg.core.model.ProductCreatedEvent
import com.jmfg.core.service.ProductService
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductServiceImpl(
    private val kafkaTemplateProductCreatedEvent: KafkaTemplate<String, ProductCreatedEvent>
) :
    ProductService {
    val logger: Logger = getLogger(this::class.java)

    @Transactional(
        rollbackFor = [NonRetryableException::class],
        noRollbackFor = [RetryableException::class],
        transactionManager = "kafkaTransactionManager"
    )
    override fun createProduct(product: Product): ProductCreatedEvent {
        return ProductCreatedEvent(product.id, product).apply {
            ProducerRecord(
                kafkaTemplateProductCreatedEvent.defaultTopic,
                id,
                this
            ).run {
                headers().add("message-id", id.toByteArray())
                kafkaTemplateProductCreatedEvent.send(this).get()
                    .also { sendResult ->
                        logger.info(
                            "Sent message with offset: ${sendResult.recordMetadata.offset()}, partition: ${sendResult.recordMetadata.partition()}, topic: ${sendResult.recordMetadata.topic()}"
                        )
                    }
            }
        }
    }
}
