package com.mszl.carsystem.controller;

import com.mszl.carsystem.service.UserService;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.UpdateProfileParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 个人信息相关接口（前端 Profile.vue 已固定使用这些路径）
 */
@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UserService usersService;

    @GetMapping("/current")
    public Result current(HttpSession session,
                          @RequestParam(value = "username", required = false) String usernameParam,
                          HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return usersService.current(usernameParam, authHeader, session, request);
    }

    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                               HttpSession session,
                               HttpServletRequest request,
                               @RequestParam(value = "username", required = false) String usernameParam) throws IOException {
        String authHeader = request.getHeader("Authorization");
        return usersService.uploadAvatar(avatar, usernameParam, authHeader, session, request);
    }

    @PutMapping("/profile")
    public Result updateProfile(@RequestBody UpdateProfileParam body,
                                HttpSession session,
                                HttpServletRequest request,
                                @RequestParam(value = "username", required = false) String usernameParam) {
        String authHeader = request.getHeader("Authorization");
        return usersService.updateProfile(body, usernameParam, authHeader, session);
    }
}

