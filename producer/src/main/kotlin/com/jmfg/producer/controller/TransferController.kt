package com.jmfg.producer.controller

import com.jmfg.core.model.TransferRequest
import com.jmfg.core.service.TransferService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transfers")
class TransferController(private val transferService: TransferService) {

    @PostMapping("/request")
    fun transfer(@RequestBody transferRequest: TransferRequest): Boolean {
        return transferService.transfer(transferRequest)
    }
}
