package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.UserParam;

/**
 * 注册服务接口
 */
public interface RegisterService {
    Result register(UserParam userParam);
}
