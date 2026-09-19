package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportMessage {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Integer userId;
    private String username;
    private String content;
    private LocalDateTime createTime;
}

