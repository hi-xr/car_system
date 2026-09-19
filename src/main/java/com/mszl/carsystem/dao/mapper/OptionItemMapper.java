package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.OptionItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OptionItemMapper {

    /**
     * 从配置选项表读取所有记录。
     * 这里假设表名为 option_item，对应截图中的字段：
     *   item_id, category_id, item_name, option_price, stock_quantity, is_mandatory
     * 如果你的真实表名不同，请把 SQL 里的 option_item 改成实际表名。
     */
    @Select("""
            SELECT
              item_id       AS id,
              category_id   AS categoryId,
              item_name     AS name,
              option_price  AS price,
              stock_quantity AS stockQuantity,
              is_mandatory  AS mandatory
            FROM option_item
            """)
    List<OptionItem> findAll();

    @Select("""
            <script>
            SELECT
              item_id       AS id,
              category_id   AS categoryId,
              item_name     AS name,
              option_price  AS price,
              stock_quantity AS stockQuantity,
              is_mandatory  AS mandatory
            FROM option_item
            WHERE 1 = 1
              <if test="categoryId != null">
                AND category_id = #{categoryId}
              </if>
              <if test="keyword != null and keyword != ''">
                AND item_name LIKE CONCAT('%', #{keyword}, '%')
              </if>
            ORDER BY category_id, item_id
            </script>
            """)
    List<OptionItem> search(@Param("categoryId") Integer categoryId,
                            @Param("keyword") String keyword);

    @Select("""
            SELECT
              item_id       AS id,
              category_id   AS categoryId,
              item_name     AS name,
              option_price  AS price,
              stock_quantity AS stockQuantity,
              is_mandatory  AS mandatory
            FROM option_item
            WHERE item_id = #{id}
            LIMIT 1
            """)
    OptionItem findById(@Param("id") Long id);

    @Insert("""
            INSERT INTO option_item
              (category_id, item_name, option_price, stock_quantity, is_mandatory)
            VALUES
              (#{categoryId}, #{name}, #{price}, #{stockQuantity}, #{mandatory})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "item_id")
    int insert(OptionItem item);

    @Update("""
            UPDATE option_item
            SET category_id = #{categoryId},
                item_name = #{name},
                option_price = #{price},
                stock_quantity = #{stockQuantity},
                is_mandatory = #{mandatory}
            WHERE item_id = #{id}
            """)
    int update(OptionItem item);

    @Delete("DELETE FROM option_item WHERE item_id = #{id}")
    int deleteById(@Param("id") Long id);
}

