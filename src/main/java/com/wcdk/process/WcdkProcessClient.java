package com.wcdk.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public class WcdkProcessClient {

    private static final String REGISTER_PATH = "/sdk/wcdkprocess/clients/register";

    private static final String CALLBACK_PATH = "/sdk/wcdkprocess/callback";

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final WcdkProcessConnectionConfig connectionConfig;

    private final WcdkProcessServerConfig serverConfig;

    public WcdkProcessClient(HttpClient httpClient,
                             ObjectMapper objectMapper,
                             WcdkProcessConnectionConfig connectionConfig,
                             WcdkProcessServerConfig serverConfig) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.connectionConfig = connectionConfig;
        this.serverConfig = serverConfig;
    }

    public void registerClient(Set<String> processBeanNames) {
        WcdkProcessClientRegisterRequest request = WcdkProcessClientRegisterRequest.builder()
                .clientId(connectionConfig.getClientId())
                .clientName(connectionConfig.getClientName())
                .username(connectionConfig.getUsername())
                .password(connectionConfig.getPassword())
                .callbackUrl(connectionConfig.getCallbackUrl())
                .authFlg(connectionConfig.getAuthFlg())
                .processBeanNames(processBeanNames)
                .build();
        postForVoid(REGISTER_PATH, request);
    }

    public void callback(WcdkProcessConnectionEvent event) {
        postForVoid(CALLBACK_PATH, event);
    }

    public void postForVoid(String path, Object body) {
        execute(buildRequest("POST", path, body), objectMapper.getTypeFactory().constructType(Void.class));
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        return execute(buildRequest("POST", path, body), objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> T post(String path, Object body, TypeReference<T> responseType) {
        return execute(buildRequest("POST", path, body), objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> T get(String path, Class<T> responseType) {
        return execute(buildRequest("GET", path, null), objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> T get(String path, TypeReference<T> responseType) {
        return execute(buildRequest("GET", path, null), objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> T get(String path, Map<String, ?> queryParams, Class<T> responseType) {
        return execute(buildRequest("GET", appendQuery(path, queryParams), null),
                objectMapper.getTypeFactory().constructType(responseType));
    }

    public void delete(String path, Map<String, ?> queryParams) {
        execute(buildRequest("DELETE", appendQuery(path, queryParams), null),
                objectMapper.getTypeFactory().constructType(Void.class));
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path)))
                .timeout(serverConfig.getTimeout())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, buildAuthorization());
        if ("POST".equals(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(writeBody(body), StandardCharsets.UTF_8));
        } else if ("DELETE".equals(method)) {
            builder.DELETE();
        } else {
            builder.GET();
        }
        return builder.build();
    }

    private String buildUrl(String path) {
        String baseUrl = serverConfig.getBaseUrl();
        if (!StringUtils.hasText(path)) {
            return trimTrailingSlash(baseUrl);
        }
        String normalizedBaseUrl = trimTrailingSlash(baseUrl);
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBaseUrl + normalizedPath;
    }

    private String trimTrailingSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String appendQuery(String path, Map<String, ?> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }
        StringBuilder builder = new StringBuilder(path);
        builder.append(path.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, ?> entry : queryParams.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (!first) {
                builder.append("&");
            }
            builder.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(String.valueOf(value)));
            first = false;
        }
        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String buildAuthorization() {
        String auth = serverConfig.getUsername() + ":" + serverConfig.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private String writeBody(Object body) {
        try {
            return body == null ? "" : objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new WcdkProcessClientException("请求参数序列化失败", exception);
        }
    }

    private <T> T execute(HttpRequest request, JavaType dataType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = response.body();
                throw new WcdkProcessClientException("服务端调用失败，状态码：" + response.statusCode() + "，响应内容：" + body);
            }
            if (dataType.getRawClass() == Void.class) {
                ApiResponse<Void> apiResponse = readApiResponse(response.body(), objectMapper.getTypeFactory().constructType(Void.class));
                validateApiResponse(apiResponse);
                return null;
            }
            ApiResponse<T> apiResponse = readApiResponse(response.body(), dataType);
            validateApiResponse(apiResponse);
            return apiResponse.getData();
        } catch (IOException exception) {
            throw new WcdkProcessClientException("读取服务端响应失败", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WcdkProcessClientException("服务端调用被中断", exception);
        }
    }

    private <T> ApiResponse<T> readApiResponse(String body, JavaType dataType) throws JsonProcessingException {
        JavaType responseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, dataType);
        return objectMapper.readValue(body, responseType);
    }

    private void validateApiResponse(ApiResponse<?> response) {
        if (response == null) {
            throw new WcdkProcessClientException("服务端返回内容为空");
        }
        if (response.getCode() == null || response.getCode() != 200) {
            throw new WcdkProcessClientException("服务端返回失败：" + response.getMessage());
        }
    }
}
