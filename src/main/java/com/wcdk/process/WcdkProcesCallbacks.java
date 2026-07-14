package com.wcdk.process;

import java.time.Duration;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
public final class WcdkProcesCallbacks {

    private WcdkProcesCallbacks() {
    }

    /**
     * 根据连接配置创建HTTP回调实现
     *
     * @param config 连接配置
     * @return HTTP回调实现
     */
    public static WcdkProcesConnectionCallback httpCallback(WcdkProcesConnectionConfig config) {
        return new HttpWcdkProcesConnectionCallback(config);
    }

    /**
     * 通过显式参数创建HTTP回调实现
     *
     * @param callbackUrl     回调地址
     * @param callbackHeaders 回调请求头
     * @param timeout         超时时间
     * @return HTTP回调实现
     */
    public static WcdkProcesConnectionCallback httpCallback(String callbackUrl, Map<String, String> callbackHeaders, Duration timeout) {
        return new HttpWcdkProcesConnectionCallback(callbackUrl, callbackHeaders, timeout);
    }
}
