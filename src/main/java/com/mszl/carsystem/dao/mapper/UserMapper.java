package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户表 Mapper
 */
@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, avatar, role, employee_id AS employeeId,
                   phone, email, password, address
            FROM user
            WHERE username = #{username}
              AND password = #{password}
              AND role = #{role}
            LIMIT 1
            """)
    User findByUsernameAndPasswordAndRole(@Param("username") String username,
                                          @Param("password") String password,
                                          @Param("role") String role);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Select("""
            SELECT id, username, avatar, role, employee_id AS employeeId,
                   phone, email, address
            FROM user
            WHERE username = #{username}
            LIMIT 1
            """)
    User findByUsername(@Param("username") String username);

    @Select("SELECT id, username, avatar, role, employee_id AS employeeId, phone, email, address FROM user WHERE id = #{id} LIMIT 1")
    User findById(@Param("id") Integer id);

    @Update("UPDATE user SET avatar = #{avatar} WHERE username = #{username}")
    int updateAvatarByUsername(@Param("username") String username, @Param("avatar") String avatar);

    @Update("""
            UPDATE user
            SET phone = #{phone},
                email = #{email},
                address = #{address}
            WHERE username = #{username}
            """)
    int updateProfileByUsername(@Param("username") String username,
                                @Param("phone") String phone,
                                @Param("email") String email,
                                @Param("address") String address);

    @Update("UPDATE user SET username = #{newUsername} WHERE username = #{oldUsername}")
    int updateUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);

    @Insert("""
            INSERT INTO user (username, phone, role, employee_id, password, address)
            VALUES (#{username}, #{phone}, #{role}, #{employeeId}, #{password}, #{address})
            """)
    int insertUser(@Param("username") String username,
                   @Param("phone") String phone,
                   @Param("role") String role,
                   @Param("employeeId") String employeeId,
                   @Param("password") String password,
                   @Param("address") String address);
}
