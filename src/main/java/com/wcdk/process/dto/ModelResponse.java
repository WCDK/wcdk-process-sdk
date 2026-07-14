package com.wcdk.process.dto;

import lombok.Data;

import java.util.Date;

/**
 * 模型响应。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Data
public class ModelResponse {

    private String modelId;

    private String modelName;

    private String modelKey;

    private String category;

    private Integer version;

    private String deploymentId;

    private Date createTime;

    private Date lastUpdateTime;
}
