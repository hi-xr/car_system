package com.mszl.carsystem.vo.param;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderParam {
    private String username;
    private Integer totalAmount;
    /** items: 前端传来的购物车条目列表，字段结构跟现在 controller 里用的 Map 一样 */
    private List<Map<String, Object>> items;
}