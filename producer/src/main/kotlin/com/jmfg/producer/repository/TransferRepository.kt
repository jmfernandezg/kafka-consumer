package com.jmfg.producer.repository

import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import com.jmfg.core.model.WithdrawalRequestedEvent
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

interface TransferRepository : JpaRepository<Transfer, String> {
    @EntityGraph(attributePaths = ["withdrawalRequestedEvent"])
    override fun findById(id: String): Optional<Transfer>
}

@Repository
interface WithdrawalRequestedEventRepository : JpaRepository<WithdrawalRequestedEvent, String>

@Repository
interface DepositRequestedEventRepository : JpaRepository<DepositRequestedEvent, String> {
    @EntityGraph(attributePaths = ["transferRequest"])
    override fun findById(id: String): Optional<DepositRequestedEvent>
}
