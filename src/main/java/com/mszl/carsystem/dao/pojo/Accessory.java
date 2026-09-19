package com.mszl.carsystem.dao.pojo;

import lombok.Data;

@Data
public class Accessory {
    private Long id;
    private String name;
    /**
     * 配件种类：exterior /  等
     */
    private String category;
    private Integer stockQty;
    private Integer price;
    private Boolean required;
}

