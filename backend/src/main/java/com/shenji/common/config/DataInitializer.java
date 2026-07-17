package com.shenji.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        try {
            // Create tables
            jdbc.execute("CREATE TABLE IF NOT EXISTS sys_role(role_id BIGINT AUTO_INCREMENT PRIMARY KEY,role_code VARCHAR(64) UNIQUE,role_name VARCHAR(64),role_level INT,data_scope VARCHAR(16),status INT DEFAULT 1)");
            jdbc.execute("CREATE TABLE IF NOT EXISTS sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,parent_id BIGINT DEFAULT 0,menu_name VARCHAR(64),menu_type CHAR(1) DEFAULT 'C',perms VARCHAR(128),path VARCHAR(256),icon VARCHAR(64),order_num INT DEFAULT 0)");
            jdbc.execute("CREATE TABLE IF NOT EXISTS sys_user(user_id BIGINT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(64) UNIQUE,password VARCHAR(128),real_name VARCHAR(64),avatar VARCHAR(256),org_id BIGINT,org_name VARCHAR(128),status INT DEFAULT 1,is_temp_auth INT DEFAULT 0)");
            jdbc.execute("ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS data_scope VARCHAR(16)");
            jdbc.execute("CREATE TABLE IF NOT EXISTS sys_user_role(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,role_id BIGINT)");
            jdbc.execute("CREATE TABLE IF NOT EXISTS sys_role_menu(id BIGINT AUTO_INCREMENT PRIMARY KEY,role_id BIGINT,menu_id BIGINT)");

            // Check if data already exists
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sys_role", Integer.class);
            if (count != null && count > 0) return;

            // Insert roles
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('school_leader','SchoolLeader',1,'all')");
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('audit_director','AuditDirector',2,'all')");
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('audit_manager','AuditManager',3,'project')");
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('auditor','Auditor',4,'project')");
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('auditee_head','AuditeeHead',5,'dept')");
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('auditee_liaison','AuditeeLiaison',6,'dept')");
            jdbc.execute("INSERT INTO sys_role(role_code,role_name,role_level,data_scope) VALUES('intermediary','Intermediary',7,'project')");

            // Insert users
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('leader','123456','Leader',1,'Office',1)");
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('admindirector','123456','ZhangMing',2,'Audit',1)");
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('manager','123456','WangFang',2,'Audit',1)");
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('auditor','123456','ZhaoQiang',2,'Audit',1)");
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('depthead','123456','LiQiang',3,'CS College',1)");
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('liaison','123456','ZhaoLi',4,'EM College',1)");
            jdbc.execute("INSERT INTO sys_user(username,password,real_name,org_id,org_name,status) VALUES('agent','123456','ZhouJie',5,'ZhongShen',1)");

            // Assign roles to users
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(1,1)");
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(2,2)");
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(3,3)");
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(4,4)");
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(5,5)");
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(6,6)");
            jdbc.execute("INSERT INTO sys_user_role(user_id,role_id) VALUES(7,7)");

            // Insert menus
            for (int i = 1; i <= 17; i++) {
                long parent = i <= 7 ? 1 : (i <= 12 ? 8 : 0);
                String name = "Menu" + i;
                String perms = i == 2 ? "dashboard:view" : i == 3 ? "dashboard:source" : i == 4 ? "dashboard:customize" : i == 9 ? "perm:role" : i == 10 ? "perm:user" : i == 11 ? "perm:data" : i == 12 ? "perm:temp_auth" : i == 13 ? "data:financial" : i == 14 ? "data:project:all" : i == 15 ? "data:project:own" : i == 16 ? "data:project:dept" : i == 17 ? "perm:project" : "";
                jdbc.execute("INSERT INTO sys_menu(menu_id,parent_id,menu_name,menu_type,perms,path,icon,order_num) VALUES(" + i + "," + parent + ",'" + name + "','C','" + perms + "','',''," + i + ")");
            }

            // Assign menus to audit_director (role 2): all menus
            for (int i = 1; i <= 17; i++) {
                jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(2," + i + ")");
            }
            // Basic menus for school_leader (role 1)
            // Assign role menus for all roles
            // role 1: school_leader - dashboard view only
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(1,1)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(1,2)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(1,3)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(1,4)");
            // role 3: audit_manager
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,1)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,2)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,14)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,17)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,10)");
            // role 4: auditor
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(4,1)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(4,2)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(4,15)");
            // role 5: auditee_head
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(5,1)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(5,2)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(5,16)");
            // role 6: auditee_liaison
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(6,1)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(6,2)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(6,16)");
            // role 7: intermediary
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(7,1)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(7,2)");
            jdbc.execute("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(7,15)");

            

            // ====== audit_project table ======
            jdbc.execute("CREATE TABLE IF NOT EXISTS audit_project(project_id BIGINT AUTO_INCREMENT PRIMARY KEY,code VARCHAR(32),name VARCHAR(128),type VARCHAR(32),org_name VARCHAR(64),leader_id BIGINT,leader_name VARCHAR(32),status VARCHAR(16),start_date VARCHAR(16),end_date VARCHAR(16),amount DECIMAL(16,2),issue_count INT DEFAULT 0)");

            // Insert 12 projects with diverse data
            String[][] projects = {
                {"SJ-2026-001","2025 Finance Audit","FINANCE","CS College","2","Zhang Ming","COMPLETED","2026-01-15","2026-03-30","2580000.00","23"},
                {"SJ-2026-002","Li Ming Economy Audit","ECONOMY","EM College","3","Wang Fang","ONGOING","2026-02-01","2026-04-30","1560000.00","15"},
                {"SJ-2026-003","Research Fund Audit","SPECIAL","Research Office","4","Zhao Qiang","ONGOING","2026-03-01","2026-05-15","890000.00","8"},
                {"SJ-2026-004","Lab Building Audit","ENGINEERING","Infrastructure","3","Wang Fang","REVIEW","2026-02-20","2026-06-30","5200000.00","12"},
                {"SJ-2026-005","Medical College Assets","SPECIAL","Medical College","4","Zhao Qiang","PLANNED","2026-04-01","2026-06-30","1200000.00","0"},
                {"SJ-2026-006","Law School Finance","FINANCE","Law School","3","Wang Fang","COMPLETED","2026-01-10","2026-02-28","1800000.00","18"},
                {"SJ-2026-007","Physics Economy Audit","ECONOMY","Physics Dept","2","Zhang Ming","COMPLETED","2026-01-05","2026-04-15","2100000.00","21"},
                {"SJ-2026-008","Chemical Special Audit","SPECIAL","Chemistry Dept","4","Zhao Qiang","ONGOING","2026-03-15","2026-05-30","750000.00","6"},
                {"SJ-2026-009","Library Renovation","ENGINEERING","Library","3","Wang Fang","PLANNED","2026-05-01","2026-08-30","3800000.00","0"},
                {"SJ-2026-010","Foreign Lang Finance","FINANCE","Foreign Lang","4","Zhao Qiang","COMPLETED","2026-02-01","2026-03-30","950000.00","10"},
                {"SJ-2026-011","PE Dept Economy","ECONOMY","PE Dept","2","Zhang Ming","ONGOING","2026-04-01","2026-06-15","1100000.00","7"},
                {"SJ-2026-012","Logistics Special Audit","SPECIAL","Logistics","4","Zhao Qiang","REVIEW","2026-03-01","2026-06-30","2300000.00","14"}
            };
            for (String[] p : projects) {
                jdbc.update("INSERT INTO audit_project(code,name,type,org_name,leader_id,leader_name,status,start_date,end_date,amount,issue_count) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    p[0],p[1],p[2],p[3],Long.parseLong(p[4]),p[5],p[6],p[7],p[8],Double.parseDouble(p[9]),Integer.parseInt(p[10]));
            }
            
            // ====== audit_project_member table for data scope ======
            jdbc.execute("CREATE TABLE IF NOT EXISTS audit_project_member(id BIGINT AUTO_INCREMENT PRIMARY KEY,project_id BIGINT,user_id BIGINT)");
            // Assign members to projects: auditor(4) assigned to 6 projects, manager(3) to 4 projects
            long[][] members = {{1,4},{2,4},{5,4},{8,4},{10,4},{12,4},{1,2},{2,3},{6,2},{7,2},{3,3},{4,3},{9,3},{11,3}};
            for (long[] m : members) { jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", m[0], m[1]); }
            // Auditee head (user 5) can see projects from similar departments
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 1L, 5L);
            
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 2L, 6L);
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 3L, 7L);
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 12L, 7L);
            // Members for depthead(user 5) and liaison(user 6)
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 1L, 5L);
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 2L, 6L);
            // Assign projects to all users for demo completeness
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 1L, 1L);
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 11L, 1L);
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 2L, 6L);
            jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", 7L, 6L);
            
            System.out.println("=== Seed data: 12 projects with members inserted ===");

            System.out.println("=== DataInitializer: 7 roles, 7 users, 16 menus initialized ===");
        } catch (Exception e) {
            System.err.println("DataInitializer failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}