package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.OptionItemMapper;
import com.mszl.carsystem.dao.pojo.OptionItem;
import com.mszl.carsystem.service.OptionItemService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OptionItemServiceImpl implements OptionItemService {

    @Autowired
    private OptionItemMapper optionItemMapper;

    @Override
    public Result list(Integer categoryId, String keyword) {
        String kw = keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null;
        List<OptionItem> list;
        if (categoryId == null && kw == null) {
            list = optionItemMapper.findAll();
        } else {
            list = optionItemMapper.search(categoryId, kw);
        }
        return Result.success(list);
    }

    @Override
    public Result getById(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        OptionItem item = optionItemMapper.findById(id);
        if (item == null) {
            return Result.fail(404, "配置项不存在");
        }
        return Result.success(item);
    }

    @Override
    public Result create(Object payload) {
        Map<String, Object> body = asMap(payload);
        if (body == null) {
            return Result.fail(400, "参数错误");
        }
        OptionItem item = fromBody(body);
        if (item.getCategoryId() == null || item.getCategoryId() <= 0) {
            return Result.fail(400, "categoryId 必填");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            return Result.fail(400, "name 必填");
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
        Map<String, Object> body = asMap(payload);
        if (body == null) {
            return Result.fail(400, "参数错误");
        }
        OptionItem item = fromBody(body);
        item.setId(id);
        int n = optionItemMapper.update(item);
        if (n <= 0) {
            return Result.fail(404, "配置项不存在");
        }
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

    private Map<String, Object> asMap(Object payload) {
        if (!(payload instanceof Map<?, ?>)) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;
        return body;
    }

    private OptionItem fromBody(Map<String, Object> body) {
        OptionItem item = new OptionItem();
        item.setCategoryId(parseInt(body.get("categoryId")));
        item.setName(parseString(body.get("name")));
        item.setPrice(parseInt(body.getOrDefault("price", 0)));

        Object stock = body.containsKey("stockQuantity") ? body.get("stockQuantity") : body.get("stockQty");
        item.setStockQuantity(parseInt(stock));

        Object mandatory = body.containsKey("mandatory") ? body.get("mandatory") : body.get("required");
        item.setMandatory(parseBool(mandatory));
        return item;
    }

    private String parseString(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.isBlank() ? null : s;
    }

    private Integer parseInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof Number n) return n.intValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        String s = String.valueOf(v).trim().toLowerCase();
        if (s.isEmpty()) return null;
        return s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("y");
    }
}

