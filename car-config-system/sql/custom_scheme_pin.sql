-- custom_scheme 置顶字段（管理员可将热门方案置顶）
-- MySQL 8+

ALTER TABLE custom_scheme
  ADD COLUMN IF NOT EXISTS is_pinned TINYINT(1) NOT NULL DEFAULT 0;

-- 可选：加索引提升列表排序性能
CREATE INDEX IF NOT EXISTS idx_custom_scheme_pinned ON custom_scheme (is_pinned);

