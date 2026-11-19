-- 仅用于导入基础表后的新库。
ALTER TABLE `user` ADD account DECIMAL(18,2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS `type` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类';

CREATE TABLE IF NOT EXISTS `notice` (
  `id`      int(11)      NOT NULL AUTO_INCREMENT                 COMMENT 'ID',
  `name`    varchar(255) DEFAULT NULL                            COMMENT '公告标题',
  `content` text                                                 COMMENT '公告内容',
  `time`    varchar(255) DEFAULT NULL                            COMMENT '创建时间(字符串,格式 yyyy-MM-dd HH:mm:ss)',
  `user_id` int(11)      DEFAULT NULL                            COMMENT '创建人ID(admin.id)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE goods (
    id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL,
    descr TEXT, content LONGTEXT, cover VARCHAR(512),
    price DECIMAL(18,2) NOT NULL DEFAULT 0, store INT NOT NULL DEFAULT 0,
    user_id INT, date VARCHAR(32), type_id INT, state VARCHAR(32), sales INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), order_no VARCHAR(64) NOT NULL UNIQUE,
    goods_id INT NOT NULL, price DECIMAL(18,2) NOT NULL, nums INT NOT NULL,
    user_name VARCHAR(255), user_phone VARCHAR(64), user_address TEXT, time VARCHAR(32),
    state VARCHAR(32) NOT NULL DEFAULT '待付款', user_id BIGINT, user_hidden TINYINT NOT NULL DEFAULT 0,
    INDEX idx_orders_user_visible (user_id, user_hidden, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE collect (
    id INT PRIMARY KEY AUTO_INCREMENT, goods_id INT NOT NULL, user_id BIGINT NOT NULL,
    time VARCHAR(32), UNIQUE KEY (user_id, goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE carousel (
    id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), cover VARCHAR(512), goods_id INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
