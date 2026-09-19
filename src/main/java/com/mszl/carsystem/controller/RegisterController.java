package com.mszl.carsystem.controller;

import com.mszl.carsystem.service.RegisterService;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.UserParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册接口：接收前端注册表单，返回统一 Result
 */
@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping("register")
    public Result register(@RequestBody UserParam userParam) {
        return registerService.register(userParam);
    }
}
