package com.mszl.carsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 让后端可以通过 /uploads/** 访问本地 uploads 目录下的文件
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsDir = Paths.get(System.getProperty("user.dir"), "uploads");
        String location = uploadsDir.toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);

        // 兼容数据库中保存的 car_image 类似 "static/xxx.jpg" 的情况：
        // 让后端可以通过 /static/** 访问本地 static 目录下的文件
        Path staticDir = Paths.get(System.getProperty("user.dir"), "static");
        String staticLocation = staticDir.toUri().toString();
        registry.addResourceHandler("/static/**").addResourceLocations(staticLocation);
    }
}

