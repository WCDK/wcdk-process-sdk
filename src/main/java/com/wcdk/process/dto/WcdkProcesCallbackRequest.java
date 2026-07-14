package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class WcdkProcesCallbackRequest {

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 业务主键
     */
    private String businessKey;

    /**
     * 流程定义标识
     */
    private String processDefinitionKey;

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
     * 透传业务数据
     */
    private Map<String, Object> payload;

    /**
     * 错误信息
     */
    private String errorMessage;
}
