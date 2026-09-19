package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

import java.util.Map;

public interface CustomSchemeService {
    Result create(Object payload);

    Result detail(Long id);

    Result update(Long id, Object payload);

    Result share(Long id);

    /** 取消分享（将方案设为非公开） */
    Result unshare(Long id);

    /** 删除方案（同时清理关联的配置项等数据） */
    Result delete(Long id);

    /**
     * 方案列表：
     * - 若 username 不为空，则优先按用户过滤
     * - 若 onlyPublic 为 true，则只返回公开方案
     * - 否则返回全部方案
     */
    Result list(String username, Boolean onlyPublic);

    /**
     * 方案点赞/取消点赞（点赞数落库，前端控制“每人一次”的体验）。
     * body 支持：liked(boolean)
     */
    Result toggleLike(Long schemeId, Map<String, Object> body);
}

