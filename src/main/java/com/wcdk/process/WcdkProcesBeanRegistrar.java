package com.wcdk.process;

import com.wcdk.process.dto.WcdkProcesClientRegisterRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WcdkProcesBeanRegistrar {

    private final WcdkProcesProperties properties;

    private final ProcesBeanRegistry procesBeanRegistry;

    private final WcdkProcesFlowClient wcdkProcesFlowClient;

    public void register() {
        if (!properties.isAutoRegister()) {
            return;
        }
        if (procesBeanRegistry.getProcessBeanNames().isEmpty()) {
            return;
        }
        WcdkProcesClientRegisterRequest request = WcdkProcesClientRegisterRequest.builder()
                .clientId(properties.getClientId())
                .clientName(properties.getClientName())
                .processBeanNames(procesBeanRegistry.getProcessBeanNames())
                .build();
        wcdkProcesFlowClient.registerClient(request);
    }
}
