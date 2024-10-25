package com.jmfg.service.controller

import net.datafaker.Faker
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

@RestController
@RequestMapping("/products")
class ProductController {

    private val faker = Faker()

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): Map<String, String> {
        if (Random.nextBoolean()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")
        }
        return mapOf(
            "id" to id,
            "name" to faker.commerce().productName(),
            "price" to faker.commerce().price(),
            "description" to faker.company().bs()
        )
    }
}
