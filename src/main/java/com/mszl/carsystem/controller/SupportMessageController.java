package com.mszl.carsystem.controller;

import com.mszl.carsystem.dao.mapper.SupportMessageMapper;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.pojo.SupportMessage;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/support-messages")
public class SupportMessageController {

    private final SupportMessageMapper supportMessageMapper;
    private final UserMapper userMapper;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SupportMessageController(SupportMessageMapper supportMessageMapper, UserMapper userMapper) {
        this.supportMessageMapper = supportMessageMapper;
        this.userMapper = userMapper;
    }

    /** 普通用户：针对订单给管理员留言 */
    @PostMapping
    public Result create(@RequestParam(value = "username", required = false) String username,
                         @RequestBody Map<String, Object> body) {
        String name = normalizeUsername(username);
        User user = userMapper.findByUsername(name);
        if (user == null) {
            return Result.fail(401, "用户不存在或未登录，无法留言");
        }
        Long orderId = parseLong(body == null ? null : body.get("orderId"));
        String orderNo = body == null ? "" : Objects.toString(body.getOrDefault("orderNo", ""), "");
        String content = body == null ? "" : Objects.toString(body.getOrDefault("content", ""), "").trim();
        if (orderId == null) {
            return Result.fail(400, "orderId 不能为空");
        }
        if (content.isEmpty()) {
            return Result.fail(400, "content 不能为空");
        }

        SupportMessage msg = new SupportMessage();
        msg.setOrderId(orderId);
        msg.setOrderNo(orderNo);
        msg.setUserId(user.getId());
        msg.setUsername(user.getUsername());
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        supportMessageMapper.insert(msg);

        return Result.success(Map.of(
                "id", msg.getId(),
                "orderId", msg.getOrderId(),
                "orderNo", msg.getOrderNo(),
                "username", msg.getUsername(),
                "content", msg.getContent(),
                "createTime", dtf.format(msg.getCreateTime())
        ));
    }

    /** 管理员：查看留言（可按订单筛选） */
    @GetMapping
    public Result list(@RequestParam(value = "username", required = false) String username,
                       @RequestParam(value = "orderId", required = false) Long orderId) {
        String name = normalizeUsername(username);
        User user = userMapper.findByUsername(name);
        if (user == null || !isAdminRole(user.getRole())) {
            return Result.fail(403, "无权限：仅管理员可查看留言");
        }
        List<SupportMessage> list = (orderId != null)
                ? supportMessageMapper.findByOrderId(orderId)
                : supportMessageMapper.findAll();

        var mapped = list.stream().map(m -> Map.of(
                "id", m.getId(),
                "orderId", m.getOrderId(),
                "orderNo", m.getOrderNo(),
                "username", m.getUsername(),
                "content", m.getContent(),
                "createTime", m.getCreateTime() == null ? "" : dtf.format(m.getCreateTime())
        )).toList();

        return Result.success(Map.of("messages", mapped));
    }

    private static boolean isAdminRole(String roleRaw) {
        String role = Objects.toString(roleRaw, "").trim();
        return "admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role) || "管理员".equals(role);
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) return "guest";
        return username;
    }

    private static Long parseLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }
}

