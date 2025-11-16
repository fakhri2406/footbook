package com.footbook.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        String key = clientIp + "_" + (System.currentTimeMillis() / 60000);

        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());

        if (counter.increment() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        if (requestCounts.size() > 10000) {
            cleanupOldEntries();
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupOldEntries() {
        long currentMinute = System.currentTimeMillis() / 60000;
        requestCounts.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            long keyMinute = Long.parseLong(key.substring(key.lastIndexOf('_') + 1));
            return currentMinute - keyMinute > 5;
        });
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public int increment() {
            return count.incrementAndGet();
        }
    }
}
