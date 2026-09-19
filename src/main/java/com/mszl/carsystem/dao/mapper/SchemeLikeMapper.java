package com.mszl.carsystem.dao.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SchemeLikeMapper {

    /**
     * 插入点赞记录（依赖 (user_id, scheme_id) 唯一索引）。
     * MySQL: INSERT IGNORE 若重复会返回 0
     */
    @Insert("INSERT IGNORE INTO scheme_like (user_id, scheme_id) VALUES (#{userId}, #{schemeId})")
    int insertIgnore(@Param("userId") Integer userId, @Param("schemeId") Long schemeId);

    @Delete("DELETE FROM scheme_like WHERE user_id = #{userId} AND scheme_id = #{schemeId}")
    int deleteByUserAndScheme(@Param("userId") Integer userId, @Param("schemeId") Long schemeId);

    @Select("SELECT COUNT(*) FROM scheme_like WHERE user_id = #{userId} AND scheme_id = #{schemeId}")
    int countByUserAndScheme(@Param("userId") Integer userId, @Param("schemeId") Long schemeId);
}

