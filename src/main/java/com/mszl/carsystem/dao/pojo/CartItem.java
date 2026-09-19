package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long id;
    private Integer userId;
    private Long schemeId;
    private String vehicleName;
    private String configSummary;
    private String imageUrl;
    private Integer totalPrice;
    private String estimatedDelivery;
    private LocalDateTime createTime;
}

