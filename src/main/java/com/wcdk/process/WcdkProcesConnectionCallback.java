package com.wcdk.process;

/**
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
public interface WcdkProcesConnectionCallback {

    /**
     * 连接建立成功回调
     *
     * @param event 连接事件
     */
    void onConnected(WcdkProcesConnectionEvent event);

    /**
     * 消息推送回调
     *
     * @param event 连接事件
     */
    void onMessage(WcdkProcesConnectionEvent event);

    /**
     * 连接关闭回调
     *
     * @param event 连接事件
     */
    void onClosed(WcdkProcesConnectionEvent event);

    /**
     * 连接异常回调
     *
     * @param event 连接事件
     */
    void onError(WcdkProcesConnectionEvent event);
}
