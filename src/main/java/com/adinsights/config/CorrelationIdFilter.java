package com.adinsights.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String traceId = httpRequest.getHeader("X-Correlation-Id");

            if (traceId == null) {
                traceId = UUID.randomUUID().toString();
            }

            MDC.put(CORRELATION_ID, traceId);

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}