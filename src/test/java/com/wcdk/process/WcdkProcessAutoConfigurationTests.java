package com.wcdk.process;

import com.wcdk.process.annotation.ProcessBean;
import com.wcdk.process.config.WcdkProcessAuthInterceptor;
import com.wcdk.process.config.WcdkProcessWebMvcConfigurer;
import com.wcdk.process.controller.WcdkProcessBeanController;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.support.ProcessBeanRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.core.Ordered;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WcdkProcessAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class, WcdkProcessAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "wcdk.process.client-id=demo-client",
                    "wcdk.process.client-name=流程演示系统",
                    "wcdk.process.endpoint=http://localhost:58082",
                    "wcdk.process.username=admin",
                    "wcdk.process.password=admin123",
                    "wcdk.process.timeout-seconds=30",
                    "wcdk.process.active-report=10"
            );

    @Test
    void shouldUseHighestPrecedenceForAutoConfigurationRegistration() {
        AutoConfigureOrder autoConfigureOrder = WcdkProcessAutoConfiguration.class.getAnnotation(AutoConfigureOrder.class);
        assertThat(autoConfigureOrder).isNotNull();
        assertThat(autoConfigureOrder.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void shouldBindWcdkProcessPropertiesAndExposeProcessBeanEndpoint() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WcdkProcessClient.class);
            assertThat(context).hasSingleBean(WcdkProcessConnectionConfig.class);
            assertThat(context).hasSingleBean(WcdkProcessServerConfig.class);
            assertThat(context).hasSingleBean(WcdkProcessFlowClient.class);
            assertThat(context).hasSingleBean(ProcessBeanRegistry.class);
            assertThat(context).hasSingleBean(WcdkProcessBeanController.class);
            assertThat(context).hasSingleBean(WcdkProcessAuthInterceptor.class);
            assertThat(context).hasSingleBean(WcdkProcessWebMvcConfigurer.class);

            WcdkProcessConnectionConfig config = context.getBean(WcdkProcessConnectionConfig.class);
            WcdkProcessServerConfig serverConfig = context.getBean(WcdkProcessServerConfig.class);
            assertThat(config.getClientId()).isEqualTo("demo-client");
            assertThat(config.getClientName()).isEqualTo("流程演示系统");
            assertThat(config.getEndpoint()).isEqualTo("http://localhost:58082");
            assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.getActiveReportInterval()).isEqualTo(Duration.ofSeconds(10));
            assertThat(config.getAuthFlg()).isNull();
            assertThat(serverConfig.getBaseUrl()).isEqualTo("http://localhost:58082");
            assertThat(serverConfig.getUsername()).isEqualTo("admin");
            assertThat(serverConfig.getPassword()).isEqualTo("admin123");
            assertThat(context.getBean(ProcessBeanRegistry.class).getProcessBeanNames()).containsExactly("demoProcess");
        });
    }

    @Test
    void shouldInvokeAnnotatedMethodThroughUnifiedPrefixEndpoint() {
        contextRunner.run(context -> {
            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) context.getSourceApplicationContext()).build();
            mockMvc.perform(post("/wcdk_process/demoProcess")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "businessKey":"BUS-001",
                                      "message":"流程回调测试"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("处理成功"))
                    .andExpect(jsonPath("$.data").value("BUS-001"));

            TestProcessBeanHandler handler = context.getBean(TestProcessBeanHandler.class);
            assertThat(handler.getLastEvent().get()).isNotNull();
            assertThat(handler.getLastEvent().get().getProcessBeanName()).isEqualTo("demoProcess");
            assertThat(handler.getLastEvent().get().getBusinessKey()).isEqualTo("BUS-001");
        });
    }

    @Test
    void shouldRejectCallbackWhenWcdkAuthHeaderDoesNotMatchConfiguredValue() {
        contextRunner.withPropertyValues("wcdk.process.auth-flg=demo-auth")
                .run(context -> {
                    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) context.getSourceApplicationContext()).build();
                    mockMvc.perform(post("/wcdk_process/demoProcess")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "businessKey":"BUS-001"
                                            }
                                            """))
                            .andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.code").value(401))
                            .andExpect(jsonPath("$.message").value("回调鉴权失败"));
                });
    }

    @Test
    void shouldInvokeCallbackWhenWcdkAuthHeaderMatchesConfiguredValue() {
        contextRunner.withPropertyValues("wcdk.process.auth-flg=demo-auth")
                .run(context -> {
                    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) context.getSourceApplicationContext()).build();
                    mockMvc.perform(post("/wcdk_process/demoProcess")
                                    .header("WCDK_AUTH", "demo-auth")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "businessKey":"BUS-002"
                                            }
                                            """))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200))
                            .andExpect(jsonPath("$.data").value("BUS-002"));
                });
    }

    @Test
    void shouldAcceptRegisterCallbackWithServerEventPayload() {
        contextRunner.withPropertyValues("wcdk.process.auth-flg=WCDK")
                .run(context -> {
                    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) context.getSourceApplicationContext()).build();
                    mockMvc.perform(post("/wcdk_process/register_bak")
                                    .header("WCDK_AUTH", "WCDK")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "clientId":"wcdk-process-demo",
                                              "clientName":"wcdk-process-demo",
                                              "processBeanName":"register_bak",
                                              "eventType":"REGISTER_SUCCESS",
                                              "message":"客户端注册成功",
                                              "eventTime":"2026-07-23T16:00:00",
                                              "errorMessage":null,
                                              "futureField":"兼容服务端新增字段"
                                            }
                                            """))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200));
                });
    }

    @Test
    void shouldInvokeAnnotatedMethodWithRequestParameter() {
        contextRunner.withUserConfiguration(PayloadTestConfiguration.class)
                .run(context -> {
                    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) context.getSourceApplicationContext()).build();
                    mockMvc.perform(post("/wcdk_process/payloadProcess")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "businessKey":"LC-001",
                                              "relatedFormData":{
                                                "amount":100
                                              }
                                            }
                                            """))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200))
                            .andExpect(jsonPath("$.data").value("LC-001"));

                    PayloadProcessBeanHandler handler = context.getBean(PayloadProcessBeanHandler.class);
                    assertThat(handler.getLastRequest().get().getBusinessKey()).isEqualTo("LC-001");
                    assertThat(handler.getLastRequest().get().getRelatedFormData()).containsEntry("amount", 100);
                });
    }

    @Test
    void shouldRequireCompleteBindingWhenDeployModel() {
        contextRunner.run(context -> {
            WcdkProcessFlowClient flowClient = context.getBean(WcdkProcessFlowClient.class);

            assertThatThrownBy(() -> flowClient.deployModel("model-001", "demo-client", ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("选择客户端时必须指定processName");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        TestProcessBeanHandler testProcessBeanHandler() {
            return new TestProcessBeanHandler();
        }
    }

    static class TestProcessBeanHandler {

        private final AtomicReference<WcdkProcessConnectionEvent> lastEvent = new AtomicReference<>();

        @ProcessBean("demoProcess")
        public String handle(WcdkProcessConnectionEvent event) {
            lastEvent.set(event);
            return event.getBusinessKey();
        }

        public AtomicReference<WcdkProcessConnectionEvent> getLastEvent() {
            return lastEvent;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class PayloadTestConfiguration {

        @Bean
        PayloadProcessBeanHandler payloadProcessBeanHandler() {
            return new PayloadProcessBeanHandler();
        }
    }

    static class PayloadProcessBeanHandler {

        private final AtomicReference<PayloadRequest> lastRequest = new AtomicReference<>();

        @ProcessBean("payloadProcess")
        public String handle(PayloadRequest request) {
            lastRequest.set(request);
            return request.getBusinessKey();
        }

        public AtomicReference<PayloadRequest> getLastRequest() {
            return lastRequest;
        }
    }

    static class PayloadRequest {

        private String businessKey;

        private Map<String, Object> relatedFormData;

        public String getBusinessKey() {
            return businessKey;
        }

        public void setBusinessKey(String businessKey) {
            this.businessKey = businessKey;
        }

        public Map<String, Object> getRelatedFormData() {
            return relatedFormData;
        }

        public void setRelatedFormData(Map<String, Object> relatedFormData) {
            this.relatedFormData = relatedFormData;
        }
    }
}
