package com.jmfg.core

class RetryableException(message: String) : RuntimeException()

class NonRetryableException : RuntimeException()
