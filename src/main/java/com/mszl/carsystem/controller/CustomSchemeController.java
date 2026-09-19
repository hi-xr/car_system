package com.mszl.carsystem.controller;

import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.service.CommentService;
import com.mszl.carsystem.service.CustomSchemeService;
import com.mszl.carsystem.dao.mapper.CustomSchemeMapper;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.dao.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

// 示例：自定义方案
   @RestController
   @RequestMapping("/api/custom-schemes")
   public class CustomSchemeController {
       @Autowired
       private CustomSchemeService customSchemeService;

       @Autowired
       private CommentService commentService;

       @Autowired
       private UserMapper userMapper;

       @Autowired
       private CustomSchemeMapper customSchemeMapper;

       /**
        * 热门配置方案：返回所有已分享（公开）方案。
        * 前端首页会自行按点赞数做排序并截取展示。
        */
       @GetMapping("/popular")
       public Result popularSchemes() {
           return customSchemeService.list(null, true);
       }

       @GetMapping
       public Result list(@RequestParam(value = "username", required = false) String username,
                          @RequestParam(value = "onlyPublic", required = false) Boolean onlyPublic) {
           return customSchemeService.list(username, onlyPublic);
       }

       @PostMapping
       public Result create(@RequestBody Object body) {
           return customSchemeService.create(body);
       }

       @GetMapping("/{id}")
       public Result detail(@PathVariable Long id) {
           return customSchemeService.detail(id);
       }

       @PutMapping("/{id}")
       public Result update(@PathVariable Long id, @RequestBody Object body) {
           return customSchemeService.update(id, body);
       }

       @PostMapping("/{id}/share")
       public Result share(@PathVariable Long id) {
           return customSchemeService.share(id);
       }

       @PostMapping("/{id}/unshare")
       public Result unshare(@PathVariable Long id) {
           return customSchemeService.unshare(id);
       }

       @DeleteMapping("/{id}")
       public Result delete(@PathVariable Long id) {
           return customSchemeService.delete(id);
       }

       @PostMapping("/{id}/comments")
       public Result addComment(@PathVariable Long id, @RequestBody Map<String,Object> body) {
           return commentService.addComment(id, body);
       }

       @GetMapping("/{id}/comments")
       public Result listComments(@PathVariable Long id) {
           return commentService.listBySchemeId(id);
       }

       @PostMapping("/{schemeId}/comments/{commentId}/like")
       public Result likeComment(@PathVariable Long schemeId,
                                 @PathVariable Integer commentId,
                                 @RequestBody(required = false) Map<String, Object> body) {
           // schemeId 仅用于路由语义；实际校验在 service 内完成
           return commentService.likeComment(commentId, body);
       }

       @PostMapping("/{schemeId}/comments/{commentId}/unlike")
       public Result unlikeComment(@PathVariable Long schemeId,
                                   @PathVariable Integer commentId,
                                   @RequestBody(required = false) Map<String, Object> body) {
           return commentService.unlikeComment(commentId, body);
       }

       @PutMapping("/{schemeId}/comments/{commentId}")
       public Result updateComment(@PathVariable Long schemeId,
                                   @PathVariable Integer commentId,
                                   @RequestBody Map<String, Object> body) {
           return commentService.updateComment(schemeId, commentId, body);
       }

       /**
        * 兼容某些环境/代理不支持 PUT 的情况：用 POST 完成更新
        */
       @PostMapping("/{schemeId}/comments/{commentId}")
       public Result updateCommentViaPost(@PathVariable Long schemeId,
                                          @PathVariable Integer commentId,
                                          @RequestBody Map<String, Object> body) {
           return commentService.updateComment(schemeId, commentId, body);
       }

       @DeleteMapping("/{schemeId}/comments/{commentId}")
       public Result deleteComment(@PathVariable Long schemeId,
                                   @PathVariable Integer commentId,
                                   @RequestBody(required = false) Map<String, Object> body) {
           return commentService.deleteComment(schemeId, commentId, body == null ? Map.of() : body);
       }

       /**
        * 兼容某些环境/代理不支持 DELETE 的情况：用 POST 完成删除
        */
       @PostMapping("/{schemeId}/comments/{commentId}/delete")
       public Result deleteCommentViaPost(@PathVariable Long schemeId,
                                          @PathVariable Integer commentId,
                                          @RequestBody(required = false) Map<String, Object> body) {
           return commentService.deleteComment(schemeId, commentId, body == null ? Map.of() : body);
       }

       @PostMapping("/{id}/like")
       public Result like(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
           return customSchemeService.toggleLike(id, body);
       }

       /** 管理员：置顶/取消置顶热门方案 */
       @PostMapping("/{id}/pin")
       public Result pin(@PathVariable Long id,
                         @RequestParam(value = "username", required = false) String username,
                         @RequestBody(required = false) Map<String, Object> body) {
           String name = (username == null || username.isBlank()) ? "guest" : username;
           User u = userMapper.findByUsername(name);
           if (u == null || !isAdminRole(u.getRole())) {
               return Result.fail(403, "无权限：仅管理员可置顶方案");
           }
           boolean pinned = true;
           if (body != null && body.containsKey("pinned")) {
               Object v = body.get("pinned");
               if (v instanceof Boolean b) pinned = b;
               else pinned = "1".equals(String.valueOf(v)) || "true".equalsIgnoreCase(String.valueOf(v));
           }
           int updated = customSchemeMapper.updatePinned(id, pinned);
           if (updated <= 0) {
               return Result.fail(404, "方案不存在");
           }
           return Result.success(Map.of("schemeId", id, "pinned", pinned));
       }

       private static boolean isAdminRole(String roleRaw) {
           String role = Objects.toString(roleRaw, "").trim();
           return "admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role) || "管理员".equals(role);
       }
   }