package com.wcdk.process.dto;

import lombok.Data;

import java.util.Date;

/**
 * 部署结果。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Data
public class DeploymentResponse {

    private String deploymentId;

    private String deploymentName;

    private String category;

    private Date deployTime;
}
