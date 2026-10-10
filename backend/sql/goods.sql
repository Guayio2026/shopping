-- ============================================================================
--  商品模块建表脚本（完整版：主表 + 多图 + 规格）
--
--  在 Navicat 中：选中 shopping 库 → 新建查询 → 粘贴本文件全部内容 → 运行
--  或者命令行：mysql -uroot -p你的密码 -D shopping < backend/sql/goods.sql
--
--  【为什么是三张表】
--  一个商品可以有「多张图」和「多个规格」，这是典型的一对多关系。
--  如果都塞进 goods 一张表，就会出现：
--    id | name   | image1       | image2       | sku1        | sku2
--    1  | 手机    | /a.png       | /b.png       | 蓝色/128G   | 黑色/256G
--  行数会爆炸（图 3 张 × 规格 4 个 = 12 行），而且改一个名字要改 12 行。
--  所以拆成：goods（主）+ goods_image（图）+ goods_sku（规格），
--  子表靠 goods_id 指回主表，这就是「一对多」。
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1. 商品主表 goods
--    【和前端的关系】这里每一列，最终都会变成前端表格的一列
--    数据库列         →  Java 字段      →  前端表格怎么用
--    ------------------------------------------------------------
--    name            →  name          →  prop="name"        商品名称
--    cover           →  cover         →  <el-image> 缩略图（还要配 mediaUrl() 处理地址）
--    price           →  price         →  prop="price"       价格
--    stock           →  stock         →  prop="stock"       库存
--    type_id         →  typeId        →  prop="typeName"     显示分类名（不是 id，靠 JOIN 查出来）
--    description     →  description   →  详情弹窗里的描述
--    status          →  status        →  <el-tag> 上架/下架
--    sort_order      →  sortOrder     →  prop="sortOrder"   排序
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `goods` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(128)  NOT NULL                COMMENT '商品名称',
    `cover`       VARCHAR(255)  DEFAULT NULL            COMMENT '封面图地址（如 /uploads/xxx.png）',
    `price`       DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '售价（用 DECIMAL 不用 FLOAT，避免小数精度丢失）',
    `stock`       INT           NOT NULL DEFAULT 0      COMMENT '总库存',
    `type_id`     BIGINT        DEFAULT NULL            COMMENT '所属分类 → type.id',
    `description` TEXT                                  COMMENT '商品描述',
    `status`      TINYINT       NOT NULL DEFAULT 1      COMMENT '1=上架 0=下架',
    `sort_order`  INT           NOT NULL DEFAULT 0      COMMENT '排序值（越小越靠前）',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    -- 加索引：列表页经常「按分类筛选」，没索引就会全表扫描（数据多了很慢）
    KEY `idx_type_id` (`type_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品';


-- ----------------------------------------------------------------------------
-- 2. 商品多图子表 goods_image
--    【关系】goods_image.goods_id → goods.id（一个商品 N 张图）
--    【图存在哪】只存「地址」，不存图片本身。图片文件在 backend/uploads/ 目录，
--                数据库里存的是 /uploads/xxx.png 这种路径。
--    【为什么要 sort_order】图的顺序要能调（第一张往往当主图/详情首图）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `goods_image` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id`   BIGINT       NOT NULL                COMMENT '所属商品 → goods.id',
    `url`        VARCHAR(255) NOT NULL                COMMENT '图片地址',
    `sort_order` INT          NOT NULL DEFAULT 0      COMMENT '展示顺序',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品图片（一对多）';


-- ----------------------------------------------------------------------------
-- 3. 商品规格子表 goods_sku
--    【关系】goods_sku.goods_id → goods.id（一个商品 N 个规格）
--    【本项目的简化】真正的电商会拆成「规格 + 规格值 + SKU 组合表」（颜色×尺寸的笛卡尔积），
--                    那套要 3 张表。这里简化成「一行一个规格」，规格名直接写成一个字符串
--                    （如 "蓝色 / 128G"），够用且好懂。
--    【为什么要有自己独立的 price/stock】同一个商品，不同规格价格和库存都可能不一样
--                    （蓝色贵 100、XL 码只剩 3 件）。总库存 goods.stock 是给列表页看的概览。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `goods_sku` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id`   BIGINT        NOT NULL                COMMENT '所属商品 → goods.id',
    `spec_name`  VARCHAR(64)   NOT NULL                COMMENT '规格名，如 "蓝色 / 128G"',
    `price`      DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '该规格售价',
    `stock`      INT           NOT NULL DEFAULT 0      COMMENT '该规格库存',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品规格（一对多）';


-- ============================================================================
--  测试数据
--  【注意】type_id 用的是变量。因为你的 type 表 id 是 10~15（不是 1~4），
--         直接写死数字很容易对不上。用「按名字反查 id」就不会错。
-- ============================================================================

-- 按名字取出分类 id，存进变量备用
SET @t_digital = (SELECT `id` FROM `type` WHERE `name` = '数码' LIMIT 1);
SET @t_cloth   = (SELECT `id` FROM `type` WHERE `name` = '服装' LIMIT 1);
SET @t_food    = (SELECT `id` FROM `type` WHERE `name` = '食品' LIMIT 1);

-- 3 个商品（图片用的是 uploads 目录里已存在的文件，所以页面上能真的看到图）
INSERT INTO `goods` (`name`, `cover`, `price`, `stock`, `type_id`, `description`, `status`, `sort_order`) VALUES
    ('真无线蓝牙耳机 Pro', '/uploads/c0b9b481-1c5a-402a-9f4b-0d8505a56df4.png', 399.00,  120, @t_digital, '主动降噪，续航 36 小时，支持无线充电。', 1, 1),
    ('纯棉圆领短袖 T 恤',   '/uploads/3fc74587-ef7a-4e08-825c-a56f9941f84e.jpg', 89.00,   500, @t_cloth,   '100% 新疆长绒棉，四季可穿，不起球。',     1, 2),
    ('每日坚果混合装 750g', '/uploads/734cd68b-aba7-49ea-9fcb-888856295e2a.jpg', 128.00,  60,  @t_food,    '7 种坚果 + 3 种果干，独立小包装。',       0, 3);

-- 反查刚才插入的三个商品 id
SET @g1 = (SELECT `id` FROM `goods` WHERE `name` = '真无线蓝牙耳机 Pro' LIMIT 1);
SET @g2 = (SELECT `id` FROM `goods` WHERE `name` = '纯棉圆领短袖 T 恤'   LIMIT 1);
SET @g3 = (SELECT `id` FROM `goods` WHERE `name` = '每日坚果混合装 750g' LIMIT 1);

-- 多图：第一个商品 3 张，另外两个各 2 张
INSERT INTO `goods_image` (`goods_id`, `url`, `sort_order`) VALUES
    (@g1, '/uploads/c0b9b481-1c5a-402a-9f4b-0d8505a56df4.png', 1),
    (@g1, '/uploads/0797fcb5-e477-4b11-878d-f48334af7bc6.svg', 2),
    (@g1, '/uploads/28d96676-5354-4f45-84de-9558d35f331a.svg', 3),
    (@g2, '/uploads/3fc74587-ef7a-4e08-825c-a56f9941f84e.jpg', 1),
    (@g2, '/uploads/93970236-886d-40bb-b07c-dc29c4edffc7.svg', 2),
    (@g3, '/uploads/734cd68b-aba7-49ea-9fcb-888856295e2a.jpg', 1),
    (@g3, '/uploads/fc69d5d4-3eb8-483f-b846-875a0bec20bc.svg', 2);

-- 规格：每个商品 2~3 个
INSERT INTO `goods_sku` (`goods_id`, `spec_name`, `price`, `stock`) VALUES
    (@g1, '白色 / 标准版', 399.00, 80),
    (@g1, '黑色 / 标准版', 399.00, 30),
    (@g1, '黑色 / 降噪版', 499.00, 10),
    (@g2, '白色 / M',       89.00, 200),
    (@g2, '白色 / L',       89.00, 180),
    (@g2, '黑色 / XL',      99.00, 120),
    (@g3, '750g 装',       128.00, 45),
    (@g3, '1500g 装',      238.00, 15);


-- ============================================================================
--  验证：三张表都查到数据就算成功
-- ============================================================================
SELECT 'goods'       AS 表, COUNT(*) AS 条数 FROM `goods`
UNION ALL SELECT 'goods_image', COUNT(*) FROM `goods_image`
UNION ALL SELECT 'goods_sku',   COUNT(*) FROM `goods_sku`;
