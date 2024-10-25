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
    val recepientId: String,
    val amount: Double
)