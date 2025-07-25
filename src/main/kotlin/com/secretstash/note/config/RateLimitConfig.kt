package com.secretstash.note.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Configuration
class RateLimitConfig {

    @Value("\${rate.limit.requests-per-minute:60}")
    private var requestsPerMinute: Int = 60

    @Bean
    fun rateLimitStore(): ConcurrentHashMap<String, RateLimitInfo> {
        return ConcurrentHashMap()
    }

    data class RateLimitInfo(
        val count: AtomicInteger = AtomicInteger(0),
        val resetTime: Long = System.currentTimeMillis() + 60000 // 1 minute
    )

    fun getRequestsPerMinute(): Int = requestsPerMinute
}
