-- ============================================================
-- 第十一章：公告（notice）模块
-- 在 shopping 库执行一次即可（MySQL 5.7 / 8.0 均可）
-- 全部语句都做了幂等处理：重复执行不会报「表已存在」「菜单重复」
-- ============================================================

-- ------------------------------------------------------------
-- 1. 建表
--    time 用 varchar：教程原样，配合 yyyy-MM-dd HH:mm:ss 时字典序等于时间序
--    user_id：存的是 admin 表的 id（本项目 admin 和 user 是两套账号，
--             公告只有管理员能发，所以这里不是顾客 id）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notice` (
  `id`      int(11)      NOT NULL AUTO_INCREMENT                 COMMENT 'ID',
  `name`    varchar(255) DEFAULT NULL                            COMMENT '公告标题',
  `content` text                                                 COMMENT '公告内容',
  `time`    varchar(255) DEFAULT NULL                            COMMENT '创建时间(字符串,格式 yyyy-MM-dd HH:mm:ss)',
  `user_id` int(11)      DEFAULT NULL                            COMMENT '创建人ID(admin.id)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';


-- ------------------------------------------------------------
-- 2. 后台菜单：商城管理 → 公告管理
--    parent_id 用子查询取「商城管理」的 id，不写死 9 —— 换台机器 id 可能不同
--    sort_order = 2：插在「分类管理(1)」和「商品管理(3)」之间的空档
-- ------------------------------------------------------------
INSERT INTO sys_menu (parent_id, title, path, icon, sort_order, status)
SELECT (SELECT id FROM sys_menu WHERE title = '商城管理' AND parent_id = 0 LIMIT 1),
       '公告管理', '/admin/notices', 'Bell', 2, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/notices');


-- ------------------------------------------------------------
-- 3. 菜单改名：轮播图2 → 轮播图
--    当初叫「轮播图2」是为了和已有的「轮播图管理」区分，名字太怪，改回正常叫法
--    注意：改完以后「轮播图」(/admin/carousels) 和「轮播图管理」(/admin/banners)
--         仍然并存，是两个不同的模块（前者 carousel 表，后者 home_banner 表）
-- ------------------------------------------------------------
UPDATE sys_menu
SET title = '轮播图'
WHERE path = '/admin/carousels'
  AND title = '轮播图2';


-- ------------------------------------------------------------
-- 4. 验证（执行完可以跑一下看结果）
-- ------------------------------------------------------------
-- SHOW CREATE TABLE notice;
-- SELECT id, parent_id, title, path, icon, sort_order
-- FROM sys_menu WHERE parent_id = (SELECT id FROM sys_menu WHERE title='商城管理' AND parent_id=0)
-- ORDER BY sort_order;
