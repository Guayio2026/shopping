-- Minimal seed data (prefer sql/admin_scaffold.sql for fresh installs)

INSERT INTO admin (username, password, nickname, avatar)
SELECT 'admin', 'admin123', '超级管理员', 'https://picsum.photos/seed/admin/100/100'
WHERE NOT EXISTS (SELECT 1 FROM admin WHERE username = 'admin');

INSERT INTO `user` (username, password, nickname, role, avatar)
SELECT 'user', 'user123', '演示用户', 'USER', 'https://picsum.photos/seed/user/100/100'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'user');

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'system_name', 'Admin Scaffold', '系统名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system_name');

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'system_logo', '', '系统 Logo'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system_logo');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT 0, '首页', '/admin/home', 'HomeFilled', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/home');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT 0, '系统管理', '/admin/system', 'Setting', 2
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/system');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT (SELECT id FROM sys_menu WHERE path = '/admin/system' LIMIT 1), '管理员管理', '/admin/admins', 'Avatar', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/admins');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT (SELECT id FROM sys_menu WHERE path = '/admin/system' LIMIT 1), '用户管理', '/admin/users', 'User', 2
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/users');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT (SELECT id FROM sys_menu WHERE path = '/admin/system' LIMIT 1), '菜单管理', '/admin/menus', 'Grid', 3
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/menus');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT (SELECT id FROM sys_menu WHERE path = '/admin/system' LIMIT 1), '轮播图管理', '/admin/banners', 'Picture', 4
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/banners');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT (SELECT id FROM sys_menu WHERE path = '/admin/system' LIMIT 1), '系统日志', '/admin/logs', 'Tickets', 5
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/logs');

INSERT INTO sys_menu (parent_id, title, path, icon, sort_order)
SELECT (SELECT id FROM sys_menu WHERE path = '/admin/system' LIMIT 1), '系统设置', '/admin/settings', 'Tools', 6
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/admin/settings');

UPDATE sys_menu child
INNER JOIN sys_menu parent ON parent.path = '/admin/system'
SET child.parent_id = parent.id
WHERE child.path IN (
    '/admin/admins', '/admin/users', '/admin/menus',
    '/admin/banners', '/admin/logs', '/admin/settings'
);

INSERT INTO home_banner (title, subtitle, cover, link_url, cta_text, sort_order, status)
SELECT * FROM (
    SELECT
        'Welcome to Admin Scaffold' AS title,
        'Spring Boot 3 + Vue 3 管理后台脚手架' AS subtitle,
        'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1600&q=80' AS cover,
        'https://example.com' AS link_url,
        '了解更多' AS cta_text,
        1 AS sort_order,
        1 AS status
    UNION ALL
    SELECT
        '开箱即用',
        '用户、菜单、日志、设置一应俱全',
        'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1600&q=80',
        'https://example.com/docs',
        '查看文档',
        2,
        1
) t
WHERE NOT EXISTS (SELECT 1 FROM home_banner LIMIT 1);
