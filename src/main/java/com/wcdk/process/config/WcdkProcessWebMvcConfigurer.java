package com.wcdk.process.config;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public class WcdkProcessWebMvcConfigurer implements WebMvcConfigurer {

    private final WcdkProcessAuthInterceptor wcdkProcessAuthInterceptor;

    public WcdkProcessWebMvcConfigurer(WcdkProcessAuthInterceptor wcdkProcessAuthInterceptor) {
        this.wcdkProcessAuthInterceptor = wcdkProcessAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(wcdkProcessAuthInterceptor)
                .addPathPatterns("/wcdk_process/**");
    }
}
