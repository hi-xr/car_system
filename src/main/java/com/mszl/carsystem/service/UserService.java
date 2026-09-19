package com.mszl.carsystem.service;

import com.mszl.carsystem.dao.pojo.User;
import com.mszl.carsystem.vo.Result;
import com.mszl.carsystem.vo.param.UpdateProfileParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author horry
 * @description TODO
 * @data 2026/3/2 23:28
 */
public interface UserService {
    User findUser(String username, String password, String role);

    User findByUsername(String username);

    int updateAvatarByUsername(String username, String avatar);

    int updateProfileByUsername(String username, String phone, String email, String address);

    // ================= 个人信息相关（原 UsersService） =================

    Result current(String usernameParam, String authHeader, HttpSession session, HttpServletRequest request);

    Result uploadAvatar(MultipartFile avatar,
                        String usernameParam,
                        String authHeader,
                        HttpSession session,
                        HttpServletRequest request) throws IOException;

    Result updateProfile(UpdateProfileParam body,
                         String usernameParam,
                         String authHeader,
                         HttpSession session);
}
