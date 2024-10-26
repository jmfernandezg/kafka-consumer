package com.jmfg.producer.service

import com.jmfg.core.NonRetryableException
import com.jmfg.core.TransferServiceException
import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import com.jmfg.core.model.TransferRequest
import com.jmfg.core.model.WithdrawalRequestedEvent
import com.jmfg.core.service.TransferService
import com.jmfg.producer.repository.DepositRequestedEventRepository
import com.jmfg.producer.repository.TransferRepository
import com.jmfg.producer.repository.WithdrawalRequestedEventRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class TransferServiceImpl(
    private val transferRepository: TransferRepository,
    private val withdrawalRequestedEventRepository: WithdrawalRequestedEventRepository,
    private val depositRequestedEventRepository: DepositRequestedEventRepository,
    private val kafkaTemplateDepositMoney: KafkaTemplate<String, DepositRequestedEvent>,
    private val kafkaTemplateWithdrawMoney: KafkaTemplate<String, WithdrawalRequestedEvent>,
    private val webClient: WebClient
) : TransferService {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun transfer(transferRequest: TransferRequest): Boolean {
        logger.info("Processing transfer request: $transferRequest")

        // Check if the transfer already exists
        if (transferRepository.findById(transferRequest.id).isPresent) {
            logger.info("Transfer does exists id: ${transferRequest.id}")
            return false
        }

        if (depositRequestedEventRepository.findById(transferRequest.id).isEmpty) {
            val depositRequestedEvent = createAndSendDepositEvent(transferRequest)

            sendEventToEndpoint(depositRequestedEvent.id)

            val withdrawalEvent = createAndSendWithdrawalRequestedEvent(depositRequestedEvent)

            withdrawalRequestedEventRepository.save(withdrawalEvent)

            sendEventToEndpoint(withdrawalEvent.id)

            transferRepository.save(Transfer(id = withdrawalEvent.id, withdrawalRequestedEvent = withdrawalEvent))
        }
        return true
    }

    fun createAndSendWithdrawalRequestedEvent(depositRequestedEvent: DepositRequestedEvent): WithdrawalRequestedEvent {
        val withdrawalRequestedEvent = WithdrawalRequestedEvent(
            id = depositRequestedEvent.id,
            depositRequestedEvent = depositRequestedEvent
        )

        kafkaTemplateWithdrawMoney.executeInTransaction {
            it.sendDefault(withdrawalRequestedEvent)
        }.whenComplete { result, exception ->
            exception?.let {
                logger.error("Failed to send withdrawal message", exception)
                throw NonRetryableException(exception.message)
            }
            logger.info("Sent withdrawal message to topic ${result.recordMetadata.topic()} with offset ${result.recordMetadata.offset()}")
        }.join()

        return withdrawalRequestedEvent
    }

    fun createAndSendDepositEvent(transferRequest: TransferRequest): DepositRequestedEvent {
        val depositRequestedEvent = DepositRequestedEvent(
            id = transferRequest.id,
            transferRequest = transferRequest
        )

        kafkaTemplateDepositMoney.executeInTransaction {
            it.sendDefault(depositRequestedEvent)
        }.whenComplete { result, exception ->
            exception?.let {
                logger.error("Failed to send deposit message", exception)
                throw NonRetryableException(exception.message)
            }
            logger.info("Sent deposit message to topic ${result.recordMetadata.topic()} with offset ${result.recordMetadata.offset()}")
        }.join()

        return depositRequestedEvent
    }

    private fun sendEventToEndpoint(id: String): Boolean {
        return webClient.get()
            .uri("/transfers/check/$id")
            .retrieve()
            .onStatus({ !it.is2xxSuccessful }) {
                throw TransferServiceException("Failed to send event to endpoint")
            }
            .bodyToMono<Boolean>()
            .block()
            ?: throw TransferServiceException("Failed to send event to endpoint")
    }
}
