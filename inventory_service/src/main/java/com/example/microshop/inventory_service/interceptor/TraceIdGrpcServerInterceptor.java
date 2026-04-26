package com.example.microshop.inventory_service.interceptor;

import io.grpc.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TraceIdGrpcServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String traceId = headers.get(Metadata.Key.of("X-Trace-Id", Metadata.ASCII_STRING_MARSHALLER));
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put("X-Trace-Id", traceId);
        try {
            return next.startCall(call, headers);
        } finally {
            MDC.remove("X-Trace-Id");
        }
    }
}