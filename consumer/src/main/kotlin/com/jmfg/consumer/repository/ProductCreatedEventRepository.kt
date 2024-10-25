package com.jmfg.consumer.repository

import com.jmfg.core.ProductCreatedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductCreatedEventRepository : JpaRepository<ProductCreatedEvent, String>
