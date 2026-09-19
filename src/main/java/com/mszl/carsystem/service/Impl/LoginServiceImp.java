package com.mszl.carsystem.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.service.LoginService;
import com.mszl.carsystem.service.UserService;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 23:00
 */
@Service
public class LoginServiceImp implements LoginService {
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Override
    public Result login(LoginParam loginParam) {
        // 1. 校验参数
        String username = loginParam.getUsername();
        String password = loginParam.getPassword();
        String role = loginParam.getRole();
        String employeeId = loginParam.getEmployeeId() != null ? loginParam.getEmployeeId().trim() : "";
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(role)) {
            return Result.fail(400, "用户名,密码,角色不能为空");
        }
        User user = userService.findUser(username, password, role);
        if (user == null) {
            return Result.fail(400, "用户名,密码或角色错误");
        }
        // 如果数据库中为该用户配置了员工号，则登录时必须填写且与之完全一致
        String dbEmployeeId = user.getEmployeeId() != null ? user.getEmployeeId().trim() : "";
        if (StringUtils.isNotBlank(dbEmployeeId)) {
            if (StringUtils.isBlank(employeeId)) {
                return Result.fail(400, "员工号不能为空");
            }
            if (!employeeId.equals(dbEmployeeId)) {
                return Result.fail(400, "员工号错误");
            }
        }

        // 2. 组装前端需要的用户信息，统一放在 Result.data 中
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("avatar", user.getAvatar());
        data.put("employeeId", user.getEmployeeId());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("address", user.getAddress());

        // 3. 生成登录 token，写入 Redis（key: login:token -> username），Redis 异常时不影响登录本身
        String token = null;
        try {
            token = UUID.randomUUID().toString().replace("-", "");
            String redisKey = "login:" + token;
            stringRedisTemplate.opsForValue()
                    .set(redisKey, user.getUsername(), 30, TimeUnit.MINUTES);
            // 同时缓存一份用户基础信息，供 /api/users/current 快速读取，减少首次加载头像的延迟
            if (objectMapper != null) {
                String profileKey = "user:profile:" + user.getUsername();
                // 这里只缓存与个人信息页相关的字段，避免存入过多无关数据
                java.util.Map<String, Object> profile = new java.util.HashMap<>();
                profile.put("id", user.getId());
                profile.put("username", user.getUsername());
                profile.put("role", user.getRole());
                profile.put("employeeId", user.getEmployeeId());
                profile.put("phone", user.getPhone());
                profile.put("email", user.getEmail());
                profile.put("address", user.getAddress());
                profile.put("avatar", user.getAvatar());
                String json = objectMapper.writeValueAsString(profile);
                stringRedisTemplate.opsForValue().set(profileKey, json, 30, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // 忽略 Redis 异常，前端仍然可以通过 session 或 username 查询
            token = null;
        }
        if (token != null) {
            data.put("token", token);
        }

        return Result.success(data);
    }
}
