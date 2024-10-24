package com.jmfg.consumer.handler

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.jmfg.core.ProductCreatedEvent

@Repository
interface ProductCreatedEventRepository : JpaRepository<ProductCreatedEvent, String>