package com.jmfg.core


data class Product(
    val name: String,
    val price: Double,
    val quantity: Int
)

data class ProductCreatedEvent(
    val id: String,
    val product: Product,
    val createdAt: Long
)


interface ProductService {
    fun createProduct(product: Product): ProductCreatedEvent
}
