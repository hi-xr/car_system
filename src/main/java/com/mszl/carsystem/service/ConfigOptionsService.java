package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

/**
 * 车辆配置选项服务（外观颜色、内饰颜色、轮毂等）
 */
public interface ConfigOptionsService {

    /**
     * 返回配置选项：
     * {
     *   exteriorColors: [{ name, hex, extraPrice }],
     *   interiorColors: [{ name, hex, extraPrice }],
     *   wheels: [{ id, name, extraPrice, thumb }]
     * }
     */
    Result getConfigOptions();
}

