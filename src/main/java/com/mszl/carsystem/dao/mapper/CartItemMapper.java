package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartItemMapper {

    @Insert("""
            INSERT INTO cart_item
              (user_id, scheme_id, vehicle_name, config_summary,
               image_url, total_price, estimated_delivery, create_time)
            VALUES
              (#{userId}, #{schemeId}, #{vehicleName}, #{configSummary},
               #{imageUrl}, #{totalPrice}, #{estimatedDelivery}, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem item);

    @Select("""
            SELECT
              id,
              user_id AS userId,
              scheme_id AS schemeId,
              vehicle_name AS vehicleName,
              config_summary AS configSummary,
              image_url AS imageUrl,
              total_price AS totalPrice,
              estimated_delivery AS estimatedDelivery,
              create_time AS createTime
            FROM cart_item
            WHERE user_id = #{userId}
            ORDER BY create_time DESC
            """)
    List<CartItem> findByUserId(@Param("userId") Integer userId);

    @Delete("""
            DELETE FROM cart_item
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Integer userId);
}

