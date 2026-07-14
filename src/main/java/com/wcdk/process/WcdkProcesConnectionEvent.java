package com.wcdk.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcesConnectionEvent {

    /**
     * 连接ID
     */
    private String connectionId;

    /**
     * 接入方标识
     */
    private String clientId;

    /**
     * 接入方名称
     */
    private String clientName;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义标识
     */
    private String processDefinitionKey;

    /**
     * 业务主键
     */
    private String businessKey;

    /**
     * 流程回调bean名称
     */
    private String processBeanName;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件消息
     */
    private String message;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 透传业务数据
     */
    private Map<String, Object> payload;

    /**
     * 异常信息
     */
    private String errorMessage;
}
