package com.jmfg.core

class RetryableException(message: String?) : RuntimeException(message)

class NonRetryableException(message: String?) : RuntimeException(message)

class TransferServiceException(message: String) : RuntimeException(message)
