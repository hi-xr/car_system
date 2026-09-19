package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.CarModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CarModelMapper {

    @Select("""
            SELECT
              model_id AS modelId,
              model_name AS modelName,
              brand,
              guide_price AS guidePrice,
              production_year AS productionYear,
              power_type AS powerType,
              body_type AS bodyType,
              car_image AS carImage
            FROM car_model
            ORDER BY model_id
            """)
    List<CarModel> findAll();

    @Select("""
            SELECT
              model_id AS modelId,
              model_name AS modelName,
              brand,
              guide_price AS guidePrice,
              production_year AS ProductionYear,
              power_type AS powerType,
              body_type AS bodyType,
              car_image AS carImage
            FROM car_model
            WHERE model_id = #{id}
            LIMIT 1
            """)
    CarModel findById(Integer id);

    @Insert("""
            INSERT INTO car_model
              (model_name, brand, guide_price, production_year, power_type, body_type, car_image)
            VALUES
              (#{modelName}, #{brand}, #{guidePrice}, #{productionYear}, #{powerType}, #{bodyType}, #{carImage})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "modelId", keyColumn = "model_id")
    int insert(CarModel model);

    @Update("""
            UPDATE car_model
            SET model_name = #{modelName},
                brand = #{brand},
                guide_price = #{guidePrice},
                production_year = #{productionYear},
                power_type = #{powerType},
                body_type = #{bodyType},
                car_image = #{carImage}
            WHERE model_id = #{modelId}
            """)
    int update(CarModel model);

    @Delete("DELETE FROM car_model WHERE model_id = #{id}")
    int deleteById(@Param("id") Integer id);
}

