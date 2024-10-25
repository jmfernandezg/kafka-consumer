package com.jmfg.core

import jakarta.persistence.*
import java.util.*

@Entity
data class Product(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)

@Entity
data class ProductCreatedEvent(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "product_id")
    val product: Product? = null,

    val createdAt: Long = System.currentTimeMillis()
)

interface ProductService {
    fun createProduct(product: Product): ProductCreatedEvent
}
