-- 配置规则表
-- 用于存储选配时的互斥、依赖、兼容等规则

CREATE TABLE IF NOT EXISTS config_rule (
  rule_id       INT PRIMARY KEY AUTO_INCREMENT COMMENT '规则主键',
  rule_type     VARCHAR(20) NOT NULL COMMENT '规则类型: mutex互斥 / depend依赖 / compatible兼容',
  item_a_id     INT NOT NULL COMMENT '配置项A (关联 optionitem.item_id)',
  item_b_id     INT NOT NULL COMMENT '配置项B (关联 optionitem.item_id)',
  series        VARCHAR(50) DEFAULT NULL COMMENT '适用车系，空表示全车系',
  model         VARCHAR(50) DEFAULT NULL COMMENT '适用车型，空表示全车型',
  is_enabled    TINYINT(1) DEFAULT 1 COMMENT '是否启用: 0禁用 1启用',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_rule_type (rule_type),
  INDEX idx_item_a (item_a_id),
  INDEX idx_item_b (item_b_id),
  INDEX idx_series_model (series, model),
  INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置规则表';
