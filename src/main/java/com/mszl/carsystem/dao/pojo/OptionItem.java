package com.mszl.carsystem.dao.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 对应数据库中的配置选项表（例如包含 item_id、category_id、item_name 等字段）
 * 如表名或列名与这里不一致，只需要在 Mapper 中调整 SQL 即可。
 */
@Data
@TableName("option_item")
public class OptionItem {
    private Long id;             // item_id
    private Integer categoryId;  // category_id
    private String name;         // item_name
    private Integer price;       // option_price
    private Integer stockQuantity; // stock_quantity
    private Boolean mandatory;     // is_mandatory
}


