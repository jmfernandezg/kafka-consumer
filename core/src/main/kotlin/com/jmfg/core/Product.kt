package com.jmfg.core

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import java.util.UUID


@Entity
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,   
    val name: String,
    val price: Double,
    val quantity: Int
)

@Entity
data class ProductCreatedEvent(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: String = UUID.randomUUID().toString(),
    val product: Product,
    val createdAt: Long
)

interface ProductService {
    fun createProduct(product: Product): ProductCreatedEvent
}
