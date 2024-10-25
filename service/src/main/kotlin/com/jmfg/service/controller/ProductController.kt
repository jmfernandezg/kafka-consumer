package com.jmfg.service.controller

import net.datafaker.Faker
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController {

    private val faker = Faker()

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): Map<String, String> {
        return mapOf(
            "id" to id,
            "name" to faker.commerce().productName(),
            "price" to faker.commerce().price(),
            "description" to faker.company().bs()
        )
    }
}
