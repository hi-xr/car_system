package com.mszl.carsystem.vo;

import lombok.Data;

@Data
public class SchemeOptionItemView {
    private Long id;            // option_item.item_id
    private Integer categoryId; // option_item.category_id
    private String name;        // option_item.item_name
    private Integer price;      // option_item.option_price
    private Boolean mandatory;  // option_item.is_mandatory
}

