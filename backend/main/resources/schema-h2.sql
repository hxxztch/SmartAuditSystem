 -- H2 内存数据库建表脚本（无 MySQL 时的开发环境）
 CREATE TABLE IF NOT EXISTS sys_user (
     user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
     username       VARCHAR(64)  NOT NULL UNIQUE,
     password       VARCHAR(128) NOT NULL,
     real_name      VARCHAR(64)  DEFAULT '',
     avatar         VARCHAR(256) DEFAULT '',
     org_id         BIGINT       DEFAULT 0,
     org_name       VARCHAR(128) DEFAULT '',
     status         TINYINT      DEFAULT 1,
     is_temp_auth   TINYINT      DEFAULT 0,
     temp_auth_expire TIMESTAMP  DEFAULT NULL,
     create_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
 );
 
 CREATE TABLE IF NOT EXISTS sys_role (
     role_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
     role_code  VARCHAR(64)  NOT NULL UNIQUE,
     role_name  VARCHAR(64)  NOT NULL,
     role_level TINYINT      DEFAULT 1,
     data_scope VARCHAR(16)  DEFAULT 'self',
     status     TINYINT      DEFAULT 1
 );
 
 CREATE TABLE IF NOT EXISTS sys_menu (
     menu_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
     parent_id BIGINT       DEFAULT 0,
     menu_name VARCHAR(64)  NOT NULL,
     menu_type CHAR(1)      DEFAULT 'C',
     perms     VARCHAR(128) DEFAULT '',
     path      VARCHAR(256) DEFAULT '',
     icon      VARCHAR(64)  DEFAULT '',
     order_num INT          DEFAULT 0
 );
 
 CREATE TABLE IF NOT EXISTS sys_user_role (
     id      BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,
     role_id BIGINT NOT NULL
 );
 
 CREATE TABLE IF NOT EXISTS sys_role_menu (
     id      BIGINT AUTO_INCREMENT PRIMARY KEY,
     role_id BIGINT NOT NULL,
     menu_id BIGINT NOT NULL
 );
 
 CREATE TABLE IF NOT EXISTS dashboard_layout (
     id          BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id     BIGINT NOT NULL,
     layout_json TEXT,
     is_default  TINYINT DEFAULT 0,
     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 );
