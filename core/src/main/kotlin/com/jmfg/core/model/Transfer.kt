package com.jmfg.core.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
data class Transfer(
    @Id
    val transferId: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val recipientId: String = "",
    val amount: Double = 0.0,
)

data class TransferRequest(
    val senderId: String,
    val recipientId: String = "",
    val amount: Double
) {
    fun toTransfer(): Transfer {
        return Transfer(
            senderId = this.senderId,
            recipientId = this.recipientId,
            amount = this.amount
        )
    }
}

data class WithdrawalRequestedEvent(
    val id: String = UUID.randomUUID().toString(),
    val transferRequest: TransferRequest
)

data class DepositRequestedEvent(
    val id: String = UUID.randomUUID().toString(),
    val transferRequest: TransferRequest
)
