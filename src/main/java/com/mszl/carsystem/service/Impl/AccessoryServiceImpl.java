package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.OptionItemMapper;
import com.mszl.carsystem.dao.pojo.Accessory;
import com.mszl.carsystem.dao.pojo.OptionItem;
import com.mszl.carsystem.service.AccessoryService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessoryServiceImpl implements AccessoryService {

    @Autowired
    private OptionItemMapper optionItemMapper;

    @Override
    public Result list() {
        List<OptionItem> items = optionItemMapper.findAll();
        List<Accessory> result = new ArrayList<>();
        for (OptionItem item : items) {
            Accessory acc = new Accessory();
            acc.setId(item.getId());
            acc.setName(item.getName());
            acc.setCategory(mapCategoryIdToCategory(item.getCategoryId()));
            acc.setStockQty(item.getStockQuantity());
            acc.setPrice(item.getPrice());
            acc.setRequired(item.getMandatory());
            result.add(acc);
        }
        return Result.success(result);
    }

    @Override
    public Result create(Object payload) {
        if (!(payload instanceof Map)) {
            return Result.fail(400, "参数错误");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;
        Accessory acc = new Accessory();
        acc.setName(String.valueOf(body.getOrDefault("name", "")));
        acc.setCategory(String.valueOf(body.getOrDefault("category", "")));
        acc.setStockQty(toInt(body.get("stockQty"), 0));
        acc.setPrice(toInt(body.get("price"), 0));
        acc.setRequired(toBool(body.get("required"), false));
        // 直接写入 option_item 表
        OptionItem item = new OptionItem();
        item.setCategoryId(mapCategoryToOptionCategoryId(acc.getCategory()));
        item.setName(acc.getName());
        item.setPrice(acc.getPrice());
        item.setStockQuantity(acc.getStockQty());
        item.setMandatory(acc.getRequired());
        if (item.getCategoryId() == null) {
            return Result.fail(400, "未知的配件种类");
        }
        optionItemMapper.insert(item);

        Map<String, Object> data = new HashMap<>();
        data.put("id", item.getId());
        return Result.success(data);
    }

    @Override
    public Result update(Long id, Object payload) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        if (!(payload instanceof Map)) {
            return Result.fail(400, "参数错误");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;
        String name = String.valueOf(body.getOrDefault("name", ""));
        String category = String.valueOf(body.getOrDefault("category", ""));
        Integer stockQty = toInt(body.get("stockQty"), 0);
        Integer price = toInt(body.get("price"), 0);
        Boolean required = toBool(body.get("required"), false);

        OptionItem item = optionItemMapper.findById(id);
        if (item == null) {
            return Result.fail(404, "配置项不存在");
        }
        item.setName(name);
        item.setCategoryId(mapCategoryToOptionCategoryId(category));
        item.setStockQuantity(stockQty);
        item.setPrice(price);
        item.setMandatory(required);
        optionItemMapper.update(item);
        return Result.success(null);
    }

    @Override
    public Result delete(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        optionItemMapper.deleteById(id);
        return Result.success(null);
    }

    /**
     * 将配件的 category（exterior/interior/tech/performance）
     * 映射到 option_item 表中使用的 category_id（1/2/3/4）。
     */
    private Integer mapCategoryToOptionCategoryId(String category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case "exterior" -> 1;
            case "interior" -> 2;
            case "performance" -> 3;
            case "tech" -> 4;
            default -> null;
        };
    }

    private String mapCategoryIdToCategory(Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        return switch (categoryId) {
            case 1 -> "exterior";
            case 2 -> "interior";
            case 3 -> "performance";
            case 4 -> "tech";
            default -> null;
        };
    }

    private Integer toInt(Object v, int defaultValue) {
        if (v == null) {
            return defaultValue;
        }
        if (v instanceof Integer i) {
            return i;
        }
        if (v instanceof Long l) {
            return l.intValue();
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Boolean toBool(Object v, boolean defaultValue) {
        if (v == null) {
            return defaultValue;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        if (v instanceof Number n) {
            return n.intValue() != 0;
        }
        String s = String.valueOf(v).trim().toLowerCase();
        if (s.isEmpty()) {
            return defaultValue;
        }
        return s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("y");
    }
}

