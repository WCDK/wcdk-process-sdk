package com.wcdk.process;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(WcdkProcesProperties.class)
public class WcdkProcesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesClient wcdkProcesClient() {
        return WcdkProcesClients.defaultClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesConnectionConfig wcdkProcesConnectionConfig(WcdkProcesProperties properties) {
        return properties.toConnectionConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesServerConfig wcdkProcesServerConfig(WcdkProcesProperties properties) {
        return WcdkProcesServerConfig.builder()
                .baseUrl(properties.getEndpoint())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesFlowClient wcdkProcesFlowClient(WcdkProcesServerConfig serverConfig) {
        return WcdkProcesClients.flowClient(serverConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcesBeanRegistry procesBeanRegistry(ApplicationContext applicationContext) {
        return new ProcesBeanRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesCallbackService wcdkProcesCallbackService(ProcesBeanRegistry procesBeanRegistry) {
        return new WcdkProcesCallbackService(procesBeanRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesCallbackController wcdkProcesCallbackController(WcdkProcesCallbackService callbackService) {
        return new WcdkProcesCallbackController(callbackService);
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcesBeanRegistrar wcdkProcesBeanRegistrar(WcdkProcesProperties properties,
                                                           ProcesBeanRegistry procesBeanRegistry,
                                                           WcdkProcesFlowClient flowClient) {
        return new WcdkProcesBeanRegistrar(properties, procesBeanRegistry, flowClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "wcdkProcesBeanRegisterRunner")
    public ApplicationRunner wcdkProcesBeanRegisterRunner(WcdkProcesBeanRegistrar registrar) {
        return args -> registrar.register();
    }
}
