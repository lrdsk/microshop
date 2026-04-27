package com.example.microshop.order_service.interceptor;

import io.grpc.*;

public class ApiKeyClientInterceptor implements ClientInterceptor {

    private final String apiKey;

    public ApiKeyClientInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    private static final Metadata.Key<String> API_KEY_METADATA_KEY =
        Metadata.Key.of("internal-api-key", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(API_KEY_METADATA_KEY, apiKey);
                super.start(responseListener, headers);
            }
        };
    }
}