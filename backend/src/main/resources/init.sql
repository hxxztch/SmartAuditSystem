 -- ============================================
 -- 高校一体化智慧审计平台 - 初始化SQL
 -- 数据库: shenji_audit
 -- ============================================
 
 CREATE DATABASE IF NOT EXISTS shenji_audit DEFAULT CHARACTER SET utf8mb4;
 USE shenji_audit;
 
 -- 用户表
 DROP TABLE IF EXISTS sys_user;
 CREATE TABLE sys_user (
     user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
     username       VARCHAR(64)  NOT NULL UNIQUE,
     password       VARCHAR(128) NOT NULL,
     real_name      VARCHAR(64)  DEFAULT '',
     avatar         VARCHAR(256) DEFAULT '',
     org_id         BIGINT       DEFAULT 0,
     org_name       VARCHAR(128) DEFAULT '',
     status         TINYINT      DEFAULT 1 COMMENT '0-禁用 1-启用',
     is_temp_auth   TINYINT      DEFAULT 0,
     temp_auth_expire DATETIME   DEFAULT NULL,
     create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP
 ) ENGINE=InnoDB;
 
 -- 角色表
 DROP TABLE IF EXISTS sys_role;
 CREATE TABLE sys_role (
     role_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
     role_code  VARCHAR(64)  NOT NULL UNIQUE,
     role_name  VARCHAR(64)  NOT NULL,
     role_level TINYINT      DEFAULT 1,
     data_scope VARCHAR(16)  DEFAULT 'self',
     status     TINYINT      DEFAULT 1
 ) ENGINE=InnoDB;
 
 -- 菜单权限表
 DROP TABLE IF EXISTS sys_menu;
 CREATE TABLE sys_menu (
     menu_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
     parent_id BIGINT       DEFAULT 0,
     menu_name VARCHAR(64)  NOT NULL,
     menu_type CHAR(1)      DEFAULT 'C',
     perms     VARCHAR(128) DEFAULT '',
     path      VARCHAR(256) DEFAULT '',
     icon      VARCHAR(64)  DEFAULT '',
     order_num INT          DEFAULT 0
 ) ENGINE=InnoDB;
 
 -- 用户角色关联
 DROP TABLE IF EXISTS sys_user_role;
 CREATE TABLE sys_user_role (
     id      BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,
     role_id BIGINT NOT NULL
 ) ENGINE=InnoDB;
 
 -- 角色菜单关联
 DROP TABLE IF EXISTS sys_role_menu;
 CREATE TABLE sys_role_menu (
     id      BIGINT AUTO_INCREMENT PRIMARY KEY,
     role_id BIGINT NOT NULL,
     menu_id BIGINT NOT NULL
 ) ENGINE=InnoDB;
 
 -- 数据权限规则
 DROP TABLE IF EXISTS sys_data_permission;
 CREATE TABLE sys_data_permission (
     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
     role_id    BIGINT       DEFAULT NULL,
     scope      VARCHAR(16)  DEFAULT 'project',
     table_name VARCHAR(64)  DEFAULT '',
     filter_sql VARCHAR(512) DEFAULT '',
     org_ids    VARCHAR(1024) DEFAULT '[]'
 ) ENGINE=InnoDB;
 
 -- 临时授权
 DROP TABLE IF EXISTS sys_temp_auth;
 CREATE TABLE sys_temp_auth (
     id          BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id     BIGINT       NOT NULL,
     auth_type   VARCHAR(16)  DEFAULT 'data',
     resources   VARCHAR(2048) DEFAULT '[]',
     start_time  DATETIME     DEFAULT NULL,
     expire_time DATETIME     DEFAULT NULL,
     reason      VARCHAR(512) DEFAULT '',
     grantor_id  BIGINT       DEFAULT NULL,
     status      TINYINT      DEFAULT 1 COMMENT '0-已撤销 1-生效中 2-已过期'
 ) ENGINE=InnoDB;
 
 -- 看板布局
 DROP TABLE IF EXISTS dashboard_layout;
 CREATE TABLE dashboard_layout (
     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id    BIGINT NOT NULL,
     layout_json TEXT,
     is_default TINYINT DEFAULT 0,
     update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
 ) ENGINE=InnoDB;
 
 -- ============================================
 -- 预置数据
 -- ============================================
 
 -- 角色
 INSERT INTO sys_role(role_code, role_name, role_level, data_scope) VALUES
 ('school_leader',     '校领导',           1, 'all'),
 ('audit_director',   '审计处长',         2, 'all'),
 ('audit_manager',    '项目组长/主审',    3, 'project'),
 ('auditor',          '普通审计人员',     4, 'project'),
 ('auditee_head',      '被审计单位负责人', 5, 'dept'),
 ('auditee_liaison',   '被审计单位联络员', 6, 'dept'),
 ('intermediary',      '中介审计机构人员', 7, 'project');
 
 -- 权限菜单
 INSERT INTO sys_menu(menu_id, parent_id, menu_name, menu_type, perms, path, icon, order_num) VALUES
 (1,  0, '数据驾驶舱',  'C', '',           '/dashboard',       'Monitor',      1),
 (2,  1, '全局总览',    'C', 'dashboard:view', '/dashboard/overview', 'DataBoard',    1),
 (3,  1, '原始明细',    'C', 'dashboard:source', '/dashboard/source', 'List',         4),
 (4,  1, '布局自定义',  'F', 'dashboard:customize', '', '', 5),
 (5,  1, '导出',        'F', 'dashboard:export',   '', '', 6),
 (6,  1, '分类统计',    'C', 'dashboard:overview', '/dashboard/category', 'PieChart', 2),
 (7,  1, '项目详情',    'C', 'dashboard:project',  '/dashboard/project',  'Document', 3),
 (8,  0, '权限管理',    'C', 'perm:manage', '/permission',     'Lock',         9),
 (9,  8, '角色管理',    'C', 'perm:role',   '/permission/roles', 'UserFilled',  1),
 (10, 8, '用户权限',    'C', 'perm:user',   '/permission/users', 'Avatar',      2),
 (11, 8, '数据权限',    'C', 'perm:data',   '/permission/data',  'DataAnalysis',3),
 (12, 8, '临时授权',    'C', 'perm:temp_auth','/permission/temp-auth','Timer',    4),
 (13, 0, '数据权限码',  'D', 'data:financial', '',                '',            10),
 (14, 0, '数据权限码',  'D', 'data:project:all', '',              '',            11),
 (15, 0, '数据权限码',  'D', 'data:project:own', '',              '',            12),
 (16, 0, '数据权限码',  'D', 'data:project:dept', '',             '',            13);
 
 -- 角色-菜单 关联
 -- audit_director (role_id=2): 所有权限
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),
 (2,8),(2,9),(2,10),(2,11),(2,12),
 (2,13),(2,14),(2,15),(2,16);
 
 -- school_leader (role_id=1): 只读驾驶舱总览
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (1,1),(1,2),(1,6),(1,7);
 
 -- audit_manager (role_id=3): 驾驶舱+权限管理部分
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (3,1),(3,2),(3,4),(3,5),(3,6),(3,7),(3,14);
 
 -- auditor (role_id=4): 基础驾驶舱
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (4,1),(4,2),(4,6),(4,7),(4,15);
 
 -- auditee_head (role_id=5): 仅本单位数据
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (5,1),(5,6),(5,16);
 
 -- auditee_liaison (role_id=6): 仅本单位数据
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (6,1),(6,16);
 
 -- intermediary (role_id=7): 仅项目数据
 INSERT INTO sys_role_menu(role_id, menu_id) VALUES
 (7,1),(7,15);
 
 -- 用户
 INSERT INTO sys_user(username, password, real_name, org_id, org_name, status) VALUES
 ('leader',        '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '校领导A',    1, '校办',   1),
 ('admindirector', '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '张明',      2, '审计处', 1),
 ('manager',       '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '王芳',      2, '审计处', 1),
 ('auditor',       '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '赵强',      2, '审计处', 1),
 ('depthead',      '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '李强',      3, '计算机学院', 1),
 ('liaison',       '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '赵丽',      4, '经管学院', 1),
 ('agent',         '$2a$10$X/hX4qJ5G5Y5J5Y5J5Y5O.5Y5J5Y5J5Y5J5Y5J5Y5J5Y5J5Y5O', '周杰',      5, '中审众环', 1);
 
 -- 用户-角色 关联
 INSERT INTO sys_user_role(user_id, role_id) VALUES
 (1,1),   -- leader -> school_leader
 (2,2),   -- admindirector -> audit_director
 (3,3),   -- manager -> audit_manager
 (4,4),   -- auditor -> auditor
 (5,5),   -- depthead -> auditee_head
 (6,6),   -- liaison -> auditee_liaison
 (7,7);   -- agent -> intermediary
