package com.mszl.carsystem.controller;

import com.mszl.carsystem.dao.mapper.CartItemMapper;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.pojo.CartItem;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 购物车接口：改为持久化到数据库 cart_item 表
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemMapper cartItemMapper;
    private final UserMapper userMapper;

    public CartController(CartItemMapper cartItemMapper, UserMapper userMapper) {
        this.cartItemMapper = cartItemMapper;
        this.userMapper = userMapper;
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "guest";
        }
        return username;
    }

    @GetMapping
    public Result getCart(@RequestParam(value = "username", required = false) String username) {
        String name = normalizeUsername(username);

        User user = userMapper.findByUsername(name);
        if (user == null) {
            return Result.success(Map.of(
                    "items", List.of(),
                    "summary", Map.of("totalAmount", 0, "estimatedDelivery", "")
            ));
        }

        List<CartItem> rows = cartItemMapper.findByUserId(user.getId());

        List<Map<String, Object>> items = new ArrayList<>();
        int total = 0;
        for (CartItem row : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("id", row.getId());
            it.put("schemeId", row.getSchemeId());
            it.put("vehicleName", row.getVehicleName());
            it.put("configSummary", row.getConfigSummary());
            it.put("imageUrl", row.getImageUrl());
            it.put("totalPrice", row.getTotalPrice());
            it.put("estimatedDelivery", row.getEstimatedDelivery());
            items.add(it);

            Integer price = row.getTotalPrice();
            if (price != null) {
                total += price;
            }
        }

        Map<String, Object> summary = Map.of(
                "totalAmount", total,
                "estimatedDelivery", items.isEmpty() ? "" : "预计 30 天内交付"
        );

        return Result.success(Map.of(
                "items", items,
                "summary", summary
        ));
    }

    @PostMapping
    public Result addToCart(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String name = normalizeUsername(username);

        User user = userMapper.findByUsername(name);
        if (user == null) {
            return Result.fail(401, "用户不存在或未登录，无法加入购物车");
        }

        CartItem item = new CartItem();
        item.setUserId(user.getId());

        Object schemeId = body.get("schemeId");
        if (schemeId instanceof Number n) {
            item.setSchemeId(n.longValue());
        }

        Object vehicleName = body.get("vehicleName");
        if (vehicleName == null) vehicleName = body.get("modelName");
        if (vehicleName == null) vehicleName = body.get("schemeName");
        item.setVehicleName(vehicleName == null ? "配置方案" : vehicleName.toString());

        Object configSummary = body.get("configSummary");
        if (configSummary == null) configSummary = body.get("schemeName");
        item.setConfigSummary(configSummary == null ? "" : configSummary.toString());

        Object imageUrl = body.get("imageUrl");
        item.setImageUrl(imageUrl == null ? "" : imageUrl.toString());

        Object price = body.get("totalPrice");
        int totalPrice = (price instanceof Number n) ? n.intValue() : 0;
        item.setTotalPrice(totalPrice);

        Object estimatedDelivery = body.get("estimatedDelivery");
        item.setEstimatedDelivery(estimatedDelivery == null ? "" : estimatedDelivery.toString());

        cartItemMapper.insert(item);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Long id,
                         @RequestParam(value = "username", required = false) String username) {
        String name = normalizeUsername(username);

        User user = userMapper.findByUsername(name);
        if (user == null) {
            return Result.success(null);
        }

        cartItemMapper.deleteByIdAndUserId(id, user.getId());
        return Result.success(null);
    }
}
