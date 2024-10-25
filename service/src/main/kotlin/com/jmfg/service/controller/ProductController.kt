package com.jmfg.service.controller

import com.jmfg.core.model.Product
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
    fun getProduct(@PathVariable id: String): Product {
        return Product(
            id = id,
            name = faker.commerce().productName(),
            price = faker.commerce().price().toDouble(),
            description = faker.company().bs()
        )
    }
}
