package com.shenji.modules.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shenji.common.utils.R;
import com.shenji.modules.system.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/auth-request")
public class AuthRequestController {
    @Resource private JdbcTemplate jdbc;
    @Resource private SysUserMapper userMapper;

    @PostMapping("/init")
    public R init() {
        jdbc.execute("DROP TABLE IF EXISTS auth_request");
        jdbc.execute("CREATE TABLE auth_request(id BIGINT AUTO_INCREMENT PRIMARY KEY, applicant_id BIGINT, applicant_name VARCHAR(64), type VARCHAR(32), project_name VARCHAR(128), description VARCHAR(512), amount DECIMAL(16,2), status VARCHAR(16), approver_id BIGINT, comments VARCHAR(2048), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        return R.ok();
    }

    @PostMapping
    public R create(@RequestBody Map<String,Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String realName = jdbc.queryForObject("SELECT real_name FROM sys_user WHERE user_id=?", String.class, userId);
        Object approverId = body.get("approverId");
        jdbc.update("INSERT INTO auth_request(applicant_id,applicant_name,type,project_name,description,amount,status,approver_id) VALUES(?,?,?,?,?,?,'PENDING',?)",
            userId, realName, body.get("type"), body.get("projectName"), body.get("description"),
            body.get("amount")!=null?Double.parseDouble(body.get("amount").toString()):null,
            approverId!=null?((Number)approverId).longValue():null);
        return R.ok();
    }

    @GetMapping("/my-requests")
    public R myRequests() {
        return R.ok(jdbc.queryForList("SELECT * FROM auth_request WHERE applicant_id=? ORDER BY created_at DESC", StpUtil.getLoginIdAsLong()));
    }

    @GetMapping("/approval-history")
    public R approvalHistory() {
        return R.ok(jdbc.queryForList("SELECT * FROM auth_request WHERE approver_id=? ORDER BY created_at DESC", StpUtil.getLoginIdAsLong()));
    }

    @DeleteMapping("/{id}")
    public R cancel(@PathVariable Long id) {
        jdbc.update("DELETE FROM auth_request WHERE id=? AND applicant_id=? AND status='PENDING'", id, StpUtil.getLoginIdAsLong());
        return R.ok();
    }

    @PutMapping("/{id}/approve")
    public R approve(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        String c = (String)body.get("comment");
        if(c!=null && !c.isEmpty())
            jdbc.update("UPDATE auth_request SET status='APPROVED', comments=? WHERE id=?",c,id);
        else
            jdbc.update("UPDATE auth_request SET status='APPROVED' WHERE id=?", id);
        return R.ok();
    }

    @PutMapping("/{id}/reject")
    public R reject(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        String c = (String)body.get("comment");
        if(c!=null && !c.isEmpty())
            jdbc.update("UPDATE auth_request SET status='REJECTED', comments=? WHERE id=?",c,id);
        else
            jdbc.update("UPDATE auth_request SET status='REJECTED' WHERE id=?", id);
        return R.ok();
    }
}



