package com.jmfg.service.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/transfers")
class TransferController {

    private val invocationCounts = ConcurrentHashMap<String, Int>()

    @GetMapping("/check/{id}")
    fun check(@PathVariable id: String): Boolean {
        val count = invocationCounts.merge(id, 1, Int::plus)

        if (!id.last().isDigit() && count!! < 3) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nothing found")
        }

        return true
    }
}
