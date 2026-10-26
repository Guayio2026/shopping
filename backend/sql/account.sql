-- ============================================================
-- 第十章：账户余额（account）模块
-- 在 shopping 库执行一次即可（MySQL 5.7 / 8.0 均可）
-- ============================================================

-- 1. user 表新增 account 列（账户余额），默认值 0
--    NOT NULL       : 避免 NULL 参与加法后结果还是 NULL（钱算错还不报错）
--    DEFAULT 0      : 老用户 ALTER 后自动补 0；新用户 insert 不写这列也是 0
--    AFTER `balance`: 仅为了列顺序好看，不影响功能
--    ⚠️ 本项目早就有 balance（decimal），本模块按教程另立 account，
--      代码里只用 account 走「充值 / 支付」，balance 暂不参与
ALTER TABLE `user`
    ADD COLUMN `account` double NOT NULL DEFAULT 0 COMMENT '账户余额(教程命名,勿与accountType账号类型混淆)'
    AFTER `balance`;

-- 2. 核对（应看到 account 一行，Type=double，Default=0）
-- SHOW COLUMNS FROM `user` LIKE 'account';

-- 3. 核对余额（充完值后看一眼）
-- SELECT id, username, account FROM `user`;
