package com.example.microshop.order_service.interceptor;

import io.grpc.*;
import org.slf4j.MDC;

public class TraceIdGrpcClientInterceptor implements ClientInterceptor {
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