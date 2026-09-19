package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

public interface ConfigRuleService {
    Result list(String ruleType, String series, String model, Boolean enabled, Long itemAId, Long itemBId);

    Result getById(Long id);

    Result create(Object payload);

    Result update(Long id, Object payload);

    Result delete(Long id);
}

