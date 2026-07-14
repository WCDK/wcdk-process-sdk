package com.wcdk.process;

import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import com.wcdk.process.dto.TaskResponse;
import com.wcdk.process.dto.WcdkProcesClientRegisterRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

public class DefaultWcdkProcesFlowClient implements WcdkProcesFlowClient {

    private final WcdkProcesServerConfig serverConfig;

    public DefaultWcdkProcesFlowClient(WcdkProcesServerConfig serverConfig) {
        validateServerConfig(serverConfig);
        this.serverConfig = serverConfig;
    }

    @Override
    public void registerClient(WcdkProcesClientRegisterRequest request) {
        ApiResponse<Void> response = restClient().post()
                .uri("/sdk/wcdkproces/clients/register")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        unwrap(response);
    }

    @Override
    public ModelResponse createModel(ModelCreateRequest request) {
        ApiResponse<ModelResponse> response = restClient().post()
                .uri("/flowable/model")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public List<ModelResponse> listModel() {
        ApiResponse<List<ModelResponse>> response = restClient().get()
                .uri("/flowable/model/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public DeploymentResponse deployModel(String modelId) {
        ApiResponse<DeploymentResponse> response = restClient().post()
                .uri("/flowable/model/{modelId}/deploy", modelId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public ProcessRequestResponse createProcessRequest(ProcessRequestCreateRequest request) {
        ApiResponse<ProcessRequestResponse> response = restClient().post()
                .uri("/process/request")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public ProcessRequestResponse submitProcessRequest(Long id) {
        ApiResponse<ProcessRequestResponse> response = restClient().post()
                .uri("/process/request/{id}/submit", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public List<ProcessRequestResponse> listProcessRequest() {
        ApiResponse<List<ProcessRequestResponse>> response = restClient().get()
                .uri("/process/request/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public List<TaskResponse> listTask(String assignee) {
        String uri = StringUtils.hasText(assignee)
                ? "/flowable/process/task/list?assignee={assignee}"
                : "/flowable/process/task/list";
        RestClient.RequestHeadersSpec<?> requestSpec = StringUtils.hasText(assignee)
                ? restClient().get().uri(uri, assignee)
                : restClient().get().uri(uri);
        ApiResponse<List<TaskResponse>> response = requestSpec.retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    @Override
    public void approveProcessRequest(ProcessRequestApproveRequest request) {
        ApiResponse<Void> response = restClient().post()
                .uri("/process/request/approve")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        unwrap(response);
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl(serverConfig.getBaseUrl())
                .build();
    }

    private void validateServerConfig(WcdkProcesServerConfig serverConfig) {
        if (serverConfig == null) {
            throw new IllegalArgumentException("Process service config cannot be null");
        }
        if (!StringUtils.hasText(serverConfig.getBaseUrl())) {
            throw new IllegalArgumentException("Process service base url cannot be empty");
        }
    }

    private <T> T unwrap(ApiResponse<T> response) {
        if (response == null) {
            throw new IllegalStateException("Process service returned empty response");
        }
        if (!Integer.valueOf(200).equals(response.getCode())) {
            throw new IllegalArgumentException(response.getMessage());
        }
        return response.getData();
    }

    private static final class ApiResponse<T> {

        private Integer code;

        private String message;

        private T data;

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }
    }
}
