package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ProcessRequestResponse {

    private Long id;

    private String processNo;

    private String starter;

    private String businessTitle;

    private Map<String, Object> formData;

    private String status;

    private String processInstanceId;

    private String currentTaskId;

    private String currentTaskName;

    private String processDefinitionKey;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
