package com.mszl.carsystem.service;

import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 22:59
 */

public interface LoginService {


    Result login(LoginParam loginParam);
}
