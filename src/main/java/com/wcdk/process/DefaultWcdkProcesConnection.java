package com.wcdk.process;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
@Getter
public class DefaultWcdkProcesConnection implements WcdkProcesConnection {

    private static final String EVENT_TYPE_CONNECTED = "CONNECTED";

    private static final String EVENT_TYPE_MESSAGE = "MESSAGE";

    private static final String EVENT_TYPE_CLOSED = "CLOSED";

    private static final String EVENT_TYPE_ERROR = "ERROR";

    private final String connectionId;

    private final WcdkProcesConnectionConfig config;

    private final WcdkProcesConnectionCallback callback;

    private boolean open;

    public DefaultWcdkProcesConnection(WcdkProcesConnectionConfig config, WcdkProcesConnectionCallback callback) {
        this.connectionId = UUID.randomUUID().toString().replace("-", "");
        this.config = config;
        this.callback = callback;
        this.open = false;
    }

    @Override
    public void open() {
        if (open) {
            return;
        }
        open = true;
        callback.onConnected(buildEvent(EVENT_TYPE_CONNECTED, "WcdkProces连接已建立", null, null));
    }

    @Override
    public void publish(String message, Map<String, Object> payload) {
        if (!open) {
            IllegalStateException ex = new IllegalStateException("WcdkProces连接尚未打开，不能发送消息");
            callback.onError(buildEvent(EVENT_TYPE_ERROR, ex.getMessage(), payload, ex));
            throw ex;
        }
        if (!StringUtils.hasText(message)) {
            IllegalArgumentException ex = new IllegalArgumentException("发送消息内容不能为空");
            callback.onError(buildEvent(EVENT_TYPE_ERROR, ex.getMessage(), payload, ex));
            throw ex;
        }
        callback.onMessage(buildEvent(EVENT_TYPE_MESSAGE, message, payload, null));
    }

    @Override
    public void close() {
        if (!open) {
            return;
        }
        open = false;
        callback.onClosed(buildEvent(EVENT_TYPE_CLOSED, "WcdkProces连接已关闭", null, null));
    }

    private WcdkProcesConnectionEvent buildEvent(String eventType, String message, Map<String, Object> payload, Throwable throwable) {
        return WcdkProcesConnectionEvent.builder()
                .connectionId(connectionId)
                .clientId(config.getClientId())
                .clientName(config.getClientName())
                .processInstanceId(resolvePayloadValue(payload, "processInstanceId"))
                .processDefinitionKey(resolvePayloadValue(payload, "processDefinitionKey"))
                .businessKey(resolvePayloadValue(payload, "businessKey"))
                .processBeanName(resolvePayloadValue(payload, "processBeanName"))
                .eventType(eventType)
                .message(message)
                .eventTime(LocalDateTime.now())
                .payload(payload)
                .errorMessage(throwable == null ? null : throwable.getMessage())
                .build();
    }

    private String resolvePayloadValue(Map<String, Object> payload, String key) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text : null;
    }
}
