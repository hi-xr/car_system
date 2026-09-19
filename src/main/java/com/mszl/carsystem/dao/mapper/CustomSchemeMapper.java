package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.CustomScheme;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CustomSchemeMapper {

    @Insert("""
            INSERT INTO custom_scheme
              (user_id, model_id, scheme_name, total_price, status, is_public,
               likes_count, copies_count, share_title, share_description, create_time)
            VALUES
              (#{userId}, #{modelId}, #{schemeName}, #{totalPrice}, #{status}, #{isPublic},
               #{likesCount}, #{copiesCount}, #{shareTitle}, #{shareDescription}, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "schemeId")
    int insert(CustomScheme scheme);

    @Select("""
            SELECT
              scheme_id AS schemeId,
              user_id AS userId,
              '' AS userName,
              model_id AS modelId,
              scheme_name AS schemeName,
              create_time AS createTime,
              total_price AS totalPrice,
              status,
              is_public AS isPublic,
              is_pinned AS pinned,
              likes_count AS likesCount,
              copies_count AS copiesCount,
              share_title AS shareTitle,
              share_description AS shareDescription
            FROM custom_scheme
            WHERE scheme_id = #{id}
            LIMIT 1
            """)
    CustomScheme findById(Long id);

    @Select("""
            SELECT
              cs.scheme_id AS schemeId,
              cs.user_id AS userId,
              u.username AS userName,
              cs.model_id AS modelId,
              cs.scheme_name AS schemeName,
              cs.create_time AS createTime,
              cs.total_price AS totalPrice,
              cs.status,
              cs.is_public AS isPublic,
              cs.is_pinned AS pinned,
              cs.likes_count AS likesCount,
              cs.copies_count AS copiesCount,
              cs.share_title AS shareTitle,
              cs.share_description AS shareDescription,
              cm.car_image AS carImage
            FROM custom_scheme cs
            LEFT JOIN car_model cm ON cm.model_id = cs.model_id
            LEFT JOIN user u ON u.id = cs.user_id
            ORDER BY cs.is_pinned DESC, cs.create_time DESC
            """)
    java.util.List<CustomScheme> findAll();

    @Select("""
            SELECT
              cs.scheme_id AS schemeId,
              cs.user_id AS userId,
              u.username AS userName,
              cs.model_id AS modelId,
              cs.scheme_name AS schemeName,
              cs.create_time AS createTime,
              cs.total_price AS totalPrice,
              cs.status,
              cs.is_public AS isPublic,
              cs.is_pinned AS pinned,
              cs.likes_count AS likesCount,
              cs.copies_count AS copiesCount,
              cs.share_title AS shareTitle,
              cs.share_description AS shareDescription,
              cm.car_image AS carImage
            FROM custom_scheme cs
            LEFT JOIN car_model cm ON cm.model_id = cs.model_id
            LEFT JOIN user u ON u.id = cs.user_id
            WHERE cs.user_id = #{userId}
            ORDER BY cs.is_pinned DESC, cs.create_time DESC
            """)
    java.util.List<CustomScheme> findByUserId(@Param("userId") Integer userId);

    @Select("""
            SELECT
              cs.scheme_id AS schemeId,
              cs.user_id AS userId,
              u.username AS userName,
              cs.model_id AS modelId,
              cs.scheme_name AS schemeName,
              cs.create_time AS createTime,
              cs.total_price AS totalPrice,
              cs.status,
              cs.is_public AS isPublic,
              cs.is_pinned AS pinned,
              cs.likes_count AS likesCount,
              cs.copies_count AS copiesCount,
              cs.share_title AS shareTitle,
              cs.share_description AS shareDescription,
              cm.car_image AS carImage
            FROM custom_scheme cs
            LEFT JOIN car_model cm ON cm.model_id = cs.model_id
            LEFT JOIN user u ON u.id = cs.user_id
            WHERE cs.is_public = 1
            ORDER BY cs.is_pinned DESC, cs.likes_count DESC, cs.create_time DESC
            """)
    java.util.List<CustomScheme> findPublic();

    /** 标配方案：被 car_model.default_scheme_id 引用的方案，用于热门配置 */
    @Select("""
            SELECT
              cs.scheme_id AS schemeId,
              cs.user_id AS userId,
              u.username AS userName,
              cs.model_id AS modelId,
              cs.scheme_name AS schemeName,
              cs.create_time AS createTime,
              cs.total_price AS totalPrice,
              cs.status,
              cs.is_public AS isPublic,
              cs.is_pinned AS pinned,
              cs.likes_count AS likesCount,
              cs.copies_count AS copiesCount,
              cs.share_title AS shareTitle,
              cs.share_description AS shareDescription,
              cm.car_image AS carImage
            FROM custom_scheme cs
            INNER JOIN car_model cm ON cm.default_scheme_id = cs.scheme_id
            LEFT JOIN user u ON u.id = cs.user_id
            """)
    java.util.List<CustomScheme> findDefaultSchemes();

    @Update("""
            UPDATE custom_scheme
            SET scheme_name = #{schemeName},
                total_price = #{totalPrice},
                share_title = #{shareTitle},
                share_description = #{shareDescription}
            WHERE scheme_id = #{schemeId}
            """)
    int updateBasic(CustomScheme scheme);

    @Update("""
            UPDATE custom_scheme
            SET is_public = #{isPublic}
            WHERE scheme_id = #{id}
            """)
    int updateIsPublic(@Param("id") Long id, @Param("isPublic") Boolean isPublic);

    @Update("""
            UPDATE custom_scheme
            SET is_pinned = #{pinned}
            WHERE scheme_id = #{id}
            """)
    int updatePinned(@Param("id") Long id, @Param("pinned") Boolean pinned);

    @Update("UPDATE custom_scheme SET likes_count = COALESCE(likes_count, 0) + 1 WHERE scheme_id = #{id}")
    int incLikesCount(@Param("id") Long id);

    @Update("UPDATE custom_scheme SET likes_count = GREATEST(0, COALESCE(likes_count, 0) - 1) WHERE scheme_id = #{id}")
    int decLikesCount(@Param("id") Long id);

    @Delete("DELETE FROM custom_scheme WHERE scheme_id = #{id}")
    int deleteById(@Param("id") Long id);
}

