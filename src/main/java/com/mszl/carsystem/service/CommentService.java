package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;

import java.util.Map;

public interface CommentService {
    Result listBySchemeId(Long schemeId);

    Result addComment(Long schemeId, Map<String, Object> body);

    Result likeComment(Integer commentId, Map<String, Object> body);

    Result unlikeComment(Integer commentId, Map<String, Object> body);

    Result updateComment(Long schemeId, Integer commentId, Map<String, Object> body);

    Result deleteComment(Long schemeId, Integer commentId, Map<String, Object> body);
}

