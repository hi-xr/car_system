package com.mszl.carsystem.controller;

import com.mszl.carsystem.service.OptionItemService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 配置项管理接口：对 option_item 表进行增删改查
 */
@RestController
@RequestMapping("/api/option-items")
public class OptionItemController {

    @Autowired
    private OptionItemService optionItemService;

    @GetMapping
    public Result list(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                       @RequestParam(value = "keyword", required = false) String keyword) {
        return optionItemService.list(categoryId, keyword);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable("id") Long id) {
        return optionItemService.getById(id);
    }

    @PostMapping
    public Result create(@RequestBody Object body) {
        return optionItemService.create(body);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @RequestBody Object body) {
        return optionItemService.update(id, body);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        return optionItemService.delete(id);
    }
}

