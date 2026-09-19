package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.OptionItemMapper;
import com.mszl.carsystem.dao.pojo.OptionItem;
import com.mszl.carsystem.service.ConfigOptionsService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigOptionsServiceImpl implements ConfigOptionsService {

    @Autowired
    private OptionItemMapper optionItemMapper;

    @Override
    public Result getConfigOptions() {
        Map<String, Object> data = new HashMap<>();

        List<OptionItem> list = optionItemMapper.findAll();

        List<Map<String, Object>> exterior = new ArrayList<>();
        List<Map<String, Object>> interior = new ArrayList<>();
        List<Map<String, Object>> performance = new ArrayList<>();
        List<Map<String, Object>> tech = new ArrayList<>();

        // 按 category_id 分类，统一字段：
        //  - id:         Long / String
        //  - name:       String
        //  - extraPrice: int
        //  - stockQty:   Integer
        //  - required:   Boolean/Integer
        //
        // 1: 外观配置   -> exteriorColors（包含车漆、轮毂等所有 category_id = 1 的记录）
        // 2: 内饰配置   -> interiorColors
        // 3: 性能配置   -> performanceOptions
        // 4: 科技配置   -> techOptions
        for (OptionItem item : list) {
            int categoryId = item.getCategoryId() != null ? item.getCategoryId() : 0;
            String name = item.getName();
            int price = safePrice(item.getPrice());

            switch (categoryId) {
                case 1 -> {
                    // 外观配置：基础字段
                    Map<String, Object> m = baseOption(item, price);
                    // 对“?色车漆”这类车漆项补充 hex，方便 3D 着色
                    if (name != null && (name.contains("车漆") || name.contains("车身颜色") || name.matches(".*色车漆.*"))) {
                        m.put("hex", pickColorHexByName(name));
                    }
                    exterior.add(m);
                }
                case 2 -> interior.add(baseOption(item, price));
                case 3 -> performance.add(baseOption(item, price));
                case 4 -> tech.add(baseOption(item, price));
                default -> {
                    // 其他分类暂时忽略
                }
            }
        }

        data.put("exteriorColors", exterior);
        data.put("interiorColors", interior);
        data.put("performanceOptions", performance);
        data.put("techOptions", tech);

        return Result.success(data);
    }

    private int safePrice(Integer v) {
        return v != null ? v : 0;
    }

    /**
     * 构造统一结构的配置项：
     * { id, name, extraPrice, stockQty, required }
     */
    private Map<String, Object> baseOption(OptionItem item, int extraPrice) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", item.getId());
        m.put("name", item.getName());
        m.put("extraPrice", extraPrice);
        m.put("stockQty", item.getStockQuantity());
        m.put("required", item.getMandatory());
        return m;
    }

    /**
     * 根据中文颜色名简单映射一个大致的车漆颜色，方便 3D 预览。
     * 例如：“红色车漆”、“黑色车漆”、“珍珠白色车漆”等。
     */
    private String pickColorHexByName(String name) {
        if (name == null) return "#1e3a5f";
        if (name.contains("红")) return "#b91c1c";
        if (name.contains("黑")) return "#111827";
        if (name.contains("白")) return "#f3f4f6";
        if (name.contains("灰")) return "#6b7280";
        if (name.contains("蓝")) return "#1d4ed8";
        return "#1e3a5f";
    }
}

