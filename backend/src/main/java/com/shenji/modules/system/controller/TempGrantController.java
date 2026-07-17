package com.shenji.modules.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shenji.common.utils.R;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/temp-grant")
public class TempGrantController {
    @Resource private JdbcTemplate jdbc;

    @PostMapping("/init")
    public R init() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS temp_grant(id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT, username VARCHAR(64), real_name VARCHAR(64), type VARCHAR(32), target_name VARCHAR(256), target_id VARCHAR(64), status VARCHAR(16) DEFAULT 'ACTIVE', granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, revoked_at TIMESTAMP NULL)");
        return R.ok();
    }

    @PostMapping
    public R grant(@RequestBody Map<String,Object> body) {
        String type = (String)body.get("type");
        Long targetUserId = Long.parseLong(body.get("userId").toString());
        String realName = jdbc.queryForObject("SELECT real_name FROM sys_user WHERE user_id=?", String.class, targetUserId);
        String userName = jdbc.queryForObject("SELECT username FROM sys_user WHERE user_id=?", String.class, targetUserId);
        String targetName = (String)body.get("targetName");
        String targetId = body.get("targetId")!=null?body.get("targetId").toString():null;

        if ("PROJECT_ACCESS".equals(type)) {
            Long projectId = Long.parseLong(targetId);
            try { jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", projectId, targetUserId); } catch(Exception e) {}
        }

        if ("DATA_PERM".equals(type) && "data:project:all".equals(targetId)) jdbc.update("UPDATE sys_user SET data_scope='all' WHERE user_id=?", targetUserId);
        jdbc.update("INSERT INTO temp_grant(user_id,username,real_name,type,target_name,target_id) VALUES(?,?,?,?,?,?)",
            targetUserId, userName, realName, type, targetName, targetId);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R revoke(@PathVariable Long id) {
        Map<String,Object> g = jdbc.queryForMap("SELECT * FROM temp_grant WHERE id=?", id);
        if (g != null && "PROJECT_ACCESS".equals(g.get("TYPE"))) {
            jdbc.update("DELETE FROM audit_project_member WHERE project_id=? AND user_id=?",
                Long.parseLong(g.get("TARGET_ID").toString()), ((Number)g.get("USER_ID")).longValue());
        }
        if (g != null && "DATA_PERM".equals(g.get("TYPE")) && "data:project:all".equals(g.get("TARGET_ID"))) jdbc.update("UPDATE sys_user SET data_scope=NULL WHERE user_id=?", ((Number)g.get("USER_ID")).longValue());
        jdbc.update("UPDATE temp_grant SET status='REVOKED', revoked_at=CURRENT_TIMESTAMP WHERE id=?", id);
        return R.ok();
    }

    @GetMapping("/list")
    public R list() { return R.ok(jdbc.queryForList("SELECT * FROM temp_grant ORDER BY granted_at DESC")); }

    @GetMapping("/my")
    public R myGrants() {
        return R.ok(jdbc.queryForList("SELECT * FROM temp_grant WHERE user_id=? AND status='ACTIVE'", StpUtil.getLoginIdAsLong()));
    }
}

