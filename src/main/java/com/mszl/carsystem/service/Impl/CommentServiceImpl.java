package com.mszl.carsystem.service.Impl;

import com.mszl.carsystem.dao.mapper.CommentMapper;
import com.mszl.carsystem.dao.mapper.CommentLikeMapper;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.pojo.Comment;
import com.mszl.carsystem.vo.CommentView;
import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.service.CommentService;
import com.mszl.carsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    public Result listBySchemeId(Long schemeId) {
        if (schemeId == null || schemeId <= 0) {
            return Result.fail(400, "方案 ID 错误");
        }
        List<CommentView> list = commentMapper.findBySchemeId(schemeId);
        return Result.success(Map.of("comments", list));
    }

    @Override
    @Transactional
    public Result addComment(Long schemeId, Map<String, Object> body) {
        if (schemeId == null || schemeId <= 0) {
            return Result.fail(400, "方案 ID 错误");
        }
        if (body == null) body = new HashMap<>();

        Integer userId = asInt(body.get("userId"));
        String username = firstNonBlank(asString(body.get("username")), asString(body.get("userName")));
        if (userId == null && username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) userId = u.getId();
        }
        if (userId == null) {
            return Result.fail(400, "缺少 userId/username");
        }

        String content = asString(body.get("content"));
        if (content == null || content.trim().isBlank()) {
            return Result.fail(400, "评论内容不能为空");
        }

        Comment c = new Comment();
        c.setUserId(userId);
        c.setSchemeId(schemeId);
        c.setContent(content.trim());
        Integer parentId = asInt(body.get("parentId"));
        Integer rootId = asInt(body.get("rootId"));
        Integer floorLevel = asInt(body.get("floorLevel"));

        // 回复逻辑：若指定 parentId，则自动计算 rootId / floorLevel（与表结构一致）
        if (parentId != null && parentId > 0) {
            Comment parent = commentMapper.findById(parentId);
            if (parent == null || parent.getIsDeleted() != null && parent.getIsDeleted() == 1) {
                return Result.fail(400, "被回复的评论不存在");
            }
            if (parent.getSchemeId() == null || !parent.getSchemeId().equals(schemeId)) {
                return Result.fail(400, "被回复的评论不属于该方案");
            }
            int parentRoot = parent.getRootId() != null ? parent.getRootId() : 0;
            rootId = parentRoot > 0 ? parentRoot : parent.getId();
            int parentFloor = parent.getFloorLevel() != null ? parent.getFloorLevel() : 1;
            floorLevel = parentFloor + 1;
        }

        c.setParentId(parentId);
        c.setRootId(rootId);
        c.setFloorLevel(floorLevel);

        commentMapper.insert(c);
        return Result.success(Map.of("commentId", c.getId()));
    }

    @Override
    @Transactional
    public Result likeComment(Integer commentId, Map<String, Object> body) {
        if (commentId == null || commentId <= 0) {
            return Result.fail(400, "评论 ID 错误");
        }
        if (body == null) body = new HashMap<>();

        Integer userId = asInt(body.get("userId"));
        String username = firstNonBlank(asString(body.get("username")), asString(body.get("userName")));
        if (userId == null && username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) userId = u.getId();
        }
        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || (comment.getIsDeleted() != null && comment.getIsDeleted() == 1)) {
            return Result.fail(404, "评论不存在");
        }

        // 每个用户对同一条评论只能点赞一次（由唯一索引保证）
        int inserted = commentLikeMapper.insertIgnore(userId, commentId);
        if (inserted <= 0) {
            return Result.fail(409, "已点赞");
        }

        commentMapper.incLikeCount(commentId);
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result unlikeComment(Integer commentId, Map<String, Object> body) {
        if (commentId == null || commentId <= 0) {
            return Result.fail(400, "评论 ID 错误");
        }
        if (body == null) body = new HashMap<>();

        Integer userId = asInt(body.get("userId"));
        String username = firstNonBlank(asString(body.get("username")), asString(body.get("userName")));
        if (userId == null && username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) userId = u.getId();
        }
        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || (comment.getIsDeleted() != null && comment.getIsDeleted() == 1)) {
            return Result.fail(404, "评论不存在");
        }

        int deleted = commentLikeMapper.deleteByUserAndComment(userId, commentId);
        if (deleted <= 0) {
            return Result.fail(409, "未点赞");
        }

        commentMapper.decLikeCount(commentId);
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result updateComment(Long schemeId, Integer commentId, Map<String, Object> body) {
        if (schemeId == null || schemeId <= 0 || commentId == null || commentId <= 0) {
            return Result.fail(400, "参数错误");
        }
        if (body == null) body = new HashMap<>();

        Integer userId = asInt(body.get("userId"));
        String username = firstNonBlank(asString(body.get("username")), asString(body.get("userName")));
        if (userId == null && username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) userId = u.getId();
        }
        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || (comment.getIsDeleted() != null && comment.getIsDeleted() == 1)) {
            return Result.fail(404, "评论不存在");
        }
        if (comment.getSchemeId() == null || !comment.getSchemeId().equals(schemeId)) {
            return Result.fail(400, "评论不属于该方案");
        }
        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            return Result.fail(403, "无权编辑该评论");
        }

        String content = asString(body.get("content"));
        if (content == null || content.trim().isBlank()) {
            return Result.fail(400, "评论内容不能为空");
        }

        commentMapper.updateContent(commentId, content.trim());
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result deleteComment(Long schemeId, Integer commentId, Map<String, Object> body) {
        if (schemeId == null || schemeId <= 0 || commentId == null || commentId <= 0) {
            return Result.fail(400, "参数错误");
        }
        if (body == null) body = new HashMap<>();

        Integer userId = asInt(body.get("userId"));
        String username = firstNonBlank(asString(body.get("username")), asString(body.get("userName")));
        if (userId == null && username != null && !username.isBlank()) {
            User u = userMapper.findByUsername(username);
            if (u != null) userId = u.getId();
        }
        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        Comment comment = commentMapper.findById(commentId);
        if (comment == null || (comment.getIsDeleted() != null && comment.getIsDeleted() == 1)) {
            return Result.fail(404, "评论不存在");
        }
        if (comment.getSchemeId() == null || !comment.getSchemeId().equals(schemeId)) {
            return Result.fail(400, "评论不属于该方案");
        }
        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            return Result.fail(403, "无权删除该评论");
        }

        commentMapper.softDelete(commentId);
        return Result.success(null);
    }

    private static Integer asInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String asString(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.isBlank() ? null : s;
    }

    private static String firstNonBlank(String... candidates) {
        if (candidates == null) return null;
        for (String c : candidates) {
            if (c != null && !c.isBlank()) return c;
        }
        return null;
    }
}

