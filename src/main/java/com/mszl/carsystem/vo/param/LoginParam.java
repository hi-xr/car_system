package com.mszl.carsystem.vo.param;

import lombok.Data;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 23:10
 */
@Data
public class LoginParam {
    private String username;
    private String password;
    private String role;
    private String employeeId;
    private Boolean remember;
}
