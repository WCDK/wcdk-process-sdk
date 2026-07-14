package com.wcdk.process;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@Validated
@ConfigurationProperties(prefix = "wcdk.process")
public class WcdkProcesProperties {

    @NotBlank(message = "wcdk.process.client-id 不能为空")
    private String clientId;

    @NotBlank(message = "wcdk.process.client-name 不能为空")
    private String clientName;

    @NotBlank(message = "wcdk.process.endpoint 不能为空")
    private String endpoint;

    @NotBlank(message = "wcdk.process.access-key 不能为空")
    private String accessKey;

    @NotBlank(message = "wcdk.process.access-secret 不能为空")
    private String accessSecret;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration timeoutSeconds = Duration.ofSeconds(30);

    private boolean autoRegister = true;

    public WcdkProcesConnectionConfig toConnectionConfig() {
        return WcdkProcesConnectionConfig.builder()
                .clientId(clientId)
                .clientName(clientName)
                .endpoint(endpoint)
                .accessKey(accessKey)
                .accessSecret(accessSecret)
                .timeout(timeoutSeconds)
                .build();
    }
}
