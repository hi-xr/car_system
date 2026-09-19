package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarModel {
    private Integer modelId;
    private String modelName;
    private String brand;
    private BigDecimal guidePrice;
    private Integer productionYear;
    private String powerType;
    private String bodyType;
    private String carImage;
}

