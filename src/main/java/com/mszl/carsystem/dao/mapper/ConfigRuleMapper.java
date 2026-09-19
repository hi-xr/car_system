package com.mszl.carsystem.dao.mapper;

import com.mszl.carsystem.dao.pojo.ConfigRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigRuleMapper {

    @Select("""
            SELECT
              rule_id    AS ruleId,
              rule_type  AS ruleType,
              item_a_id  AS itemAId,
              item_b_id  AS itemBId,
              series,
              model,
              is_enabled AS enabled,
              created_at AS createdAt,
              updated_at AS updatedAt
            FROM config_rule
            ORDER BY rule_id DESC
            """)
    List<ConfigRule> findAll();

    @Select("""
            <script>
            SELECT
              rule_id    AS ruleId,
              rule_type  AS ruleType,
              item_a_id  AS itemAId,
              item_b_id  AS itemBId,
              series,
              model,
              is_enabled AS enabled,
              created_at AS createdAt,
              updated_at AS updatedAt
            FROM config_rule
            WHERE 1=1
              <if test="ruleType != null and ruleType != ''">
                AND rule_type = #{ruleType}
              </if>
              <if test="series != null and series != ''">
                AND (series IS NULL OR series = '' OR series = #{series})
              </if>
              <if test="model != null and model != ''">
                AND (model IS NULL OR model = '' OR model = #{model})
              </if>
              <if test="enabled != null">
                AND is_enabled = #{enabled}
              </if>
              <if test="itemAId != null">
                AND item_a_id = #{itemAId}
              </if>
              <if test="itemBId != null">
                AND item_b_id = #{itemBId}
              </if>
            ORDER BY rule_id DESC
            </script>
            """)
    List<ConfigRule> query(@Param("ruleType") String ruleType,
                           @Param("series") String series,
                           @Param("model") String model,
                           @Param("enabled") Boolean enabled,
                           @Param("itemAId") Long itemAId,
                           @Param("itemBId") Long itemBId);

    @Select("""
            SELECT
              rule_id    AS ruleId,
              rule_type  AS ruleType,
              item_a_id  AS itemAId,
              item_b_id  AS itemBId,
              series,
              model,
              is_enabled AS enabled,
              created_at AS createdAt,
              updated_at AS updatedAt
            FROM config_rule
            WHERE rule_id = #{id}
            LIMIT 1
            """)
    ConfigRule findById(@Param("id") Long id);

    @Insert("""
            INSERT INTO config_rule
              (rule_type, item_a_id, item_b_id, series, model, is_enabled)
            VALUES
              (#{ruleType}, #{itemAId}, #{itemBId}, #{series}, #{model}, #{enabled})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "ruleId", keyColumn = "rule_id")
    int insert(ConfigRule rule);

    @Update("""
            UPDATE config_rule
            SET rule_type = #{ruleType},
                item_a_id = #{itemAId},
                item_b_id = #{itemBId},
                series = #{series},
                model = #{model},
                is_enabled = #{enabled}
            WHERE rule_id = #{ruleId}
            """)
    int update(ConfigRule rule);

    @Delete("DELETE FROM config_rule WHERE rule_id = #{id}")
    int deleteById(@Param("id") Long id);
}

