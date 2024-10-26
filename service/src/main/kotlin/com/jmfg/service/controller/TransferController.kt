package com.jmfg.service.controller

import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import net.datafaker.Faker
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/transfers")
class TransferController {
    private val faker = Faker()
    private val invocationCounts = ConcurrentHashMap<String, Int>()

    @PostMapping("/create")
    fun deposit(@RequestBody depositRequest: DepositRequestedEvent): Transfer {
        val count = invocationCounts.merge(depositRequest.transferRequest.senderId, 1, Int::plus) ?: 1

        if (!depositRequest.transferRequest.senderId.last().isDigit() && count < 3) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ID must end in a number")
        }

        return Transfer(
            senderId = depositRequest.transferRequest.senderId + "-rev-" + faker.military().armyRank() + faker.number()
                .numberBetween(1, 100),
            recipientId = depositRequest.transferRequest.recipientId + "-rev-" + faker.observation() + faker.number()
                .numberBetween(1, 100),
            amount = depositRequest.transferRequest.amount,
            comment = faker.company().bs()
        )
    }
}