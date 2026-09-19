package com.mszl.carsystem.controller;

import com.mszl.carsystem.service.LoginService;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 22:58
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    @Autowired
    private LoginService loginservice;
    @PostMapping("/login")
    public Result login(@RequestBody LoginParam loginParam, HttpSession session){
        Result result = loginservice.login(loginParam);
        if (result != null && result.isSuccess()) {
            Object raw = result.getData();
            if (raw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) raw;
                Object username = data.get("username");
                Object role = data.get("role");
                if (username != null) session.setAttribute("username", String.valueOf(username));
                if (role != null) session.setAttribute("role", String.valueOf(role));
                session.setAttribute("currentUser", data);
            }
        }
        return result;
    }
}
