package com.mszl.carsystem.controller;

import com.mszl.carsystem.service.ConfigOptionsService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆配置选项接口：供前端在线选配页面使用
 */
@RestController
@RequestMapping("/api")
public class ConfigOptionsController {

    @Autowired
    private ConfigOptionsService configOptionsService;

    @GetMapping("/config-options")
    public Result getConfigOptions() {
        return configOptionsService.getConfigOptions();
    }

    // 单独获取：外观配置（全部 category_id = 1，已在 service 放入 exteriorColors）
    @GetMapping("/options/exterior")
    public Result getExteriorOptions() {
        Object data = configOptionsService.getConfigOptions().getData();
        if (data instanceof java.util.Map<?, ?> map) {
            return Result.success(map.get("exteriorColors"));
        }
        return Result.success(null);
    }

    // 内饰配置
    @GetMapping("/options/interior")
    public Result getInteriorOptions() {
        Object data = configOptionsService.getConfigOptions().getData();
        if (data instanceof java.util.Map<?, ?> map) {
            return Result.success(map.get("interiorColors"));
        }
        return Result.success(null);
    }

    // 性能配置
    @GetMapping("/options/performance")
    public Result getPerformanceOptions() {
        Object data = configOptionsService.getConfigOptions().getData();
        if (data instanceof java.util.Map<?, ?> map) {
            return Result.success(map.get("performanceOptions"));
        }
        return Result.success(null);
    }

    // 科技配置
    @GetMapping("/options/tech")
    public Result getTechOptions() {
        Object data = configOptionsService.getConfigOptions().getData();
        if (data instanceof java.util.Map<?, ?> map) {
            return Result.success(map.get("techOptions"));
        }
        return Result.success(null);
    }
}

