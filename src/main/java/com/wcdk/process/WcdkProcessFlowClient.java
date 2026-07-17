package com.wcdk.process;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public class WcdkProcessFlowClient {

    private static final String PROCESS_REQUEST_PATH = "/process/request";

    private static final String PROCESS_REQUEST_APPROVE_PATH = "/process/request/approve";

    private static final String FLOWABLE_MODEL_PATH = "/flowable/model";

    private static final String FLOWABLE_DEPLOY_PATH = "/flowable/deploy";

    private final WcdkProcessClient wcdkProcessClient;

    public WcdkProcessFlowClient(WcdkProcessClient wcdkProcessClient) {
        this.wcdkProcessClient = wcdkProcessClient;
    }

    public ProcessRequestResponse createProcessRequest(ProcessRequestCreateRequest request) {
        return wcdkProcessClient.post(PROCESS_REQUEST_PATH, request, ProcessRequestResponse.class);
    }

    public ProcessRequestResponse submitProcessRequest(Long id) {
        return wcdkProcessClient.post(PROCESS_REQUEST_PATH + "/" + id + "/submit", null, ProcessRequestResponse.class);
    }

    public ProcessRequestResponse getProcessRequest(Long id) {
        return wcdkProcessClient.get(PROCESS_REQUEST_PATH + "/" + id, ProcessRequestResponse.class);
    }

    public void approveProcessRequest(ProcessRequestApproveRequest request) {
        wcdkProcessClient.postForVoid(PROCESS_REQUEST_APPROVE_PATH, request);
    }

    public ModelResponse createModel(ModelCreateRequest request) {
        return wcdkProcessClient.post(FLOWABLE_MODEL_PATH, request, ModelResponse.class);
    }

    public List<ModelResponse> listModel() {
        return wcdkProcessClient.get(FLOWABLE_MODEL_PATH + "/list", new TypeReference<>() {
        });
    }

    public String getModelXml(String modelId) {
        return wcdkProcessClient.get(FLOWABLE_MODEL_PATH + "/" + modelId + "/xml", String.class);
    }

    public DeploymentResponse deployModel(String modelId, String processBeanName) {
        return deployModel(modelId, null, processBeanName);
    }

    public DeploymentResponse deployModel(String modelId, String clientId, String processBeanName) {
        validateDeployModelRequest(modelId, clientId, processBeanName);
        Map<String, Object> queryParams = new LinkedHashMap<>();
        if (StringUtils.hasText(clientId)) {
            queryParams.put("clientId", clientId.trim());
        }
        if (StringUtils.hasText(processBeanName)) {
            queryParams.put("processBeanName", processBeanName.trim());
        }
        String path = appendQuery(FLOWABLE_MODEL_PATH + "/" + modelId + "/deploy", queryParams);
        return wcdkProcessClient.post(path, null, DeploymentResponse.class);
    }

    public void deleteModel(String modelId) {
        wcdkProcessClient.delete(FLOWABLE_MODEL_PATH + "/" + modelId, null);
    }

    public List<DeploymentResponse> listDeployment() {
        return wcdkProcessClient.get(FLOWABLE_DEPLOY_PATH + "/list", new TypeReference<>() {
        });
    }

    public void deleteDeployment(String deploymentId, Boolean cascade) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("deploymentId", deploymentId);
        queryParams.put("cascade", cascade);
        wcdkProcessClient.delete(FLOWABLE_DEPLOY_PATH, queryParams);
    }

    private void validateDeployModelRequest(String modelId, String clientId, String processBeanName) {
        if (!StringUtils.hasText(modelId)) {
            throw new IllegalArgumentException("部署流程模型时模型ID不能为空");
        }
        if (StringUtils.hasText(clientId) && !StringUtils.hasText(processBeanName)) {
            throw new IllegalArgumentException("选择客户端时必须指定processName");
        }
        if (!StringUtils.hasText(clientId) && StringUtils.hasText(processBeanName)) {
            throw new IllegalArgumentException("选择processName时必须指定客户端");
        }
    }

    private String appendQuery(String path, Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }
        StringBuilder builder = new StringBuilder(path);
        builder.append("?");
        boolean first = true;
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!first) {
                builder.append("&");
            }
            builder.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(String.valueOf(entry.getValue())));
            first = false;
        }
        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
