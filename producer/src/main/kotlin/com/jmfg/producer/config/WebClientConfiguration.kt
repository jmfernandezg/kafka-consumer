package com.jmfg.producer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {
    @Value("\${spring.webflux.client.base-url}")
    private lateinit var baseUrl: String

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}
