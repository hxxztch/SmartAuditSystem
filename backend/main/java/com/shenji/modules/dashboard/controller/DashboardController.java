package com.shenji.modules.dashboard.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shenji.common.utils.R;
import com.shenji.modules.dashboard.vo.DashboardVO;
import com.shenji.modules.system.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource private SysUserMapper sysUserMapper;
    @Resource private JdbcTemplate jdbc;

    @GetMapping("/overview")
    public R overview() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String dataScope = jdbc.queryForObject("SELECT data_scope FROM sys_user WHERE user_id=?", String.class, userId);
        if (dataScope==null||dataScope.isEmpty()) {
            if (roles.contains("audit_director")||roles.contains("school_leader")) dataScope="all";
            else if (roles.contains("auditee_head")||roles.contains("auditee_liaison")) dataScope="dept";
            else dataScope="project";
        }
        
        // Use same data filtering as myProjects 鈥?query real DB data
        String wherePart;
        if ("all".equals(dataScope)) {
            wherePart = "";
        } else if ("dept".equals(dataScope)) {
            wherePart = " WHERE (org_name=(SELECT org_name FROM sys_user WHERE user_id=" + userId + ") OR project_id IN (SELECT project_id FROM audit_project_member WHERE user_id=" + userId + "))";
        } else {
            wherePart = " WHERE project_id IN (SELECT project_id FROM audit_project_member WHERE user_id=" + userId + ")";
        }
        String concat = wherePart.isEmpty() ? " WHERE " : " AND ";
        
        int total      = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project" + wherePart, Integer.class);
        int ongoing    = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project" + (wherePart.isEmpty()?" WHERE ":wherePart+" AND ") + "status='ONGOING'", Integer.class);
        int completed  = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project" + (wherePart.isEmpty()?" WHERE ":wherePart+" AND ") + "status='COMPLETED'", Integer.class);
        int review     = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project" + (wherePart.isEmpty()?" WHERE ":wherePart+" AND ") + "status='REVIEW'", Integer.class);
        int planned    = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project" + (wherePart.isEmpty()?" WHERE ":wherePart+" AND ") + "status='PLANNED'", Integer.class);
        
        Double amount = jdbc.queryForObject("SELECT COALESCE(SUM(amount),0) FROM audit_project" + wherePart, Double.class);
        
        // Type distribution from real data
        List<Map<String,Object>> typeRows = jdbc.queryForList(
            "SELECT type, COUNT(*) as cnt FROM audit_project" + wherePart + 
            (wherePart.isEmpty() ? " GROUP BY type" : " GROUP BY type ORDER BY type"));
        List<Map<String,Object>> typeDist = new ArrayList<>();
        for (Map<String,Object> t : typeRows) {
            typeDist.add(m("name", t.get("TYPE"), "value", ((Number)t.get("CNT")).intValue()));
        }
        
        // Status distribution
        List<Map<String,Object>> statusDist = new ArrayList<>();
        statusDist.add(m("name","COMPLETED","value",completed));
        statusDist.add(m("name","ONGOING","value",ongoing));
        statusDist.add(m("name","REVIEW","value",review));
        statusDist.add(m("name","PLANNED","value",planned));
        
        DashboardVO vo = new DashboardVO();
        vo.setTotalProjects(total);
        vo.setOngoingProjects(ongoing);
        vo.setCompletedProjects(completed);
        vo.setTotalAmountInvolved(amount != null ? amount.longValue() : 0L);
        vo.setTotalIssues(0);
        vo.setRectifiedIssues(0);
        vo.setRectifyRate(0.0);
        vo.setProjectTypeDist(typeDist);
        vo.setProjectStatusDist(statusDist);
        return R.ok(vo);
    }
    
    private Map<String,Object> m(String k, Object v, String k2, Object v2) {
        Map<String,Object> r = new LinkedHashMap<>(); r.put(k,v); r.put(k2,v2); return r;
    }

    @GetMapping("/my-projects")
    public R myProjects() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String dataScope = jdbc.queryForObject("SELECT data_scope FROM sys_user WHERE user_id=?", String.class, userId);
        if (dataScope==null||dataScope.isEmpty()) {
            if (roles.contains("audit_director")||roles.contains("school_leader")) dataScope="all";
            else if (roles.contains("auditee_head")||roles.contains("auditee_liaison")) dataScope="dept";
            else dataScope="project";
        }
        String sql;
        if ("all".equals(dataScope)) {
            sql = "SELECT project_id as pid, code, name, type, org_name as orgname, leader_name as leader, status, start_date as startdate, end_date as enddate, amount FROM audit_project";
        } else if ("dept".equals(dataScope)) {
            sql = "SELECT DISTINCT p.project_id as pid, p.code, p.name, p.type, p.org_name as orgname, p.leader_name as leader, p.status, p.start_date as startdate, p.end_date as enddate, p.amount FROM audit_project p LEFT JOIN audit_project_member m ON p.project_id=m.project_id WHERE p.org_name=(SELECT org_name FROM sys_user WHERE user_id=" + userId + ") OR m.user_id=" + userId + "";
        } else {
            sql = "SELECT DISTINCT p.project_id as pid, p.code, p.name, p.type, p.org_name as orgname, p.leader_name as leader, p.status, p.start_date as startdate, p.end_date as enddate, p.amount FROM audit_project p LEFT JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=" + userId;
        }
        List<Map<String,Object>> raw = jdbc.queryForList(sql);
        List<Map<String,Object>> fixed = new ArrayList<>();
        for (Map<String,Object> row : raw) {
            Map<String,Object> m = new LinkedHashMap<>();
            for (Map.Entry<String,Object> e : row.entrySet()) {
                m.put(e.getKey().toLowerCase(), e.getValue());
            }
            fixed.add(m);
        }
        return R.ok(fixed);
    }

    @PutMapping("/project/{projectId}")
    public R updateProject(@PathVariable Long projectId, @RequestBody Map<String,Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String dataScope = jdbc.queryForObject("SELECT data_scope FROM sys_user WHERE user_id=?", String.class, userId);
        if (dataScope==null||dataScope.isEmpty()) {
            if (roles.contains("audit_director")||roles.contains("school_leader")) dataScope="all";
            else if (roles.contains("auditee_head")||roles.contains("auditee_liaison")) dataScope="dept";
            else dataScope="project";
        }
        if (!"all".equals(dataScope)) {
            Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project_member WHERE project_id=? AND user_id=?", Integer.class, projectId, userId);
            if (cnt == null || cnt == 0) return R.fail(403, "Not authorized for this project");
        }
        StringBuilder sb = new StringBuilder("UPDATE audit_project SET ");
        List<Object> params = new ArrayList<>();
        for (String key : body.keySet()) {
            if ("userIds".equals(key) || "pid".equals(key)) continue;
            sb.append(key).append("=?,");
            params.add(body.get(key));
        }
        if (!params.isEmpty()) {
            sb.setLength(sb.length()-1);
            sb.append(" WHERE project_id=?");
            params.add(projectId);
            jdbc.update(sb.toString(), params.toArray());
        }
        if (body.containsKey("userIds")) {
            List<Integer> uids = (List<Integer>) body.get("userIds");
            jdbc.update("DELETE FROM audit_project_member WHERE project_id=?", projectId);
            if (uids != null) for (Integer uid : uids) jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", projectId, uid.longValue());
        }
        return R.ok();
    }

    @PostMapping("/project")
    public R createProject(@RequestBody Map<String,Object> body) {
        jdbc.update("INSERT INTO audit_project(code,name,type,org_name,leader_name,status,start_date,end_date,amount,issue_count) VALUES(?,?,?,?,?,?,?,?,?,0)",
            body.get("code"), body.get("name"), body.get("type"), body.get("org_name"), body.get("leader_name"),
            body.get("status"), body.get("start_date"), body.get("end_date"), Double.parseDouble(body.get("amount").toString()));
        return R.ok();
    }

    @DeleteMapping("/project/{projectId}")
    public R deleteProject(@PathVariable Long projectId) {
        jdbc.update("DELETE FROM audit_project_member WHERE project_id=?", projectId);
        jdbc.update("DELETE FROM audit_project WHERE project_id=?", projectId);
        return R.ok();
    }

    @GetMapping("/project/{projectId}/members")
    public R projectMembers(@PathVariable Long projectId) {
        return R.ok(jdbc.queryForList("SELECT user_id FROM audit_project_member WHERE project_id=?", Long.class, projectId));
    }

    @PostMapping("/project/{projectId}/members")
    public R assignMembers(@PathVariable Long projectId, @RequestBody Map<String,Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String dataScope = jdbc.queryForObject("SELECT data_scope FROM sys_user WHERE user_id=?", String.class, userId);
        if (dataScope==null||dataScope.isEmpty()) {
            if (roles.contains("audit_director")||roles.contains("school_leader")) dataScope="all";
            else if (roles.contains("auditee_head")||roles.contains("auditee_liaison")) dataScope="dept";
            else dataScope="project";
        }
        if (!"all".equals(dataScope)) {
            Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM audit_project_member WHERE project_id=? AND user_id=?", Integer.class, projectId, userId);
            if (cnt == null || cnt == 0) return R.fail(403, "Not authorized for this project");
        }
        List<Integer> userIds = (List<Integer>) body.get("userIds");
        jdbc.update("DELETE FROM audit_project_member WHERE project_id=?", projectId);
        if (userIds != null) for (Integer uid : userIds) jdbc.update("INSERT INTO audit_project_member(project_id,user_id) VALUES(?,?)", projectId, uid.longValue());
        return R.ok();
    }

@GetMapping("/category-stats") public R categoryStats() { return R.ok(new ArrayList<>()); }
    @GetMapping("/project/{id}") public R projectDetail(@PathVariable String id) { return R.ok(new LinkedHashMap<>()); }
    @GetMapping("/source-data")
    public R sourceData() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String dataScope = jdbc.queryForObject("SELECT data_scope FROM sys_user WHERE user_id=?", String.class, userId);
        if (dataScope==null||dataScope.isEmpty()) {
            if (roles.contains("audit_director")||roles.contains("school_leader")) dataScope="all";
            else if (roles.contains("auditee_head")||roles.contains("auditee_liaison")) dataScope="dept";
            else dataScope="project";
        }
        String sql;
        if ("all".equals(dataScope)) sql = "SELECT project_id as pid, code, name, type, org_name as orgname, leader_name as leader, status, start_date as startdate, end_date as enddate, amount FROM audit_project ORDER BY code";
        else if ("dept".equals(dataScope)) sql = "SELECT DISTINCT p.project_id as pid, p.code, p.name, p.type, p.org_name as orgname, p.leader_name as leader, p.status, p.start_date as startdate, p.end_date as enddate, p.amount FROM audit_project p LEFT JOIN audit_project_member m ON p.project_id=m.project_id WHERE p.org_name=(SELECT org_name FROM sys_user WHERE user_id=" + userId + ") OR m.user_id=" + userId + " ORDER BY p.code";
        else sql = "SELECT DISTINCT p.project_id as pid, p.code, p.name, p.type, p.org_name as orgname, p.leader_name as leader, p.status, p.start_date as startdate, p.end_date as enddate, p.amount FROM audit_project p LEFT JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=" + userId + " ORDER BY p.code";
        List<Map<String,Object>> raw = jdbc.queryForList(sql);
        List<Map<String,Object>> fixed = new ArrayList<>();
        for (Map<String,Object> row : raw) { Map<String,Object> m = new LinkedHashMap<>(); for (Map.Entry<String,Object> e : row.entrySet()) m.put(e.getKey().toLowerCase(), e.getValue()); fixed.add(m); }
        return R.ok(fixed);
    }

    
    @GetMapping("/layout") public R layout() { return R.ok(new ArrayList<>()); }
    @PutMapping("/layout") public R saveLayout(@RequestBody List<Map<String,Object>> l) { return R.ok(); }
    @GetMapping("/views") public R personalViews() { return R.ok(new ArrayList<>()); }
    @GetMapping("/warnings") public R warnings() { return R.ok(new ArrayList<>()); }
}



