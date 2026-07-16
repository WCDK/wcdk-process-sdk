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
        validateDeployModelRequest(modelId, processBeanName);
        return wcdkProcessClient.post(
                FLOWABLE_MODEL_PATH + "/" + modelId + "/deploy?processBeanName=" + encode(processBeanName),
                null,
                DeploymentResponse.class);
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

    private void validateDeployModelRequest(String modelId, String processBeanName) {
        if (!StringUtils.hasText(modelId)) {
            throw new IllegalArgumentException("部署流程模型时模型ID不能为空");
        }
        if (!StringUtils.hasText(processBeanName)) {
            throw new IllegalArgumentException("部署流程模型时必须指定 processBean");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
