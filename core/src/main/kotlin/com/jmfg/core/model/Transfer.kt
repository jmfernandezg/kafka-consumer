package com.jmfg.core.model

import jakarta.persistence.*
import java.util.*

@Entity
data class TransferRequest(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val recipientId: String = "",
    val amount: Double = 0.0
)

@Entity
data class WithdrawalRequestedEvent(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @OneToOne(
        mappedBy = "withdrawalRequestedEvent",
        cascade = [CascadeType.ALL]
    )
    val depositRequestedEvent: DepositRequestedEvent = DepositRequestedEvent()
)

@Entity
data class DepositRequestedEvent(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "transfer_request_id")
    val transferRequest: TransferRequest = TransferRequest()
)

@Entity
data class Transfer(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @OneToOne
    @JoinColumn(name = "deposit_requested_event_id")
    val depositRequestedEvent: DepositRequestedEvent = DepositRequestedEvent(),

    var comment: String? = null
)
