package com.wcdk.process.dto;

import lombok.Data;

/**
 * 任务响应。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Data
public class TaskResponse {

    private String taskId;

    private String taskName;

    private String assignee;

    private String processInstanceId;

    private String processDefinitionId;
}
