package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

public interface AccessoryService {
    Result list();

    Result create(Object payload);

    Result update(Long id, Object payload);

    Result delete(Long id);
}

