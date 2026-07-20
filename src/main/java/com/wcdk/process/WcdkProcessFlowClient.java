package com.wcdk.process;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.DeploymentBindingUpdateRequest;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ModelUpdateRequest;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import com.wcdk.process.dto.ProcessDesignerExportRequest;
import com.wcdk.process.dto.ProcessDesignerExportResponse;
import com.wcdk.process.dto.ProcessInstanceResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import com.wcdk.process.dto.StartProcessRequest;
import com.wcdk.process.dto.TaskCompleteRequest;
import com.wcdk.process.dto.TaskResponse;
import com.wcdk.process.dto.WcdkProcessClientResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final String FLOWABLE_PROCESS_PATH = "/flowable/process";

    private static final String FLOWABLE_MODEL_PATH = "/flowable/model";

    private static final String FLOWABLE_DEPLOY_PATH = "/flowable/deploy";

    private static final String FLOWABLE_DESIGNER_PATH = "/flowable/designer";

    private static final String WCDK_PROCESS_CLIENT_PATH = "/wcdk/process/client";

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

    public ProcessDefinitionDetailResponse getProcessRequestDiagramDetail(Long id) {
        return wcdkProcessClient.get(PROCESS_REQUEST_PATH + "/" + id + "/diagram", ProcessDefinitionDetailResponse.class);
    }

    public PageResponse<ProcessRequestResponse> listProcessRequest(Long pageNum,
                                                                    Long pageSize,
                                                                    String processNo,
                                                                    String starter,
                                                                    String businessTitle,
                                                                    String category,
                                                                    String processDefinitionKey,
                                                                    String status) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("pageNum", pageNum);
        queryParams.put("pageSize", pageSize);
        queryParams.put("processNo", processNo);
        queryParams.put("starter", starter);
        queryParams.put("businessTitle", businessTitle);
        queryParams.put("category", category);
        queryParams.put("processDefinitionKey", processDefinitionKey);
        queryParams.put("status", status);
        return wcdkProcessClient.get(PROCESS_REQUEST_PATH + "/list", queryParams, new TypeReference<>() {
        });
    }

    public void approveProcessRequest(ProcessRequestApproveRequest request) {
        wcdkProcessClient.postForVoid(PROCESS_REQUEST_APPROVE_PATH, request);
    }

    public void deleteProcessRequest(Long id, String deleteReason) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("deleteReason", deleteReason);
        wcdkProcessClient.delete(PROCESS_REQUEST_PATH + "/" + id, queryParams);
    }

    public ProcessInstanceResponse startProcess(StartProcessRequest request) {
        return wcdkProcessClient.post(FLOWABLE_PROCESS_PATH + "/start", request, ProcessInstanceResponse.class);
    }

    public ProcessInstanceResponse getProcessInstance(String processInstanceId) {
        return wcdkProcessClient.get(FLOWABLE_PROCESS_PATH + "/instance/" + processInstanceId, ProcessInstanceResponse.class);
    }

    public List<TaskResponse> listTask(String assignee) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("assignee", assignee);
        return wcdkProcessClient.get(FLOWABLE_PROCESS_PATH + "/task/list", queryParams, new TypeReference<>() {
        });
    }

    public void completeTask(TaskCompleteRequest request) {
        wcdkProcessClient.postForVoid(FLOWABLE_PROCESS_PATH + "/task/complete", request);
    }

    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("deleteReason", deleteReason);
        wcdkProcessClient.delete(FLOWABLE_PROCESS_PATH + "/instance/" + processInstanceId, queryParams);
    }

    public void deleteTask(String taskId, String deleteReason) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("deleteReason", deleteReason);
        wcdkProcessClient.delete(FLOWABLE_PROCESS_PATH + "/task/" + taskId, queryParams);
    }

    public ModelResponse createModel(ModelCreateRequest request) {
        return wcdkProcessClient.post(FLOWABLE_MODEL_PATH, request, ModelResponse.class);
    }

    public ModelResponse updateModel(String modelId, ModelUpdateRequest request) {
        return wcdkProcessClient.put(FLOWABLE_MODEL_PATH + "/" + modelId, request, ModelResponse.class);
    }

    public List<ModelResponse> listModel() {
        return listModel(null, null, null, null);
    }

    public List<ModelResponse> listModel(String modelName, String modelKey, String category, String deployed) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("modelName", modelName);
        queryParams.put("modelKey", modelKey);
        queryParams.put("category", category);
        queryParams.put("deployed", deployed);
        return wcdkProcessClient.get(FLOWABLE_MODEL_PATH + "/list", queryParams, new TypeReference<>() {
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

    public ProcessDesignerExportResponse exportDesignerProcess(ProcessDesignerExportRequest request) {
        return wcdkProcessClient.post(FLOWABLE_DESIGNER_PATH + "/export", request, ProcessDesignerExportResponse.class);
    }

    public DeploymentResponse deployProcess(String deploymentName,
                                            String category,
                                            String clientId,
                                            String processBeanName,
                                            Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            return deployProcess(deploymentName, category, clientId, processBeanName,
                    fileName, "application/xml", Files.readAllBytes(filePath));
        } catch (IOException exception) {
            throw new WcdkProcessClientException("读取流程定义文件失败", exception);
        }
    }

    public DeploymentResponse deployProcess(String deploymentName,
                                            String category,
                                            String clientId,
                                            String processBeanName,
                                            String fileName,
                                            String contentType,
                                            byte[] fileContent) {
        Map<String, Object> textParts = new LinkedHashMap<>();
        textParts.put("deploymentName", deploymentName);
        textParts.put("category", category);
        textParts.put("clientId", clientId);
        textParts.put("processBeanName", processBeanName);
        return wcdkProcessClient.postMultipart(FLOWABLE_DEPLOY_PATH + "/process", textParts,
                "file", fileName, contentType, fileContent, DeploymentResponse.class);
    }

    public List<DeploymentResponse> listDeployment() {
        return listDeployment(null, null, null);
    }

    public List<DeploymentResponse> listDeployment(String deploymentName, String category, String clientId) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("deploymentName", deploymentName);
        queryParams.put("category", category);
        queryParams.put("clientId", clientId);
        return wcdkProcessClient.get(FLOWABLE_DEPLOY_PATH + "/list", queryParams, new TypeReference<>() {
        });
    }

    public PageResponse<WcdkProcessClientResponse> listDeployClient(Long pageNum,
                                                                    Long pageSize,
                                                                    String clientId,
                                                                    String clientName) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("pageNum", pageNum);
        queryParams.put("pageSize", pageSize);
        queryParams.put("clientId", clientId);
        queryParams.put("clientName", clientName);
        return wcdkProcessClient.get(FLOWABLE_DEPLOY_PATH + "/client/list", queryParams, new TypeReference<>() {
        });
    }

    public List<String> listClientProcessBean(String clientId) {
        return wcdkProcessClient.get(FLOWABLE_DEPLOY_PATH + "/client/" + clientId + "/process-bean/list", new TypeReference<>() {
        });
    }

    public List<ProcessDefinitionResponse> listProcessDefinition() {
        return wcdkProcessClient.get(FLOWABLE_DEPLOY_PATH + "/definition/list", new TypeReference<>() {
        });
    }

    public ProcessDefinitionDetailResponse getProcessDefinitionDetail(String processDefinitionId) {
        return wcdkProcessClient.get(FLOWABLE_DEPLOY_PATH + "/definition/" + processDefinitionId,
                ProcessDefinitionDetailResponse.class);
    }

    public void updateDeploymentBinding(String deploymentId, DeploymentBindingUpdateRequest request) {
        wcdkProcessClient.putForVoid(FLOWABLE_DEPLOY_PATH + "/" + deploymentId + "/binding", request);
    }

    public void deleteDeployment(String deploymentId, Boolean cascade) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("deploymentId", deploymentId);
        queryParams.put("cascade", cascade);
        wcdkProcessClient.delete(FLOWABLE_DEPLOY_PATH, queryParams);
    }

    public PageResponse<WcdkProcessClientResponse> listClient(Long pageNum,
                                                              Long pageSize,
                                                              String clientId,
                                                              String clientName,
                                                              String callbackUrl,
                                                              String processBeanName,
                                                              String sortProp,
                                                              String sortOrder) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("pageNum", pageNum);
        queryParams.put("pageSize", pageSize);
        queryParams.put("clientId", clientId);
        queryParams.put("clientName", clientName);
        queryParams.put("callbackUrl", callbackUrl);
        queryParams.put("processBeanName", processBeanName);
        queryParams.put("sortProp", sortProp);
        queryParams.put("sortOrder", sortOrder);
        return wcdkProcessClient.get(WCDK_PROCESS_CLIENT_PATH + "/list", queryParams, new TypeReference<>() {
        });
    }

    public Boolean detectClient(String clientId) {
        return wcdkProcessClient.post(WCDK_PROCESS_CLIENT_PATH + "/" + clientId + "/detect", null, Boolean.class);
    }

    public void removeClient(String clientId) {
        wcdkProcessClient.delete(WCDK_PROCESS_CLIENT_PATH + "/" + clientId, null);
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
