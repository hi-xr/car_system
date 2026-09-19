package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.vo.SchemeOptionItemView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface SchemeOptionMapper {

    @Insert("""
            <script>
            INSERT INTO scheme_option (scheme_id, item_id)
            VALUES
            <foreach collection="itemIds" item="itemId" separator=",">
              (#{schemeId}, #{itemId})
            </foreach>
            </script>
            """)
    int batchInsert(@Param("schemeId") Long schemeId, @Param("itemIds") List<Long> itemIds);

    @Select("""
            SELECT
              oi.item_id AS id,
              oi.category_id AS categoryId,
              oi.item_name AS name,
              oi.option_price AS price,
              oi.is_mandatory AS mandatory
            FROM scheme_option so
            JOIN option_item oi ON oi.item_id = so.item_id
            WHERE so.scheme_id = #{schemeId}
            ORDER BY oi.category_id, oi.item_id
            """)
    List<SchemeOptionItemView> findItemsBySchemeId(@Param("schemeId") Long schemeId);

    @Delete("DELETE FROM scheme_option WHERE scheme_id = #{schemeId}")
    int deleteBySchemeId(@Param("schemeId") Long schemeId);
}

