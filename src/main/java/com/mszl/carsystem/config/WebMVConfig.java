package com.mszl.carsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 22:31
 */
@Configuration
public class WebMVConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/carmodel/**")
                .addResourceLocations("classpath:/carmodel/"); // 如果图片放在 src/main/resources/carmodel/
    }
}
