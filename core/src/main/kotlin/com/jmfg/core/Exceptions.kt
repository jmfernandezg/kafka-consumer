package com.jmfg.core

class RetryableException(message: String) : RuntimeException(message)

class NonRetryableException : RuntimeException()
