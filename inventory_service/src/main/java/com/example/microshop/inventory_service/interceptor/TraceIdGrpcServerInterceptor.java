package com.example.microshop.inventory_service.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@GrpcGlobalServerInterceptor
public class TraceIdGrpcServerInterceptor implements ServerInterceptor {
    public static final Context.Key<String> TRACE_ID_CONTEXT_KEY = Context.key("X-Trace-Id");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String traceId = headers.get(Metadata.Key.of("X-Trace-Id", Metadata.ASCII_STRING_MARSHALLER));
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        Context ctx = Context.current().withValue(TRACE_ID_CONTEXT_KEY, traceId);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}