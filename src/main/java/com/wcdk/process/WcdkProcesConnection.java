package com.wcdk.process;

import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
public interface WcdkProcesConnection {

    /**
     * 获取连接ID
     *
     * @return 连接ID
     */
    String getConnectionId();

    /**
     * 获取连接配置
     *
     * @return 连接配置
     */
    WcdkProcesConnectionConfig getConfig();

    /**
     * 当前连接是否已打开
     *
     * @return 是否已打开
     */
    boolean isOpen();

    /**
     * 打开连接
     */
    void open();

    /**
     * 推送业务消息
     *
     * @param message 业务消息
     * @param payload 业务数据
     */
    void publish(String message, Map<String, Object> payload);

    /**
     * 关闭连接
     */
    void close();
}
