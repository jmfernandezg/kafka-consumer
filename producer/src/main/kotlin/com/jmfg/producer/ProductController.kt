package com.jmfg.producer

import com.jmfg.core.Product
import com.jmfg.core.ProductService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @PostMapping("/create")
    fun createProduct(@RequestBody product: Product) = productService.createProduct(product)
}
