package com.essay.agent.config;

import com.essay.agent.interceptor.ConcurrentLockInterceptor;
import com.essay.agent.interceptor.RateLimitInterceptor;
import com.essay.agent.interceptor.SessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.image-dir:./uploads/images}")
    private String imageDir;

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private ConcurrentLockInterceptor concurrentLockInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health");

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/ocr/**", "/api/history/**", "/api/session/**");

        registry.addInterceptor(concurrentLockInterceptor)
                .addPathPatterns("/api/essay/correct", "/api/essay/re-correct");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + imageDir + "/");
    }
}