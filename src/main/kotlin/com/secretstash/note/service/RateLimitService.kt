package com.secretstash.note.service

import com.secretstash.note.config.RateLimitConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class RateLimitService(
    private val rateLimitStore: ConcurrentHashMap<String, RateLimitConfig.RateLimitInfo>,
    private val rateLimitConfig: RateLimitConfig
) {

    fun isRateLimitedByIp(httpRequest: HttpServletRequest): Boolean {
        val now = System.currentTimeMillis()
        // Rate limiting by IP - same system requesting many times
        val clientIp = getClientIpAddress(httpRequest)
        return isRateLimited(clientIp, now)
    }

    fun isRateLimitedByUserAndIp(username: String, httpRequest: HttpServletRequest): Boolean {
        val now = System.currentTimeMillis()
        // Rate limiting by IP - same system requesting many users many times
        val clientIp = getClientIpAddress(httpRequest)
        if (isRateLimited(clientIp, now)) {
            return true
        }

        // Rate limiting by Username - multiple systems requesting same user many times
        return isRateLimited(username, now)
    }

    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (xForwardedFor != null && xForwardedFor.isNotEmpty()) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }

    private fun isRateLimited(clientId: String, currentTimeInMs: Long): Boolean {
        val rateLimitInfo = rateLimitStore.computeIfAbsent(clientId) {
            RateLimitConfig.RateLimitInfo(AtomicInteger(0), currentTimeInMs + 60000)
        }

        if (currentTimeInMs > rateLimitInfo.resetTime) {
            rateLimitStore[clientId] = RateLimitConfig.RateLimitInfo(AtomicInteger(1), currentTimeInMs + 60000)
            return false
        }

        val currentCount = rateLimitInfo.count.incrementAndGet()
        return currentCount > rateLimitConfig.getRequestsPerMinute()
    }
}
