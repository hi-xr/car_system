package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.SupportMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SupportMessageMapper {

    @Insert("""
            INSERT INTO support_message
              (order_id, order_no, user_id, username, content, create_time)
            VALUES
              (#{orderId}, #{orderNo}, #{userId}, #{username}, #{content}, #{createTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SupportMessage msg);

    @Select("""
            SELECT
              id,
              order_id   AS orderId,
              order_no   AS orderNo,
              user_id    AS userId,
              username,
              content,
              create_time AS createTime
            FROM support_message
            WHERE order_id = #{orderId}
            ORDER BY create_time DESC
            """)
    List<SupportMessage> findByOrderId(@Param("orderId") Long orderId);

    @Select("""
            SELECT
              id,
              order_id   AS orderId,
              order_no   AS orderNo,
              user_id    AS userId,
              username,
              content,
              create_time AS createTime
            FROM support_message
            ORDER BY create_time DESC
            """)
    List<SupportMessage> findAll();
}

