package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
public class DeploymentResponse {

    private String deploymentId;

    private String deploymentName;

    private String fileName;

    private String category;

    private Date deployTime;
}
