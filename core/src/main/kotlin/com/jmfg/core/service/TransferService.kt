package com.jmfg.core.service

import com.jmfg.core.model.TransferRequest

interface TransferService {
    fun transfer(transferRequest: TransferRequest): Boolean
}
