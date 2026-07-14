package com.wcdk.process;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
public final class WcdkProcesClients {

    private WcdkProcesClients() {
    }

    /**
     * 创建默认客户端
     *
     * @return 默认客户端
     */
    public static WcdkProcesClient defaultClient() {
        return new DefaultWcdkProcesClient();
    }

    /**
     * 创建流程能力客户端。
     *
     * @param serverConfig 流程服务配置
     * @return 流程能力客户端
     */
    public static WcdkProcesFlowClient flowClient(WcdkProcesServerConfig serverConfig) {
        return new DefaultWcdkProcesFlowClient(serverConfig);
    }
}
