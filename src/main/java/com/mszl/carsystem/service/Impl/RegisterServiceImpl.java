package com.mszl.carsystem.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.mszl.carsystem.dao.mapper.UserMapper;
import com.mszl.carsystem.service.RegisterService;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.UserParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 注册服务实现：校验参数、检查用户名是否已存在、写入 user 表
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result register(UserParam userParam) {
        String username = userParam.getUsername();
        String password = userParam.getPassword();
        String role = userParam.getRole();
        String phone = userParam.getPhone();
        String address = userParam.getAddress() != null ? userParam.getAddress() : "";
        String employeeId = userParam.getEmployeeId() != null ? userParam.getEmployeeId() : "";

        if (StringUtils.isBlank(username)) {
            return Result.fail(400, "用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return Result.fail(400, "密码不能为空");
        }
        if (StringUtils.isBlank(role)) {
            return Result.fail(400, "请选择角色");
        }
        if (StringUtils.isBlank(phone)) {
            return Result.fail(400, "电话号码不能为空");
        }

        if (userMapper.countByUsername(username) > 0) {
            return Result.fail(400, "用户名已存在");
        }

        int rows = userMapper.insertUser(username, phone, role, employeeId, password, address);
        if (rows <= 0) {
            return Result.fail(500, "注册失败，请稍后重试");
        }
        return Result.success(null);
    }
}
