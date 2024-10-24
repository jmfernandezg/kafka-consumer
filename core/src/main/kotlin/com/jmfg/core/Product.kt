package com.jmfg.core

import java.util.UUID
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.JoinColumn


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
    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,
    val createdAt: Long
)

interface ProductService {
    fun createProduct(product: Product): ProductCreatedEvent
}
