package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.Comment;
import com.mszl.carsystem.vo.CommentView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("""
            SELECT
              c.id AS commentId,
              c.user_id AS userId,
              u.username AS userName,
              u.avatar AS avatar,
              c.scheme_id AS schemeId,
              c.content AS content,
              c.parent_id AS parentId,
              c.root_id AS rootId,
              c.floor_level AS floorLevel,
              c.like_count AS likeCount,
              c.create_time AS createdAt
            FROM `comment` c
            LEFT JOIN user u ON u.id = c.user_id
            WHERE c.scheme_id = #{schemeId}
              AND (c.is_deleted IS NULL OR c.is_deleted = 0)
            ORDER BY c.create_time DESC, c.id DESC
            """)
    List<CommentView> findBySchemeId(@Param("schemeId") Long schemeId);

    @Select("""
            SELECT
              id,
              user_id AS userId,
              scheme_id AS schemeId,
              content,
              parent_id AS parentId,
              root_id AS rootId,
              floor_level AS floorLevel,
              like_count AS likeCount,
              create_time AS createTime,
              update_time AS updateTime,
              is_deleted AS isDeleted
            FROM `comment`
            WHERE id = #{id}
            LIMIT 1
            """)
    Comment findById(@Param("id") Integer id);

    @Insert("""
            INSERT INTO `comment` (user_id, scheme_id, content, parent_id, root_id, floor_level, like_count, is_deleted)
            VALUES (#{userId}, #{schemeId}, #{content},
                    COALESCE(#{parentId}, 0),
                    COALESCE(#{rootId}, 0),
                    COALESCE(#{floorLevel}, 1),
                    0,
                    0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Update("UPDATE `comment` SET like_count = like_count + 1 WHERE id = #{id} AND (is_deleted IS NULL OR is_deleted = 0)")
    int incLikeCount(@Param("id") Integer id);

    @Update("UPDATE `comment` SET like_count = GREATEST(0, like_count - 1) WHERE id = #{id} AND (is_deleted IS NULL OR is_deleted = 0)")
    int decLikeCount(@Param("id") Integer id);

    @Update("""
            UPDATE `comment`
            SET content = #{content}, update_time = NOW()
            WHERE id = #{id} AND (is_deleted IS NULL OR is_deleted = 0)
            """)
    int updateContent(@Param("id") Integer id, @Param("content") String content);

    @Update("UPDATE `comment` SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND (is_deleted IS NULL OR is_deleted = 0)")
    int softDelete(@Param("id") Integer id);
}

