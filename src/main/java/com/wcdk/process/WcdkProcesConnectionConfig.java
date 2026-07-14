package com.wcdk.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * WCDK Process 连接配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcesConnectionConfig {

    /**
     * 第三方接入方标识。
     */
    private String clientId;

    /**
     * 第三方接入方名称。
     */
    private String clientName;

    /**
     * 流程服务端地址。
     */
    private String endpoint;

    /**
     * 接入密钥标识。
     */
    private String accessKey;

    /**
     * 接入密钥密文。
     */
    private String accessSecret;

    /**
     * 连接超时时间，默认 30 秒。
     */
    @Builder.Default
    private Duration timeout = Duration.ofSeconds(30);
}
