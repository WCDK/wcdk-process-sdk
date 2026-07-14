package com.wcdk.process.dto;

import lombok.Data;

/**
 * 模型创建请求。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Data
public class ModelCreateRequest {

    private String modelName;

    private String modelKey;

    private String category;

    private String bpmnXml;
}
