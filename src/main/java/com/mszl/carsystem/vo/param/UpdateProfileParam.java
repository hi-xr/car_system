package com.mszl.carsystem.vo.param;

import lombok.Data;

@Data
public class UpdateProfileParam {
    private String username;
    private String phone;
    private String email;
    private String address;
}

