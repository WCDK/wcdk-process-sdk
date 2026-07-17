package com.wcdk.process.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.WcdkProcessAutoConfiguration;
import com.wcdk.process.annotation.ProcessBean;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
class WcdkProcessClientAutoRegisterRunnerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[0]);

    @Test
    void shouldAutoRegisterProcessBeanAfterStartup() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/sdk/wcdkprocess/clients/register", exchange -> handleRegister(exchange, requestBodyRef));
        server.start();
        try {
            int port = server.getAddress().getPort();
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class, WcdkProcessAutoConfiguration.class))
                    .withUserConfiguration(TestConfiguration.class)
                    .withPropertyValues(
                            "wcdk.process.client-id=demo-client",
                            "wcdk.process.client-name=流程演示系统",
                            "wcdk.process.endpoint=http://127.0.0.1:" + port,
                            "wcdk.process.username=admin",
                            "wcdk.process.password=admin123",
                            "wcdk.process.timeout-seconds=30",
                            "wcdk.process.callback-url=http://127.0.0.1:58083"
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        context.getBean(org.springframework.boot.ApplicationRunner.class).run(applicationArguments);
                        assertThat(requestBodyRef.get()).isNotBlank();
                        JsonNode root = objectMapper.readTree(requestBodyRef.get());
                        assertThat(root.get("clientId").asText()).isEqualTo("demo-client");
                        assertThat(root.get("clientName").asText()).isEqualTo("流程演示系统");
                        assertThat(root.get("callbackUrl").asText()).isEqualTo("http://127.0.0.1:58083");
                        assertThat(root.get("processBeanNames").isArray()).isTrue();
                        assertThat(root.get("processBeanNames")).hasSize(1);
                        assertThat(root.get("processBeanNames").get(0).asText()).isEqualTo("demoProcess");
                    });
        } finally {
            server.stop(0);
        }
    }

    private void handleRegister(HttpExchange exchange, AtomicReference<String> requestBodyRef) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            requestBodyRef.set(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        byte[] responseBytes = """
                {"code":200,"message":"处理成功","data":null}
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        TestProcessBeanHandler testProcessBeanHandler() {
            return new TestProcessBeanHandler();
        }
    }

    static class TestProcessBeanHandler {

        @ProcessBean("demoProcess")
        public String handle(WcdkProcessConnectionEvent event) {
            return event == null ? "" : event.getBusinessKey();
        }
    }
}
