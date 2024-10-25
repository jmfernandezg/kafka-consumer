package com.jmfg.service.controller

import com.jmfg.core.model.DepositRequestedEvent
import com.jmfg.core.model.Transfer
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transfers")
class TransferController {

    @PostMapping("/deposit")
    fun deposit(@RequestBody depositRequest: DepositRequestedEvent): Transfer =
        depositRequest.transferRequest.amount.toInt().let {
            if (it % 2 != 0) {
                Transfer()
            } else {
                throw IllegalArgumentException("Amount must be an odd number")
            }
        }
}
