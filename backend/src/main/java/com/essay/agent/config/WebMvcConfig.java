package com.essay.agent.config;

import com.essay.agent.interceptor.ConcurrentLockInterceptor;
import com.essay.agent.interceptor.SessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Autowired
    private ConcurrentLockInterceptor concurrentLockInterceptor;

    private static final List<String> API_PATHS = Arrays.asList(
        "/api/correct",
        "/api/generate-topic",
        "/api/generate-reference"
    );

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/api/health",
        "/api/session"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/session", "/api/session/**");

        registry.addInterceptor(concurrentLockInterceptor)
                .order(2)
                .addPathPatterns(API_PATHS)
                .excludePathPatterns(EXCLUDE_PATHS);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:/data/uploads/images/", "classpath:uploads/");
    }

}