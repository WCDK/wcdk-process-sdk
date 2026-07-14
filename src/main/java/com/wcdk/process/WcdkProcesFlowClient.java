package com.wcdk.process;

import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import com.wcdk.process.dto.TaskResponse;
import com.wcdk.process.dto.WcdkProcesClientRegisterRequest;

import java.util.List;

public interface WcdkProcesFlowClient {

    void registerClient(WcdkProcesClientRegisterRequest request);

    ModelResponse createModel(ModelCreateRequest request);

    List<ModelResponse> listModel();

    DeploymentResponse deployModel(String modelId);

    ProcessRequestResponse createProcessRequest(ProcessRequestCreateRequest request);

    ProcessRequestResponse submitProcessRequest(Long id);

    List<ProcessRequestResponse> listProcessRequest();

    List<TaskResponse> listTask(String assignee);

    void approveProcessRequest(ProcessRequestApproveRequest request);
}
