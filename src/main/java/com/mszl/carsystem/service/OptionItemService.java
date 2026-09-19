package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

public interface OptionItemService {
    Result list(Integer categoryId, String keyword);

    Result getById(Long id);

    Result create(Object payload);

    Result update(Long id, Object payload);

    Result delete(Long id);
}

