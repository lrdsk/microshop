package com.example.microshop.order_service.interceptor;

import io.grpc.*;
import org.slf4j.MDC;

/**
 * Клиентский gRPC-интерцептор для передачи сквозного идентификатора трассировки (trace ID).
 * <p>
 * Перехватывает исходящие gRPC-вызовы и добавляет в метаданные заголовок {@code X-Trace-Id},
 * если он присутствует в {@link MDC} (Mapped Diagnostic Context) текущего потока.
 * Это позволяет передавать trace ID от клиента к серверу для сквозного логирования.
 * </p>
 *
 * @see ServerInterceptor
 * @see MDC
 */
public class TraceIdGrpcClientInterceptor implements ClientInterceptor {
    /**
     * Перехватывает вызов и оборачивает его в {@link ClientCall}, добавляющий trace ID в метаданные.
     *
     * @param method      вызываемый метод
     * @param callOptions опции вызова
     * @param next        следующий канал в цепочке
     * @param <ReqT>      тип запроса
     * @param <RespT>     тип ответа
     * @return обёрнутый клиентский вызов
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String traceId = MDC.get("X-Trace-Id");
                if (traceId != null) {
                    headers.put(Metadata.Key.of("X-Trace-Id", Metadata.ASCII_STRING_MARSHALLER), traceId);
                }
                super.start(responseListener, headers);
            }
        };
    }
}