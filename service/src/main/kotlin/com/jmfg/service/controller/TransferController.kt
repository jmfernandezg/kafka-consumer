package com.jmfg.service.controller

import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/transfers")
class TransferController {

    @PostMapping("/create")
    fun deposit(@RequestBody depositRequest: DepositRequestedEvent): Transfer =
        if (!depositRequest.id.last().isDigit()) Transfer() else
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ID must end in a number")

}
