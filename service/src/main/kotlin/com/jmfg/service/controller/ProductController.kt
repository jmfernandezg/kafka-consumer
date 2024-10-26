package com.jmfg.service.controller

import com.jmfg.core.model.Product
import net.datafaker.Faker
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/products")
class ProductController {
    private val faker = Faker()
    private val invocationCounts = ConcurrentHashMap<String, Int>()

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): Product {
        val count = invocationCounts.merge(id, 1, Int::plus) ?: 1

        if (!id.last().isDigit() && count < 3) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "ID must end in a number or try ${3 - count} more times :wink: 😊"
            )
        }

        return Product(
            id = id,
            name = faker.commerce().productName(),
            price = faker.commerce().price().toDouble(),
            description = faker.company().bs()
        )
    }
}
