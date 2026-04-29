package com.example.microshop.inventory_service.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@GrpcGlobalServerInterceptor
public class ApiKeyServerInterceptor implements ServerInterceptor {

    @Value("${internal.api.key}")
    private String expectedApiKey;

    private static final Metadata.Key<String> API_KEY_METADATA_KEY =
            Metadata.Key.of("internal-api-key", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String apiKey = headers.get(API_KEY_METADATA_KEY);
        if (!expectedApiKey.equals(apiKey)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid API Key"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        return next.startCall(call, headers);
    }
}