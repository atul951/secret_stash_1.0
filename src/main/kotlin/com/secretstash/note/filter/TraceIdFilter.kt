package com.secretstash.note.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class TraceIdFilter : OncePerRequestFilter() {

    companion object {
        const val TRACE_ID_HEADER = "X-Trace-ID"
        const val TRACE_ID_MDC_KEY = "traceId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val traceId = request.getHeader(TRACE_ID_HEADER) ?: UUID.randomUUID().toString()
            MDC.put(TRACE_ID_MDC_KEY, traceId)
            response.setHeader(TRACE_ID_HEADER, traceId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY)
        }
    }
}
