package com.mszl.carsystem.dao.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 22:34
 */
@Data
@TableName("user")
public class User {

    private Integer id;

    private String username;

    private String avatar;

    private String role;

    private String employeeId;

    private String phone;

    private String email;

    private String password;

    private String address;
}
