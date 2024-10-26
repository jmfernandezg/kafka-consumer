package com.jmfg.consumer.handler

import com.jmfg.core.model.DepositRequestedEvent
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
@KafkaListener(
    topics = ["deposit-money-topic"],
    containerFactory = "kafkaListenerContainerFactory"
)
class DepositRequestedEventHandler {
    private val logger = getLogger(this.javaClass)

    @KafkaHandler
    fun handle(@Payload event: DepositRequestedEvent) {
        logger.info("Received a new deposit event: $event ")
    }
}
