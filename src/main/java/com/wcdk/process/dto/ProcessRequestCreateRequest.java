package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestCreateRequest {

    private String processDefinitionKey;

    private Map<String, Object> formData;

    private Boolean submit;
}
