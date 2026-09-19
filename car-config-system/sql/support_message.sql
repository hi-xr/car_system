-- 订单留言（普通用户 -> 管理员）
-- 如果你用的是 MySQL 8+

CREATE TABLE IF NOT EXISTS support_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(64) DEFAULT '',
  user_id INT NOT NULL,
  username VARCHAR(64) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  create_time DATETIME NOT NULL,
  INDEX idx_support_message_order_id (order_id),
  INDEX idx_support_message_user_id (user_id),
  INDEX idx_support_message_create_time (create_time)
);

