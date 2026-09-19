package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/6 22:36
 */
@Data
public class Order {
    private Long id;
    private Integer userId;
    private String orderNo;
    private LocalDateTime orderTime;
    private String status;
    private Long schemeId;
    private String vehicleName;
    private String configSummary;
    private String estimatedDelivery;
    private Integer totalAmount;
}
