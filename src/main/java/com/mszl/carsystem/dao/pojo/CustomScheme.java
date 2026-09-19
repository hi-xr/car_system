package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomScheme {
    private Long schemeId;
    private Integer userId;
    /** 方案作者用户名（来自 user.username） */
    private String userName;
    private String modelId;
    private String schemeName;
    private LocalDateTime createTime;
    private Integer totalPrice;
    private String status;
    private Boolean isPublic;
    /** 是否置顶（管理员可设置） */
    private Boolean pinned;
    private Integer likesCount;
    private Integer copiesCount;
    private String shareTitle;
    private String shareDescription;
    /** 车型图片（来自 car_model.car_image） */
    private String carImage;
}

