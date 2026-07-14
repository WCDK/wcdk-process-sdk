package com.wcdk.process;

import com.wcdk.process.dto.WcdkProcesCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class WcdkProcesCallbackService {

    private final ProcesBeanRegistry procesBeanRegistry;

    public void callback(WcdkProcesCallbackRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("流程回调请求不能为空");
        }
        if (!StringUtils.hasText(request.getProcessBeanName())) {
            throw new IllegalArgumentException("流程回调时必须提供 processBeanName");
        }
        WcdkProcesConnectionEvent event = WcdkProcesConnectionEvent.builder()
                .processInstanceId(request.getProcessInstanceId())
                .processDefinitionKey(request.getProcessDefinitionKey())
                .businessKey(request.getBusinessKey())
                .processBeanName(request.getProcessBeanName())
                .eventType(request.getEventType())
                .message(request.getMessage())
                .eventTime(LocalDateTime.now())
                .payload(request.getPayload())
                .errorMessage(request.getErrorMessage())
                .build();
        procesBeanRegistry.invoke(request.getProcessBeanName(), event);
    }
}
