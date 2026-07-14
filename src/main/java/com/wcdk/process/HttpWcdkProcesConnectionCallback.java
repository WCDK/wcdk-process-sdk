package com.wcdk.process;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP 回调实现。
 */
public class HttpWcdkProcesConnectionCallback implements WcdkProcesConnectionCallback {

    private static final String DEFAULT_CALLBACK_PATH = "/sdk/wcdkproces/callback";

    private final String callbackUrl;

    private final Map<String, String> callbackHeaders;

    private final RestClient restClient;

    public HttpWcdkProcesConnectionCallback(WcdkProcesConnectionConfig config) {
        this(resolveCallbackUrl(config), null, config == null ? null : config.getTimeout());
    }

    public HttpWcdkProcesConnectionCallback(String callbackUrl, Map<String, String> callbackHeaders, Duration timeout) {
        this.callbackUrl = callbackUrl;
        this.callbackHeaders = callbackHeaders == null ? Map.of() : new LinkedHashMap<>(callbackHeaders);
        this.restClient = RestClient.builder()
                .requestFactory(buildRequestFactory(timeout))
                .build();
    }

    @Override
    public void onConnected(WcdkProcesConnectionEvent event) {
        sendEvent(event);
    }

    @Override
    public void onMessage(WcdkProcesConnectionEvent event) {
        sendEvent(event);
    }

    @Override
    public void onClosed(WcdkProcesConnectionEvent event) {
        sendEvent(event);
    }

    @Override
    public void onError(WcdkProcesConnectionEvent event) {
        sendEvent(event);
    }

    private void sendEvent(WcdkProcesConnectionEvent event) {
        restClient.post()
                .uri(callbackUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(this::applyHeaders)
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }

    private void applyHeaders(HttpHeaders headers) {
        callbackHeaders.forEach(headers::add);
    }

    private static String resolveCallbackUrl(WcdkProcesConnectionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("HTTP回调配置不能为空");
        }
        if (config.getEndpoint() == null || config.getEndpoint().isBlank()) {
            throw new IllegalArgumentException("HTTP回调地址不能为空");
        }
        return config.getEndpoint().trim() + DEFAULT_CALLBACK_PATH;
    }

    private static SimpleClientHttpRequestFactory buildRequestFactory(Duration timeout) {
        Duration validTimeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(validTimeout);
        requestFactory.setReadTimeout(validTimeout);
        return requestFactory;
    }
}
