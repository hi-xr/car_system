package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.Accessory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccessoryMapper {

    @Select("SELECT id, name, category, stock_qty AS stockQty, price, required FROM accessory")
    List<Accessory> findAll();

    @Insert("""
            INSERT INTO accessory (name, category, stock_qty, price, required)
            VALUES (#{name}, #{category}, #{stockQty}, #{price}, #{required})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Accessory accessory);

    @Update("""
            UPDATE accessory
            SET name = #{name},
                category = #{category},
                stock_qty = #{stockQty},
                price = #{price},
                required = #{required}
            WHERE id = #{id}
            """)
    int update(Accessory accessory);

    @Delete("DELETE FROM accessory WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}

