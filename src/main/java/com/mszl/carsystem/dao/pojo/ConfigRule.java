package com.mszl.carsystem.dao.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("config_rule")
public class ConfigRule {
    private Long ruleId;
    private String ruleType;   // mutex / depend / compatible
    private Long itemAId;
    private Long itemBId;
    private String series;
    private String model;
    private Boolean enabled;   // is_enabled
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

