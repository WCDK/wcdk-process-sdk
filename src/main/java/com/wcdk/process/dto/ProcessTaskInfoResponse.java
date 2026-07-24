package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/23
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTaskInfoResponse {

    private String taskId;

    private String taskDefinitionKey;

    private String taskName;

    private String assignee;

    private String processInstanceId;

    private String processDefinitionId;

    private Long processRequestId;
    /**
     * 关联表单数据。
     */
    private Map<String, Object> relatedFormData;

    /**
     * 关联表单ID。
     */
    private Long relatedFormId;

    /**
     * 关联表单名称。
     */
    private String relatedFormName;

    /**
     * 当前任务关联表单列表。
     */
    private List<Map<String, Object>> relatedForms;

    /**
     * 审批时间。
     */
    private LocalDateTime eventTime;


}
