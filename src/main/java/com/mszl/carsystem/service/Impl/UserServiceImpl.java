package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.service.UserService;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.UpdateProfileParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 23:28
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Override
    public User findUser(String username, String password, String role) {
        return userMapper.findByUsernameAndPasswordAndRole(username, password, role);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public int updateAvatarByUsername(String username, String avatar) {
        return userMapper.updateAvatarByUsername(username, avatar);
    }

    @Override
    public int updateProfileByUsername(String username, String phone, String email, String address) {
        return userMapper.updateProfileByUsername(username, phone, email, address);
    }

    // ================= 个人信息相关（给 UsersController 调用） =================

    @Override
    public Result current(String usernameParam, String authHeader, HttpSession session, HttpServletRequest request) {
        String username = resolveUsername(usernameParam, authHeader, session);
        if (!StringUtils.hasText(username)) {
            return Result.fail(401, "未登录");
        }

        // 1. 优先从 Redis 读取用户基础信息，减少数据库访问，加快头像等信息的首次加载速度。
        Map<String, Object> cachedProfile = loadProfileFromRedis(username);
        User user = null;
        if (cachedProfile == null || cachedProfile.isEmpty()) {
            // Redis 没有缓存，再查询数据库，并在下面写回一份缓存
            user = findByUsername(username);
            if (user == null) {
                return Result.fail(404, "用户不存在");
            }
        }

        Map<String, Object> data = new HashMap<>();
        if (cachedProfile != null && !cachedProfile.isEmpty()) {
            data.putAll(cachedProfile);
        } else {
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            data.put("employeeId", user.getEmployeeId());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());
            data.put("address", user.getAddress());
            data.put("avatar", user.getAvatar());

            // 刚从数据库读到，顺便写一份缓存，后续请求可直接从 Redis 命中
            saveProfileToRedis(username, data);
        }
        String role = String.valueOf(data.getOrDefault("role", ""));
        data.put("identity", "admin".equalsIgnoreCase(role) ? "管理员" : "普通用户");

        Object avatarObj = data.get("avatar");
        String avatar = avatarObj != null ? String.valueOf(avatarObj) : null;
        data.put("avatar", StringUtils.hasText(avatar) ? toAbsoluteUrlIfNeeded(avatar, request) : null);

        return Result.success(data);
    }

    @Override
    public Result uploadAvatar(MultipartFile avatar,
                               String usernameParam,
                               String authHeader,
                               HttpSession session,
                               HttpServletRequest request) throws IOException {
        String username = resolveUsername(usernameParam, authHeader, session);
        if (!StringUtils.hasText(username)) {
            return Result.fail(401, "未登录");
        }
        if (avatar == null || avatar.isEmpty()) {
            return Result.fail(400, "请选择要上传的图片");
        }

        String original = avatar.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        if (!StringUtils.hasText(ext)) {
            ext = ".png";
        }

        Path dir = Paths.get(System.getProperty("user.dir"), "uploads", "avatars");
        Files.createDirectories(dir);

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(filename);
        try (InputStream in = avatar.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String storedPath = "/uploads/avatars/" + filename;
        int rows = updateAvatarByUsername(username, storedPath);
        if (rows <= 0) {
            return Result.fail(500, "头像保存失败（用户不存在或写入失败）");
        }

        // 更新 Redis 中的用户缓存头像字段，确保刷新页面时能立即看到新头像
        Map<String, Object> cached = loadProfileFromRedis(username);
        if (cached == null) {
            cached = new HashMap<>();
        }
        cached.put("avatar", storedPath);
        cached.put("username", username);
        saveProfileToRedis(username, cached);

        Map<String, Object> data = new HashMap<>();
        data.put("avatarUrl", toAbsoluteUrlIfNeeded(storedPath, request));
        return Result.success(data);
    }

    @Override
    public Result updateProfile(UpdateProfileParam body,
                                String usernameParam,
                                String authHeader,
                                HttpSession session) {
        String username = resolveUsername(usernameParam, authHeader, session);
        if (!StringUtils.hasText(username)) {
            return Result.fail(401, "未登录");
        }
        String newUsername = body != null ? body.getUsername() : null;
        if (StringUtils.hasText(newUsername) && !newUsername.equals(username)) {
            if (newUsername.length() > 32) {
                return Result.fail(400, "用户名过长");
            }
            if (userMapper.countByUsername(newUsername) > 0) {
                return Result.fail(400, "用户名已存在");
            }
            int rows = userMapper.updateUsername(username, newUsername);
            if (rows <= 0) {
                return Result.fail(500, "用户名更新失败");
            }
            // 更新 session
            if (session != null) {
                session.setAttribute("username", newUsername);
            }
            // 更新 Redis token -> username（尽量保持原 TTL）
            if (stringRedisTemplate != null && authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (StringUtils.hasText(token)) {
                    String key = "login:" + token;
                    try {
                        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
                        if (ttl != null && ttl > 0) {
                            stringRedisTemplate.opsForValue().set(key, newUsername, ttl, TimeUnit.SECONDS);
                        } else {
                            stringRedisTemplate.opsForValue().set(key, newUsername, 30, TimeUnit.MINUTES);
                        }
                    } catch (Exception ignore) {
                        // ignore
                    }
                }
            }
            username = newUsername;
        }
        String phone = body != null ? body.getPhone() : null;
        String email = body != null ? body.getEmail() : null;
        String address = body != null ? body.getAddress() : null;

        int rows = updateProfileByUsername(username, phone, email, address);
        if (rows <= 0) {
            return Result.fail(500, "更新失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);

        // 同步更新 Redis 中缓存的个人信息
        Map<String, Object> cached = loadProfileFromRedis(username);
        if (cached == null) {
            cached = new HashMap<>();
        }
        cached.put("username", username);
        if (phone != null) {
            cached.put("phone", phone);
        }
        if (email != null) {
            cached.put("email", email);
        }
        if (address != null) {
            cached.put("address", address);
        }
        saveProfileToRedis(username, cached);

        return Result.success(data);
    }

    private String resolveUsername(String usernameParam, String authHeader, HttpSession session) {
        if (StringUtils.hasText(usernameParam)) {
            return usernameParam;
        }
        if (stringRedisTemplate != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (StringUtils.hasText(token)) {
                try {
                    String username = stringRedisTemplate.opsForValue().get("login:" + token);
                    if (StringUtils.hasText(username)) {
                        return username;
                    }
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
        Object u = session != null ? session.getAttribute("username") : null;
        return u != null ? String.valueOf(u) : null;
    }

    /**
     * 从 Redis 中读取用户基础信息缓存。
     */
    private Map<String, Object> loadProfileFromRedis(String username) {
        if (!StringUtils.hasText(username) || stringRedisTemplate == null || objectMapper == null) {
            return null;
        }
        try {
            String key = "user:profile:" + username;
            String json = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            return map;
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 将用户基础信息写入 Redis，供后续 /api/users/current 快速返回头像等数据。
     */
    private void saveProfileToRedis(String username, Map<String, Object> profile) {
        if (!StringUtils.hasText(username) || stringRedisTemplate == null || objectMapper == null || profile == null) {
            return;
        }
        try {
            String key = "user:profile:" + username;
            String json = objectMapper.writeValueAsString(profile);
            stringRedisTemplate.opsForValue().set(key, json, 30, TimeUnit.MINUTES);
        } catch (Exception ignore) {
            // ignore
        }
    }

    private String toAbsoluteUrlIfNeeded(String pathOrUrl, HttpServletRequest request) {
        String v = pathOrUrl.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        if (v.startsWith("/")) {
            return baseUrl + v;
        }
        return baseUrl + "/" + v;
    }
}
