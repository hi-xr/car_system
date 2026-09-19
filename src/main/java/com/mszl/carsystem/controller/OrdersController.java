package com.mszl.carsystem.controller;

import com.mszl.carsystem.dao.mapper.ConfigRuleMapper;
import com.mszl.carsystem.dao.mapper.OptionItemMapper;
import com.mszl.carsystem.dao.mapper.OrderMapper;
import com.mszl.carsystem.dao.mapper.SchemeOptionMapper;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.pojo.ConfigRule;
import com.mszl.carsystem.dao.pojo.OptionItem;
import com.mszl.carsystem.dao.pojo.Order;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.SchemeOptionItemView;
import com.mszl.carsystem.vo.param.OrderParam;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final Map<String, List<Map<String, Object>>> ordersByUser = new ConcurrentHashMap<>();
    private final AtomicLong orderSeq = new AtomicLong(100000);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final SchemeOptionMapper schemeOptionMapper;
    private final ConfigRuleMapper configRuleMapper;
    private final OptionItemMapper optionItemMapper;

    public OrdersController(OrderMapper orderMapper,
                            UserMapper userMapper,
                            SchemeOptionMapper schemeOptionMapper,
                            ConfigRuleMapper configRuleMapper,
                            OptionItemMapper optionItemMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.schemeOptionMapper = schemeOptionMapper;
        this.configRuleMapper = configRuleMapper;
        this.optionItemMapper = optionItemMapper;
    }

    @GetMapping
    public Result list(@RequestParam(value = "status", required = false, defaultValue = "all") String status,
                       @RequestParam(value = "username", required = false) String username) {
        String name = normalizeUsername(username);

        User user = userMapper.findByUsername(name);
        if (user == null) {
            return Result.success(Map.of("orders", List.of()));
        }
        String role = Objects.toString(user.getRole(), "").trim();
        boolean isAdmin =
                "admin".equalsIgnoreCase(role) ||
                "manager".equalsIgnoreCase(role) ||
                "管理员".equals(role);

        // 管理员：可查看所有普通用户的订单（并附带用户信息，便于联系）
        if (isAdmin) {
            List<Map<String, Object>> list;
            if ("all".equalsIgnoreCase(status)) {
                list = orderMapper.findAllWithUserInfo();
            } else {
                list = orderMapper.findAllWithUserInfoByStatus(status);
            }
            // 统一时间格式
            for (Map<String, Object> m : list) {
                Object t = m.get("orderTime");
                if (t instanceof LocalDateTime ldt) {
                    m.put("orderTime", dtf.format(ldt));
                } else if (t != null) {
                    m.put("orderTime", Objects.toString(t, ""));
                }
            }
            return Result.success(Map.of("orders", list));
        }

        List<Order> orders;
        if ("all".equalsIgnoreCase(status)) {
            orders = orderMapper.findByUserId(user.getId());
        } else {
            orders = orderMapper.findByUserIdAndStatus(user.getId(), status);
        }

        // 转成前端之前使用的 Map 结构
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("orderTime", dtf.format(o.getOrderTime()));
            m.put("status", o.getStatus());
            m.put("schemeId", o.getSchemeId());
            m.put("vehicleName", o.getVehicleName());
            m.put("configSummary", o.getConfigSummary());
            m.put("estimatedDelivery", o.getEstimatedDelivery());
            m.put("totalAmount", o.getTotalAmount());
            list.add(m);
        }

        return Result.success(Map.of("orders", list));
    }

    /** 管理员处理订单状态：准备中/制造中/完成/已取消... */
    @PatchMapping("/{id}/status")
    public Result updateStatus(@PathVariable("id") Long id,
                               @RequestParam(value = "username", required = false) String username,
                               @RequestBody Map<String, Object> body) {
        String name = normalizeUsername(username);
        User user = userMapper.findByUsername(name);
        String role = user == null ? "" : Objects.toString(user.getRole(), "").trim();
        boolean isAdmin =
                "admin".equalsIgnoreCase(role) ||
                "manager".equalsIgnoreCase(role) ||
                "管理员".equals(role);
        if (user == null || !isAdmin) {
            return Result.fail(403, "无权限：仅管理员可处理订单");
        }
        String status = body == null ? null : Objects.toString(body.getOrDefault("status", ""), "").trim();
        if (status == null || status.isEmpty()) {
            return Result.fail(400, "status 不能为空");
        }
        // 允许的状态集合（兼容历史：pending/producing/delivered/cancelled）
        Set<String> allowed = Set.of(
                "preparing", "producing", "completed", "cancelled",
                "pending", "delivered"
        );
        if (!allowed.contains(status)) {
            return Result.fail(400, "不支持的订单状态：" + status);
        }
        int updated = orderMapper.updateStatus(id, status);
        if (updated <= 0) {
            return Result.fail(404, "订单不存在");
        }
        return Result.success(Map.of("id", id, "status", status));
    }

    @PostMapping
    public Result create(@RequestBody OrderParam param) {
        String username = normalizeUsername(param.getUsername());
        int totalAmount = Optional.ofNullable(param.getTotalAmount()).orElse(0);

        // 根据用户名查出用户，获取 userId，防止 user_id 为空
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Result.fail(401, "用户不存在或未登录，无法创建订单");
        }

        var items = param.getItems();
        if (items == null || items.isEmpty()) {
            return Result.fail(400, "订单商品不能为空");
        }

        Map<String, Object> first = items.get(0);
        String vehicleName = Objects.toString(first.getOrDefault("vehicleName", "配置方案"), "");
        String configSummary = Objects.toString(first.getOrDefault("configSummary", ""), "");
        String estimatedDelivery = Objects.toString(first.getOrDefault("estimatedDelivery", ""), "");

        Long schemeId = null;
        Object rawSchemeId = first.get("schemeId");
        if (rawSchemeId instanceof Number n) {
            schemeId = n.longValue();
        }

        // 在创建订单前，基于配置规则做一次最终校验（互斥 / 依赖）
        if (schemeId != null) {
            String err = validateConfigByRules(schemeId);
            if (err != null && !err.isBlank()) {
                return Result.fail(400, err);
            }
        }

        String nowStr = LocalDateTime.now().format(dtf);
        String orderNo = "OD" + System.currentTimeMillis() + orderSeq.getAndIncrement();

        // 1. 先写入数据库（此时已经完成支付，订单进入“生产中”状态）
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderNo(orderNo);
        order.setOrderTime(LocalDateTime.now());
        // 支付完成后不再是待支付，改为生产中
        order.setStatus("producing");
        order.setSchemeId(schemeId);
        order.setVehicleName(vehicleName);
        order.setConfigSummary(configSummary);
        order.setEstimatedDelivery(estimatedDelivery.isEmpty() ? "预计 30 天内交付" : estimatedDelivery);
        order.setTotalAmount(totalAmount);

        orderMapper.insert(order);      // 插入后，order.id 会被回填

        // 2. 如需兼容原来的内存结构，可以继续往内存 map 里放（可选）
        Map<String, Object> orderView = new HashMap<>();
        orderView.put("id", order.getId());
        orderView.put("orderNo", orderNo);
        orderView.put("orderTime", nowStr);
        orderView.put("status", order.getStatus());
        orderView.put("schemeId", schemeId);
        orderView.put("vehicleName", vehicleName);
        orderView.put("configSummary", configSummary);
        orderView.put("estimatedDelivery", order.getEstimatedDelivery());
        orderView.put("totalAmount", totalAmount);

        ordersByUser.computeIfAbsent(username, k -> new ArrayList<>()).add(orderView);

        return Result.success(orderView);
    }

    /**
     * 使用 config_rule 表对某个方案的配置进行校验：
     * - mutex：不能同时出现
     * - depend：若 A 出现则必须同时包含 B
     */
    private String validateConfigByRules(Long schemeId) {
        try {
            List<SchemeOptionItemView> optionViews = schemeOptionMapper.findItemsBySchemeId(schemeId);
            if (optionViews == null || optionViews.isEmpty()) {
                return null;
            }
            Set<Long> selectedIds = optionViews.stream()
                    .map(SchemeOptionItemView::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (selectedIds.isEmpty()) return null;

            // 只取启用状态的规则
            List<ConfigRule> rules = configRuleMapper.query(null, null, null, true, null, null);
            if (rules == null || rules.isEmpty()) return null;

            Map<Long, String> optionNameCache = new HashMap<>();
            // 先把当前方案的配置项名称放进去
            for (SchemeOptionItemView v : optionViews) {
                if (v.getId() != null) {
                    optionNameCache.putIfAbsent(v.getId(), Optional.ofNullable(v.getName()).orElse("配置项" + v.getId()));
                }
            }

            for (ConfigRule rule : rules) {
                if (rule == null || rule.getItemAId() == null || rule.getItemBId() == null) continue;
                long a = rule.getItemAId();
                long b = rule.getItemBId();
                String type = rule.getRuleType();
                if (type == null) continue;
                type = type.trim().toLowerCase(Locale.ROOT);

                boolean hasA = selectedIds.contains(a);
                boolean hasB = selectedIds.contains(b);

                if ("mutex".equals(type)) {
                    if (hasA && hasB) {
                        String nameA = getOptionNameCached(a, optionNameCache);
                        String nameB = getOptionNameCached(b, optionNameCache);
                        return "配置冲突：\"" + nameA + "\" 与 \"" + nameB + "\" 不能同时选择，请返回修改配置后再下单。";
                    }
                } else if ("depend".equals(type)) {
                    if (hasA && !hasB) {
                        String nameA = getOptionNameCached(a, optionNameCache);
                        String nameB = getOptionNameCached(b, optionNameCache);
                        return "配置不完整：已选择 \"" + nameA + "\"，必须同时选择 \"" + nameB + "\"，请返回补充配置后再下单。";
                    }
                }
            }
        } catch (Exception e) {
            // 校验异常不应阻塞下单流程，如需严格控制可改为返回错误
            e.printStackTrace();
        }
        return null;
    }

    private String getOptionNameCached(Long id, Map<Long, String> cache) {
        if (id == null) return "配置项";
        if (cache.containsKey(id)) return cache.get(id);
        OptionItem item = optionItemMapper.findById(id);
        String name = item != null && item.getName() != null ? item.getName() : "配置项" + id;
        cache.put(id, name);
        return name;
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "guest";
        }
        return username;
    }
}