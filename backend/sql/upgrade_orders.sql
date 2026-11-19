-- 在已有商城库停写期间执行。NULL 标记尚未校正销量的旧订单，支持失败后重跑。
SET NAMES utf8mb4;
ALTER TABLE goods MODIFY price DECIMAL(18,2) NOT NULL DEFAULT 0;
ALTER TABLE orders MODIFY price DECIMAL(18,2) NOT NULL;
ALTER TABLE `user` MODIFY account DECIMAL(18,2) NOT NULL DEFAULT 0;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'user_hidden'),
    'SELECT 1', 'ALTER TABLE orders ADD user_hidden TINYINT NULL DEFAULT NULL');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

START TRANSACTION;
UPDATE goods g JOIN (
    SELECT goods_id, SUM(nums) AS reserved FROM orders
    WHERE user_hidden IS NULL AND state = '待付款' GROUP BY goods_id
) p ON p.goods_id = g.id
SET g.sales = GREATEST(0, COALESCE(g.sales, 0) - p.reserved);
UPDATE orders SET user_hidden = 0 WHERE user_hidden IS NULL;
COMMIT;
ALTER TABLE orders MODIFY user_hidden TINYINT NOT NULL DEFAULT 0;
