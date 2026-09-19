package com.mszl.carsystem.controller;

import com.mszl.carsystem.service.AccessoryService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 配件管理接口：供规则页和配置选项使用
 */
@RestController
@RequestMapping("/api/accessories")
public class AccessoryController {

    @Autowired
    private AccessoryService accessoryService;

    @GetMapping
    public Result list() {
        return accessoryService.list();
    }

    @PostMapping
    public Result create(@RequestBody Object body) {
        return accessoryService.create(body);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @RequestBody Object body) {
        return accessoryService.update(id, body);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        return accessoryService.delete(id);
    }
}

