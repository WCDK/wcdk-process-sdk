package com.wcdk.process;

import org.springframework.util.StringUtils;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
public class DefaultWcdkProcesClient implements WcdkProcesClient {

    @Override
    public WcdkProcesConnection createConnection(WcdkProcesConnectionConfig config, WcdkProcesConnectionCallback callback) {
        validateConfig(config);
        if (callback == null) {
            throw new IllegalArgumentException("创建WcdkProces连接时必须提供回调方法");
        }
        return new DefaultWcdkProcesConnection(config, callback);
    }

    private void validateConfig(WcdkProcesConnectionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("WcdkProces连接配置不能为空");
        }
        if (!StringUtils.hasText(config.getClientId())) {
            throw new IllegalArgumentException("WcdkProces连接配置中的clientId不能为空");
        }
        if (!StringUtils.hasText(config.getClientName())) {
            throw new IllegalArgumentException("WcdkProces连接配置中的clientName不能为空");
        }
        if (!StringUtils.hasText(config.getEndpoint())) {
            throw new IllegalArgumentException("WcdkProces连接配置中的endpoint不能为空");
        }
        if (!StringUtils.hasText(config.getAccessKey())) {
            throw new IllegalArgumentException("WcdkProces连接配置中的accessKey不能为空");
        }
        if (!StringUtils.hasText(config.getAccessSecret())) {
            throw new IllegalArgumentException("WcdkProces连接配置中的accessSecret不能为空");
        }
    }
}
