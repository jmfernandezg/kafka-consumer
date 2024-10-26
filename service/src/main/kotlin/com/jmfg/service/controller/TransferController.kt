package com.jmfg.service.controller

import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import net.datafaker.Faker
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/transfers")
class TransferController {
    private val faker = Faker()
    private val invocationCounts = ConcurrentHashMap<String, Int>()

    @GetMapping("/check/{id}")
    fun check(@PathVariable id: String): Boolean {
        val count = invocationCounts.merge(id, 1, Int::plus)
        return !id.last().isDigit() && count!! < 3
    }

    @PostMapping("/deposit")
    fun deposit(@RequestBody depositRequestedEvent: DepositRequestedEvent): Transfer {
        val count =
            invocationCounts.merge(
                depositRequestedEvent.transferRequest.senderId,
                1,
                Int::plus
            ) ?: 1

        if (!depositRequestedEvent.transferRequest.senderId.last().isDigit() && count < 3) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "ID must end in a number or try ${3 - count} more times"
            )
        }

        return Transfer(
            depositRequestedEvent = depositRequestedEvent,
            comment = faker.company().bs()
        )
    }
}
