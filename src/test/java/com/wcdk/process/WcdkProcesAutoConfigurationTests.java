package com.wcdk.process;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class WcdkProcesAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WcdkProcesAutoConfiguration.class))
            .withPropertyValues(
                    "wcdk.process.client-id=demo-client",
                    "wcdk.process.client-name=流程演示系统",
                    "wcdk.process.endpoint=http://localhost:58082",
                    "wcdk.process.access-key=demo-access-key",
                    "wcdk.process.access-secret=demo-access-secret",
                    "wcdk.process.timeout-seconds=30",
                    "wcdk.process.auto-register=false"
            );

    @Test
    void shouldBindWcdkProcessPropertiesAndExposeConnectionConfig() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WcdkProcesClient.class);
            assertThat(context).hasSingleBean(WcdkProcesConnectionConfig.class);

            WcdkProcesConnectionConfig config = context.getBean(WcdkProcesConnectionConfig.class);
            assertThat(config.getClientId()).isEqualTo("demo-client");
            assertThat(config.getClientName()).isEqualTo("流程演示系统");
            assertThat(config.getEndpoint()).isEqualTo("http://localhost:58082");
            assertThat(config.getAccessKey()).isEqualTo("demo-access-key");
            assertThat(config.getAccessSecret()).isEqualTo("demo-access-secret");
            assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        });
    }
}
