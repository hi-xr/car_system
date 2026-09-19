package com.mszl.carsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mszl.carsystem.dao.mapper")
public class CarsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarsystemApplication.class, args);
    }

}
