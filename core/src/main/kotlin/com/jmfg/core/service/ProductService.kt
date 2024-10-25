package com.jmfg.core.service

import com.jmfg.core.model.Product
import com.jmfg.core.model.ProductCreatedEvent

interface ProductService {
    fun createProduct(product: Product): ProductCreatedEvent
}
