package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("""
            INSERT INTO orders
              (user_id, order_no, create_time, status,
               scheme_id, vehicle_name, config_summary, estimated_delivery, total_amount)
            VALUES
              (#{userId}, #{orderNo}, #{orderTime}, #{status},
               #{schemeId}, #{vehicleName}, #{configSummary}, #{estimatedDelivery}, #{totalAmount})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("""
            SELECT
              id,
              user_id      AS userId,
              order_no     AS orderNo,
              create_time  AS orderTime,
              status,
              scheme_id    AS schemeId,
              vehicle_name AS vehicleName,
              config_summary AS configSummary,
              estimated_delivery AS estimatedDelivery,
              total_amount AS totalAmount
            FROM orders
            WHERE user_id = #{userId}
            ORDER BY create_time DESC
            """)
    List<Order> findByUserId(@Param("userId") Integer userId);

    @Select("""
            SELECT
              id,
              user_id      AS userId,
              order_no     AS orderNo,
              create_time  AS orderTime,
              status,
              scheme_id    AS schemeId,
              vehicle_name AS vehicleName,
              config_summary AS configSummary,
              estimated_delivery AS estimatedDelivery,
              total_amount AS totalAmount
            FROM orders
            WHERE user_id = #{userId}
              AND status = #{status}
            ORDER BY create_time DESC
            """)
    List<Order> findByUserIdAndStatus(@Param("userId") Integer userId,
                                      @Param("status") String status);

    @Select("""
            SELECT
              o.id,
              o.user_id      AS userId,
              o.order_no     AS orderNo,
              o.create_time  AS orderTime,
              o.status       AS status,
              o.scheme_id    AS schemeId,
              o.vehicle_name AS vehicleName,
              o.config_summary AS configSummary,
              o.estimated_delivery AS estimatedDelivery,
              o.total_amount AS totalAmount,
              u.username     AS username,
              u.phone        AS phone,
              u.email        AS email,
              u.address      AS address
            FROM orders o
            LEFT JOIN user u ON u.id = o.user_id
            ORDER BY o.create_time DESC
            """)
    List<Map<String, Object>> findAllWithUserInfo();

    @Select("""
            SELECT
              o.id,
              o.user_id      AS userId,
              o.order_no     AS orderNo,
              o.create_time  AS orderTime,
              o.status       AS status,
              o.scheme_id    AS schemeId,
              o.vehicle_name AS vehicleName,
              o.config_summary AS configSummary,
              o.estimated_delivery AS estimatedDelivery,
              o.total_amount AS totalAmount,
              u.username     AS username,
              u.phone        AS phone,
              u.email        AS email,
              u.address      AS address
            FROM orders o
            LEFT JOIN user u ON u.id = o.user_id
            WHERE o.status = #{status}
            ORDER BY o.create_time DESC
            """)
    List<Map<String, Object>> findAllWithUserInfoByStatus(@Param("status") String status);

    @Update("""
            UPDATE orders
            SET status = #{status}
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}