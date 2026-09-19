package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.CarModelMapper;
import com.mszl.carsystem.dao.pojo.CarModel;
import com.mszl.carsystem.service.CarModelService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarModelServiceImpl implements CarModelService {

    @Autowired
    private CarModelMapper carModelMapper;

    @Override
    public Result list() {
        List<CarModel> list = carModelMapper.findAll();
        return Result.success(Map.of("models", list));
    }

    @Override
    public Result getById(Integer id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        CarModel model = carModelMapper.findById(id);
        if (model == null) {
            return Result.fail(404, "车型不存在");
        }
        return Result.success(Map.of("model", model));
    }

    @Override
    public Result create(Object payload) {
        Map<String, Object> body = asMap(payload);
        if (body == null) {
            return Result.fail(400, "参数错误");
        }
        CarModel model = fromBody(body);
        if (model.getModelName() == null || model.getModelName().isBlank()) {
            return Result.fail(400, "车型名称不能为空");
        }
        if (model.getBrand() == null || model.getBrand().isBlank()) {
            return Result.fail(400, "品牌不能为空");
        }
        carModelMapper.insert(model);
        Map<String, Object> data = new HashMap<>();
        data.put("modelId", model.getModelId());
        return Result.success(data);
    }

    @Override
    public Result update(Integer id, Object payload) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        Map<String, Object> body = asMap(payload);
        if (body == null) {
            return Result.fail(400, "参数错误");
        }
        CarModel existing = carModelMapper.findById(id);
        if (existing == null) {
            return Result.fail(404, "车型不存在");
        }
        CarModel model = fromBody(body);
        model.setModelId(id);
        carModelMapper.update(model);
        return Result.success(null);
    }

    @Override
    public Result delete(Integer id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "ID 错误");
        }
        CarModel existing = carModelMapper.findById(id);
        if (existing == null) {
            return Result.fail(404, "车型不存在");
        }
        carModelMapper.deleteById(id);
        return Result.success(null);
    }

    private Map<String, Object> asMap(Object payload) {
        if (!(payload instanceof Map<?, ?>)) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) payload;
        return m;
    }

    private CarModel fromBody(Map<String, Object> body) {
        CarModel m = new CarModel();
        m.setModelName(str(body.get("modelName")));
        m.setBrand(str(body.get("brand")));
        m.setGuidePrice(toBigDecimal(body.get("guidePrice")));
        m.setProductionYear(toInt(body.get("productionYear")));
        m.setPowerType(str(body.get("powerType")));
        m.setBodyType(str(body.get("bodyType")));
        m.setCarImage(str(body.get("carImage")));
        return m;
    }

    private String str(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
