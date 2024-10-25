package com.jmfg.consumer.handler

import com.jmfg.core.model.WithdrawalRequestedEvent
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
@KafkaListener(topics = ["withdraw-money-topic"], containerFactory = "kafkaListenerContainerFactory")
class WithdrawalRequestedEventHandler {
    private val logger = getLogger(this.javaClass)

    @KafkaHandler
    fun handle(@Payload event: WithdrawalRequestedEvent) {
        logger.info("Received a new withdrawal event: $event ")
    }
}
