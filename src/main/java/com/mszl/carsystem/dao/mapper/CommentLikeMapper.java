package com.mszl.carsystem.dao.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface CommentLikeMapper {

    @Select("SELECT COUNT(*) FROM comment_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int countByUserAndComment(@Param("userId") Integer userId, @Param("commentId") Integer commentId);

    /**
     * 插入点赞记录。依赖 (user_id, comment_id) 唯一索引。
     * MySQL: INSERT IGNORE 若重复会返回 0
     */
    @Insert("INSERT IGNORE INTO comment_like (user_id, comment_id) VALUES (#{userId}, #{commentId})")
    int insertIgnore(@Param("userId") Integer userId, @Param("commentId") Integer commentId);

    @Delete("DELETE FROM comment_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int deleteByUserAndComment(@Param("userId") Integer userId, @Param("commentId") Integer commentId);
}

