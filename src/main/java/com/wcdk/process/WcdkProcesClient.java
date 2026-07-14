package com.wcdk.process;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
public interface WcdkProcesClient {

    /**
     * 创建WcdkProces连接
     * 创建连接时必须显式传入回调方法，便于第三方系统接收连接建立、消息推送、关闭和异常通知。
     *
     * @param config   连接配置
     * @param callback 回调方法
     * @return 连接对象
     */
    WcdkProcesConnection createConnection(WcdkProcesConnectionConfig config, WcdkProcesConnectionCallback callback);
}
