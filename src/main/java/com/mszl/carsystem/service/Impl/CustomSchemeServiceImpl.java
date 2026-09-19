package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.CustomSchemeMapper;
import com.mszl.carsystem.dao.mapper.SchemeOptionMapper;
import com.mszl.carsystem.dao.mapper.SchemeLikeMapper;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.mapper.CarModelMapper;
import com.mszl.carsystem.dao.pojo.CustomScheme;
import com.mszl.carsystem.vo.SchemeOptionItemView;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.service.CustomSchemeService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CustomSchemeServiceImpl implements CustomSchemeService {

    @Autowired
    private CustomSchemeMapper customSchemeMapper;

    @Autowired
    private SchemeOptionMapper schemeOptionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SchemeLikeMapper schemeLikeMapper;

    @Autowired
    private CarModelMapper carModelMapper;

    @Override
    @Transactional
    public Result create(Object payload) {
        if (!(payload instanceof Map)) {
            return Result.fail(400, "参数错误");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;

        Integer userId = asInt(body.get("userId"));
        String username = asString(body.get("username"));
        if (userId == null && username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) userId = u.getId();
        }
        if (userId == null) {
            return Result.fail(400, "缺少 userId/username");
        }

        String modelId = firstNonBlank(
                asString(body.get("modelId")),
                asString(body.get("vehicleId"))
        );
        String vehicleName = asString(body.get("vehicleName"));
        if (modelId == null || modelId.isBlank()) {
            return Result.fail(400, "缺少车型 modelId/vehicleId");
        }

        String schemeName = firstNonBlank(asString(body.get("schemeName")), vehicleName, "未命名方案");
        Integer totalPrice = asInt(body.get("totalPrice"));
        if (totalPrice == null) totalPrice = 0;

        CustomScheme scheme = new CustomScheme();
        scheme.setUserId(userId);
        scheme.setModelId(modelId);
        scheme.setSchemeName(schemeName);
        scheme.setTotalPrice(totalPrice);
        scheme.setStatus(firstNonBlank(asString(body.get("status")), "草稿"));
        scheme.setIsPublic(asBool(body.get("isPublic"), false));
        scheme.setLikesCount(0);
        scheme.setCopiesCount(0);
        scheme.setShareTitle(asString(body.get("shareTitle")));
        scheme.setShareDescription(asString(body.get("shareDescription")));

        customSchemeMapper.insert(scheme);

        List<Long> itemIds = collectSelectedItemIds(body);
        if (!itemIds.isEmpty()) {
            schemeOptionMapper.batchInsert(scheme.getSchemeId(), itemIds);
        }

        return Result.success(Map.of("schemeId", scheme.getSchemeId()));
    }

    @Override
    public Result detail(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        CustomScheme scheme = customSchemeMapper.findById(id);
        if (scheme == null) {
            return Result.fail(404, "方案不存在");
        }

        User user = scheme.getUserId() != null ? userMapper.findById(scheme.getUserId()) : null;
        String authorName = user != null ? user.getUsername() : "未知用户";

        // 根据 custom_scheme.model_id → car_model.model_name
        String modelName = "";
        String modelIdStr = scheme.getModelId();
        if (modelIdStr != null && !modelIdStr.isBlank()) {
            try {
                Integer mid = Integer.parseInt(modelIdStr.trim());
                var carModel = carModelMapper.findById(mid);
                if (carModel != null && carModel.getModelName() != null) {
                    modelName = carModel.getModelName();
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (modelName == null || modelName.isBlank()) {
            modelName = "未知车型";
        }

        List<SchemeOptionItemView> items = schemeOptionMapper.findItemsBySchemeId(id);
        List<Map<String, Object>> itemViews = new ArrayList<>();
        for (SchemeOptionItemView it : items) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", it.getId());
            row.put("categoryId", it.getCategoryId());
            row.put("category", mapCategoryName(it.getCategoryId()));
            row.put("name", it.getName());
            row.put("extraPrice", it.getPrice() == null ? 0 : it.getPrice());
            row.put("required", Boolean.TRUE.equals(it.getMandatory()));
            itemViews.add(row);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("schemeId", scheme.getSchemeId());
        data.put("schemeName", scheme.getSchemeName());
        data.put("userId", scheme.getUserId());
        data.put("userName", authorName);
        data.put("modelId", scheme.getModelId());
        data.put("modelName", modelName);
        data.put("carImage", scheme.getCarImage());
        data.put("createTime", scheme.getCreateTime());
        data.put("totalPrice", scheme.getTotalPrice());
        data.put("likesCount", scheme.getLikesCount());
        data.put("copiesCount", scheme.getCopiesCount());
        data.put("shareTitle", scheme.getShareTitle());
        data.put("shareDescription", scheme.getShareDescription());
        data.put("items", itemViews);
        return Result.success(Map.of("scheme", data));
    }

    @Override
    @Transactional
    public Result update(Long id, Object payload) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        if (!(payload instanceof Map)) {
            return Result.fail(400, "参数错误");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;

        CustomScheme scheme = customSchemeMapper.findById(id);
        if (scheme == null) {
            return Result.fail(404, "方案不存在");
        }

        String schemeName = asString(body.get("schemeName"));
        String shareDescription = asString(body.get("shareDescription"));
        String shareTitle = asString(body.get("shareTitle"));
        Integer newTotalPrice = asInt(body.get("totalPrice"));

        if (schemeName != null && !schemeName.isBlank()) {
            scheme.setSchemeName(schemeName);
        }
        if (shareTitle != null) {
            scheme.setShareTitle(shareTitle);
        }
        if (shareDescription != null) {
            scheme.setShareDescription(shareDescription);
        }
        if (newTotalPrice != null) {
            scheme.setTotalPrice(newTotalPrice);
        }

        // 如果前端传了新的配件 id 列表，则覆盖原有方案配件
        List<Long> itemIds = collectSelectedItemIds(body);
        if (!itemIds.isEmpty()) {
            schemeOptionMapper.deleteBySchemeId(id);
            schemeOptionMapper.batchInsert(id, itemIds);
        }

        customSchemeMapper.updateBasic(scheme);
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result share(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        CustomScheme scheme = customSchemeMapper.findById(id);
        if (scheme == null) {
            return Result.fail(404, "方案不存在");
        }
        customSchemeMapper.updateIsPublic(id, true);
        return Result.success(null);
    }

    @Override
    public Result unshare(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        CustomScheme scheme = customSchemeMapper.findById(id);
        if (scheme == null) {
            return Result.fail(404, "方案不存在");
        }
        customSchemeMapper.updateIsPublic(id, false);
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        CustomScheme scheme = customSchemeMapper.findById(id);
        if (scheme == null) {
            return Result.fail(404, "方案不存在");
        }

        // 1) 删除方案与配置项的关联
        schemeOptionMapper.deleteBySchemeId(id);
        // 2) 删除方案本身
        customSchemeMapper.deleteById(id);

        // 评论等关联信息可以视需要做软删除；当前列表接口不会再返回已删除方案
        return Result.success(null);
    }

    @Override
    public Result list(String username, Boolean onlyPublic) {
        Integer userId = null;
        if (username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) {
                userId = u.getId();
            }
        }

        java.util.List<CustomScheme> schemes;
        if (userId != null) {
            schemes = customSchemeMapper.findByUserId(userId);
        } else if (Boolean.TRUE.equals(onlyPublic)) {
            schemes = customSchemeMapper.findPublic();
        } else {
            schemes = customSchemeMapper.findAll();
        }

        java.util.List<Map<String, Object>> list = new ArrayList<>();
        for (CustomScheme s : schemes) {
            Map<String, Object> row = new HashMap<>();
            row.put("schemeId", s.getSchemeId());
            row.put("schemeName", s.getSchemeName());
            row.put("userId", s.getUserId());
            row.put("userName", s.getUserName());
            row.put("modelId", s.getModelId());
            row.put("carImage", s.getCarImage());
            row.put("createTime", s.getCreateTime());
            row.put("totalPrice", s.getTotalPrice());
            row.put("status", s.getStatus());
            row.put("isPublic", s.getIsPublic());
            row.put("pinned", s.getPinned());
            row.put("likesCount", s.getLikesCount());
            row.put("copiesCount", s.getCopiesCount());
            row.put("shareTitle", s.getShareTitle());
            row.put("shareDescription", s.getShareDescription());
            list.add(row);
        }

        return Result.success(Map.of("schemes", list));
    }

    @Override
    @Transactional
    public Result toggleLike(Long schemeId, Map<String, Object> body) {
        if (schemeId == null || schemeId <= 0) {
            return Result.fail(400, "方案 ID 错误");
        }
        if (body == null) body = new HashMap<>();

        CustomScheme scheme = customSchemeMapper.findById(schemeId);
        if (scheme == null) {
            return Result.fail(404, "方案不存在");
        }

        Object likedObj = body.get("liked");
        // 简化：不再依赖 scheme_like 表，完全按前端传入 liked 进行 +1 / -1，
        // 由前端通过 localStorage 控制“每人只能点一次”的体验。
        boolean liked = likedObj == null ? true : asBool(likedObj, false);

        if (liked) {
            customSchemeMapper.incLikesCount(schemeId);
        } else {
            customSchemeMapper.decLikesCount(schemeId);
        }

        CustomScheme latest = customSchemeMapper.findById(schemeId);
        Integer likesCount = latest != null ? latest.getLikesCount() : null;
        return Result.success(Map.of(
                "liked", liked,
                "likesCount", likesCount == null ? 0 : likesCount
        ));
    }

//    @Override
//    public Result getPopularSchemes() {
//        java.util.List<CustomScheme> publicList = customSchemeMapper.findPublic();
//        java.util.List<CustomScheme> defaultList;
//        try {
//            defaultList = customSchemeMapper.findDefaultSchemes();
//        } catch (Exception e) {
//            defaultList = new ArrayList<>();
//        }
//        java.util.Set<Long> seen = new java.util.HashSet<>();
//        java.util.List<CustomScheme> merged = new ArrayList<>();
//        for (CustomScheme s : defaultList) {
//            if (s != null && s.getSchemeId() != null && seen.add(s.getSchemeId())) {
//                merged.add(s);
//            }
//        }
//        for (CustomScheme s : publicList) {
//            if (s != null && s.getSchemeId() != null && seen.add(s.getSchemeId())) {
//                merged.add(s);
//            }
//        }
//        merged.sort((a, b) -> Integer.compare(
//                (b.getLikesCount() != null ? b.getLikesCount() : 0),
//                (a.getLikesCount() != null ? a.getLikesCount() : 0)
//        ));
//
//        java.util.List<Map<String, Object>> list = new ArrayList<>();
//        for (CustomScheme s : merged) {
//            Map<String, Object> row = new HashMap<>();
//            row.put("schemeId", s.getSchemeId());
//            row.put("schemeName", s.getSchemeName());
//            row.put("userId", s.getUserId());
//            row.put("userName", s.getUserName());
//            row.put("modelId", s.getModelId());
//            row.put("carImage", s.getCarImage());
//            row.put("createTime", s.getCreateTime());
//            row.put("totalPrice", s.getTotalPrice());
//            row.put("status", s.getStatus());
//            row.put("isPublic", s.getIsPublic());
//            row.put("likesCount", s.getLikesCount());
//            row.put("copiesCount", s.getCopiesCount());
//            row.put("shareTitle", s.getShareTitle());
//            row.put("shareDescription", s.getShareDescription());
//            list.add(row);
//        }
//        return Result.success(Map.of("schemes", list));
//    }

    private static String mapCategoryName(Integer categoryId) {
        if (categoryId == null) return "";
        return switch (categoryId) {
            case 1 -> "外观";
            case 2 -> "内饰";
            case 3 -> "性能";
            case 4 -> "科技";
            default -> "其它";
        };
    }

    private static List<Long> collectSelectedItemIds(Map<String, Object> body) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();

        // 1) 支持直接传平铺数组：selectedOptionItemIds
        Object flat = body.get("selectedOptionItemIds");
        addIds(ids, flat);

        // 2) 支持按分类传：exteriorItemIds / interiorItemIds / performanceItemIds / techItemIds
        addIds(ids, body.get("exteriorItemIds"));
        addIds(ids, body.get("interiorItemIds"));
        addIds(ids, body.get("performanceItemIds"));
        addIds(ids, body.get("techItemIds"));

        // 3) 兼容前端传对象数组：exteriorExtras / interiorExtras / performanceExtras / techExtras
        addIdsFromObjectList(ids, body.get("exteriorExtras"));
        addIdsFromObjectList(ids, body.get("interiorExtras"));
        addIdsFromObjectList(ids, body.get("performanceExtras"));
        addIdsFromObjectList(ids, body.get("techExtras"));

        return new ArrayList<>(ids);
    }

    private static void addIds(Set<Long> ids, Object value) {
        if (!(value instanceof List<?> list)) return;
        for (Object it : list) {
            Long id = asLong(it);
            if (id != null) ids.add(id);
        }
    }

    private static void addIdsFromObjectList(Set<Long> ids, Object value) {
        if (!(value instanceof List<?> list)) return;
        for (Object it : list) {
            if (it instanceof Map<?, ?> m) {
                Long id = asLong(m.get("id"));
                if (id != null) ids.add(id);
            }
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static Integer asInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isBlank()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isBlank()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean asBool(Object v, boolean defaultVal) {
        if (v == null) return defaultVal;
        if (v instanceof Boolean b) return b;
        String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT);
        if (s.equals("true") || s.equals("1")) return true;
        if (s.equals("false") || s.equals("0")) return false;
        return defaultVal;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}

