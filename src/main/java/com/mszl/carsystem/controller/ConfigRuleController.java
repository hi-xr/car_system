package com.mszl.carsystem.controller;

import com.mszl.carsystem.dao.mapper.CarModelMapper;
import com.mszl.carsystem.dao.mapper.OptionItemMapper;
import com.mszl.carsystem.dao.pojo.CarModel;
import com.mszl.carsystem.dao.pojo.OptionItem;
import com.mszl.carsystem.service.ConfigRuleService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 配置规则管理接口：对 config_rule 表进行增删改查
 */
@RestController
@RequestMapping("/api/config-rules")
public class ConfigRuleController {

    @Autowired
    private ConfigRuleService configRuleService;

    @Autowired
    private CarModelMapper carModelMapper;

    @Autowired
    private OptionItemMapper optionItemMapper;

    /**
     * 新建/编辑规则时的下拉数据：
     * - brands: 来自 car_model.brand
     * - modelsByBrand: brand -> [modelName...]
     * - optionItems: 来自 option_item（可直接用 item_id 作为配置项选择值）
     */
    @GetMapping("/meta")
    public Result meta() {
        List<CarModel> models = carModelMapper.findAll();
        List<String> brands = models.stream()
                .map(CarModel::getBrand)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Map<String, List<String>> modelsByBrand = new TreeMap<>();
        for (CarModel m : models) {
            String brand = m.getBrand();
            String modelName = m.getModelName();
            if (brand == null || brand.isBlank() || modelName == null || modelName.isBlank()) continue;
            modelsByBrand.computeIfAbsent(brand, k -> new java.util.ArrayList<>()).add(modelName);
        }
        // 去重 + 排序，保持前端下拉稳定
        modelsByBrand.replaceAll((k, v) -> v.stream().distinct().sorted().collect(Collectors.toList()));

        List<OptionItem> optionItems = optionItemMapper.findAll();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("brands", brands);
        data.put("modelsByBrand", modelsByBrand);
        data.put("optionItems", optionItems);
        return Result.success(data);
    }

    @GetMapping
    public Result list(@RequestParam(value = "ruleType", required = false) String ruleType,
                       @RequestParam(value = "series", required = false) String series,
                       @RequestParam(value = "model", required = false) String model,
                       @RequestParam(value = "brand", required = false) String brand,
                       @RequestParam(value = "modelName", required = false) String modelName,
                       @RequestParam(value = "enabled", required = false) Boolean enabled,
                       @RequestParam(value = "itemAId", required = false) Long itemAId,
                       @RequestParam(value = "itemBId", required = false) Long itemBId) {
        String seriesFinal = (series == null || series.isBlank()) ? brand : series;
        String modelFinal = (model == null || model.isBlank()) ? modelName : model;
        return configRuleService.list(ruleType, seriesFinal, modelFinal, enabled, itemAId, itemBId);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable("id") Long id) {
        return configRuleService.getById(id);
    }

    @PostMapping
    public Result create(@RequestBody Object body) {
        return configRuleService.create(body);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @RequestBody Object body) {
        return configRuleService.update(id, body);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        return configRuleService.delete(id);
    }
}

