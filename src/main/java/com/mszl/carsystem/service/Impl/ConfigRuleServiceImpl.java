package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.ConfigRuleMapper;
import com.mszl.carsystem.dao.pojo.ConfigRule;
import com.mszl.carsystem.service.ConfigRuleService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ConfigRuleServiceImpl implements ConfigRuleService {

    private static final Set<String> ALLOWED_TYPES = Set.of("mutex", "depend", "compatible");

    @Autowired
    private ConfigRuleMapper configRuleMapper;

    @Override
    public Result list(String ruleType, String series, String model, Boolean enabled, Long itemAId, Long itemBId) {
        boolean hasAnyFilter =
                notBlank(ruleType) || notBlank(series) || notBlank(model) || enabled != null || itemAId != null || itemBId != null;
        if (!hasAnyFilter) {
            return Result.success(configRuleMapper.findAll());
        }
        return Result.success(configRuleMapper.query(trimToNull(ruleType), trimToNull(series), trimToNull(model), enabled, itemAId, itemBId));
    }

    @Override
    public Result getById(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        ConfigRule rule = configRuleMapper.findById(id);
        if (rule == null) {
            return Result.fail(404, "规则不存在");
        }
        return Result.success(rule);
    }

    @Override
    public Result create(Object payload) {
        Map<String, Object> body = asMap(payload);
        if (body == null) {
            return Result.fail(400, "参数错误");
        }
        ConfigRule rule = fromBody(body);

        String type = trimToNull(rule.getRuleType());
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            return Result.fail(400, "ruleType 只能为 mutex / depend / compatible");
        }
        if (rule.getItemAId() == null || rule.getItemAId() <= 0 || rule.getItemBId() == null || rule.getItemBId() <= 0) {
            return Result.fail(400, "itemAId / itemBId 必填");
        }
        if (rule.getItemAId().equals(rule.getItemBId())) {
            return Result.fail(400, "itemAId 与 itemBId 不能相同");
        }
        rule.setRuleType(type);
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }

        configRuleMapper.insert(rule);
        Map<String, Object> data = new HashMap<>();
        data.put("ruleId", rule.getRuleId());
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
        ConfigRule rule = fromBody(body);
        rule.setRuleId(id);

        String type = trimToNull(rule.getRuleType());
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            return Result.fail(400, "ruleType 只能为 mutex / depend / compatible");
        }
        if (rule.getItemAId() == null || rule.getItemAId() <= 0 || rule.getItemBId() == null || rule.getItemBId() <= 0) {
            return Result.fail(400, "itemAId / itemBId 必填");
        }
        if (rule.getItemAId().equals(rule.getItemBId())) {
            return Result.fail(400, "itemAId 与 itemBId 不能相同");
        }
        rule.setRuleType(type);
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }

        int n = configRuleMapper.update(rule);
        if (n <= 0) {
            return Result.fail(404, "规则不存在");
        }
        return Result.success(null);
    }

    @Override
    public Result delete(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        configRuleMapper.deleteById(id);
        return Result.success(null);
    }

    private Map<String, Object> asMap(Object payload) {
        if (!(payload instanceof Map<?, ?>)) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;
        return body;
    }

    private ConfigRule fromBody(Map<String, Object> body) {
        ConfigRule r = new ConfigRule();
        r.setRuleType(parseString(body.get("ruleType")));
        r.setItemAId(parseLong(body.get("itemAId")));
        r.setItemBId(parseLong(body.get("itemBId")));
        // 兼容前端使用 brand/modelName 字段
        Object seriesVal = body.containsKey("series") ? body.get("series") : body.get("brand");
        Object modelVal = body.containsKey("model") ? body.get("model") : body.get("modelName");
        r.setSeries(parseString(seriesVal));
        r.setModel(parseString(modelVal));

        Object enabled = body.containsKey("enabled") ? body.get("enabled") : body.get("isEnabled");
        r.setEnabled(parseBool(enabled));
        return r;
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String parseString(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.isBlank() ? null : s.trim();
    }

    private Long parseLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
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

