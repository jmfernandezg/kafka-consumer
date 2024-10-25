package com.jmfg.core.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
data class Product(
    @Id
    val id: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var description: String? = null,
    val price: Double = 0.0,
    val quantity: Int = 0,
    var updatedAt: LocalDateTime? = null
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
data class ProductCreatedEvent(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "product_id")
    val product: Product = Product(),

    val createdAt: LocalDateTime = LocalDateTime.now()
)
