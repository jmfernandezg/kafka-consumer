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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    @Transactional(transactionManager = "kafkaTransactionManager")
    override fun transfer(transferRequest: TransferRequest): Boolean {
        logger.info("Processing transfer request: $transferRequest")

        // Check if the transfer already exists
        transferRepository.findByIdOrNull(transferRequest.id)?.let {
            logger.info("Transfer does exists id: ${transferRequest.id}")
            return false
        }

        depositRequestedEventRepository.findByIdOrNull(transferRequest.id) ?: run {
            // Create and send deposit request event
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

            sendDepositEventToEndpoint(depositRequestedEvent)

            val withdrawalEvent = WithdrawalRequestedEvent(depositRequestedEvent = depositRequestedEvent)
            kafkaTemplateWithdrawMoney.executeInTransaction {
                it.sendDefault(withdrawalEvent)
            }.whenComplete { result, exception ->
                exception?.let {
                    logger.error("Failed to send withdrawal message", it)
                    throw NonRetryableException(exception.message)
                }
                logger.info("Sent withdrawal message to topic ${result.recordMetadata.topic()} with offset ${result.recordMetadata.offset()}")
            }.join()

            withdrawalRequestedEventRepository.save(withdrawalEvent)
        }
        return true
    }

    fun sendDepositEventToEndpoint(depositEvent: DepositRequestedEvent) {
        webClient.post()
            .uri("/transfers/check")
            .bodyValue(depositEvent)
            .retrieve()
            .onStatus({ !it.is2xxSuccessful }) {
                throw TransferServiceException("Failed to send deposit event to endpoint")
            }
            .bodyToMono<Transfer>()
            .block()
            ?.let {
                logger.info("Successfully sent deposit event to endpoint: $depositEvent it: $it")
            } ?: throw TransferServiceException("Failed to send deposit event to endpoint")
    }
}
