package com.jmfg.producer.service

import com.jmfg.core.TransferServiceException
import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import com.jmfg.core.model.TransferRequest
import com.jmfg.core.model.WithdrawalRequestedEvent
import com.jmfg.core.service.TransferService
import com.jmfg.producer.repository.TransferRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class TransferServiceImpl(
    private val transferRepository: TransferRepository,
    private val kafkaTemplateDepositMoney: KafkaTemplate<String, DepositRequestedEvent>,
    private val kafkaTemplateWithdrawMoney: KafkaTemplate<String, WithdrawalRequestedEvent>,
    private val webClient: WebClient
) : TransferService {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional("transactionManager")
    override fun transfer(transferRequest: TransferRequest): Boolean {
        logger.info("Processing transfer request: $transferRequest")
        saveTransfer(transferRequest.toTransfer())

        val depositEvent = DepositRequestedEvent(transferRequest = transferRequest)
        kafkaTemplateDepositMoney.sendDefault(depositEvent.id, depositEvent)
        logger.info("Sent deposit event: $depositEvent")

        sendDepositEventToEndpoint(depositEvent)

        val withdrawalEvent = WithdrawalRequestedEvent(transferRequest = transferRequest)
        kafkaTemplateWithdrawMoney.sendDefault(withdrawalEvent.id, withdrawalEvent)
        logger.info("Sent withdrawal event: $withdrawalEvent")

        return true
    }

    private fun saveTransfer(transfer: Transfer) {
        transferRepository.save(transfer)
        logger.info("Saved transfer: $transfer")
    }

    fun sendDepositEventToEndpoint(depositEvent: DepositRequestedEvent) {
        webClient.post()
            .uri("/transfer/deposit")
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
