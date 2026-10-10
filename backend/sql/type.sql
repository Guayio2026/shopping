-- 商品分类表（type）
-- 在 Navicat 中：选中 shopping 库 -> 新建查询 -> 粘贴本文件全部内容 -> 运行

CREATE TABLE IF NOT EXISTS `type` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类';

-- 测试数据
INSERT INTO `type` (`name`, `sort_order`) VALUES
    ('数码', 1),
    ('服装', 2),
    ('食品', 3),
    ('家居', 4);
