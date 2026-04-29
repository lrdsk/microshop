package com.example.microshop.auth_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Фильтр для управления сквозным идентификатором трассировки (trace ID).
 * <p>
 * Фильтр извлекает заголовок {@code X-Trace-Id} из входящего HTTP-запроса.
 * Если заголовок отсутствует, генерирует новый UUID в качестве trace ID.
 * Устанавливает trace ID в {@link MDC} (Mapped Diagnostic Context) для использования
 * в логировании в рамках всего текущего запроса, а также добавляет его в HTTP-ответ
 * в тот же заголовок. После обработки запроса очищает MDC.
 * </p>
 * <p>
 * Аннотация {@code @Order(Integer.MIN_VALUE)} гарантирует, что фильтр будет
 * выполняться максимально рано, чтобы trace ID был доступен во всех последующих
 * фильтрах и обработчиках запроса.
 * </p>
 */
@Component
@Order(Integer.MIN_VALUE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = Optional.ofNullable(request.getHeader(TRACE_ID))
                    .orElse(UUID.randomUUID().toString());
            MDC.put(TRACE_ID, traceId);
            response.addHeader(TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}