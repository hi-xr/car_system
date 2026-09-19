package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

public interface CarModelService {
    Result list();

    Result getById(Integer id);

    Result create(Object payload);

    Result update(Integer id, Object payload);

    Result delete(Integer id);
}
