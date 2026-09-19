package com.mszl.carsystem.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 22:27
 */
@Configuration
@MapperScan("com.mszl.carsystem.dao")
public class MybatisPlusConfig {


}
