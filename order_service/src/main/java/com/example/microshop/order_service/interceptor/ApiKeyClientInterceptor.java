package com.example.microshop.order_service.interceptor;

import io.grpc.*;

/**
 * Клиентский gRPC-интерцептор для добавления API-ключа в метаданные запроса.
 * <p>
 * Перехватывает исходящие gRPC-вызовы и добавляет в метаданные заголовок
 * {@code internal-api-key} с заданным значением API-ключа.
 * Это используется для аутентификации межсервисных вызовов на стороне сервера.
 * </p>
 *
 * @see ClientInterceptor
 */
public class ApiKeyClientInterceptor implements ClientInterceptor {

    private final String apiKey;

    public ApiKeyClientInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }

    private static final Metadata.Key<String> API_KEY_METADATA_KEY =
        Metadata.Key.of("internal-api-key", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Перехватывает вызов и оборачивает его в {@link ClientCall}, добавляющий API-ключ в метаданные.
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