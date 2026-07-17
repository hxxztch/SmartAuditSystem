package com.shenji.modules.system.controller;

import com.shenji.common.utils.R;
import com.shenji.modules.system.mapper.SysUserMapper;
import com.shenji.modules.system.mapper.SysRoleMapper;
import com.shenji.modules.system.mapper.SysUserRoleMapper;
import com.shenji.modules.system.domain.SysUser;
import com.shenji.modules.system.domain.SysRole;
import com.shenji.modules.system.domain.SysUserRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/user-mgmt")
public class UserManageController {

    @Resource private SysUserMapper userMapper;
    @Resource private SysRoleMapper roleMapper;
    @Resource private SysUserRoleMapper userRoleMapper;
    @Resource private JdbcTemplate jdbc;

    /** List all users */
    @GetMapping("/users")
    public R listUsers() {
        List<SysUser> users = userMapper.selectList(null);
        List<Map<String,Object>> result = new ArrayList<>();
        for (SysUser u : users) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("userId", u.getUserId()); m.put("username", u.getUsername()); m.put("realName", u.getRealName());
            m.put("orgId", u.getOrgId()); m.put("orgName", u.getOrgName()); m.put("password", u.getPassword());
            m.put("roles", userMapper.selectRoleCodesByUserId(u.getUserId())); m.put("dataScope", u.getDataScope()!=null?u.getDataScope():"");
            result.add(m);
        }
        return R.ok(result);
    }

    /** Update user roles */
    @PutMapping("/user/{userId}/roles")
    public R updateUserRoles(@PathVariable Long userId, @RequestBody Map<String,Object> body) {
        List<String> roleCodes = (List<String>) body.get("roleCodes");
        if (roleCodes == null) return R.fail("roleCodes required");
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);
        for (String code : roleCodes) {
            SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
            if (role != null) { SysUserRole ur = new SysUserRole(); ur.setUserId(userId); ur.setRoleId(role.getRoleId()); userRoleMapper.insert(ur); }
        }
        return R.ok();
    }

    /** Update user org */
    @PutMapping("/user/{userId}/profile")
    public R updateProfile(@PathVariable Long userId, @RequestBody Map<String,Object> body) {
        if (body.containsKey("username")) jdbc.update("UPDATE sys_user SET username=? WHERE user_id=?", body.get("username"), userId);
        if (body.containsKey("password") && body.get("password")!=null && !"".equals(body.get("password"))) jdbc.update("UPDATE sys_user SET password=? WHERE user_id=?", body.get("password"), userId);
        if (body.containsKey("realName")) jdbc.update("UPDATE sys_user SET real_name=? WHERE user_id=?", body.get("realName"), userId);
        if (body.containsKey("orgName")) jdbc.update("UPDATE sys_user SET org_name=? WHERE user_id=?", body.get("orgName"), userId);
        return R.ok();
    }

    @PutMapping("/user/{userId}/org")
    public R updateUserOrg(@PathVariable Long userId, @RequestBody Map<String,Object> body) {
        String orgName = (String) body.get("orgName");
        if (orgName == null) return R.fail("orgName required");
        SysUser user = userMapper.selectById(userId);
        if (user == null) return R.fail("user not found");
        String oldOrg = user.getOrgName();
        user.setOrgName(orgName); userMapper.updateById(user);
        // Project assignments now managed via ProjectManage page, no auto-sync here
        return R.ok();
    }

    @PostMapping("/user")
    public R createUser(@RequestBody Map<String,Object> body) {
        try {
            jdbc.update("INSERT INTO sys_user(username,password,real_name,org_name,status) VALUES(?,?,?,?,1)",
                body.get("username"), body.get("password"), body.get("realName"), body.get("orgName"));
        } catch (Exception e) { return R.fail("username already exists"); }
        return R.ok();
    }

    @DeleteMapping("/user/{userId}")
    public R deleteUser(@PathVariable Long userId) {
        jdbc.update("DELETE FROM sys_user_role WHERE user_id=?", userId);
        jdbc.update("DELETE FROM audit_project_member WHERE user_id=?", userId);
        jdbc.update("DELETE FROM sys_user WHERE user_id=?", userId);
        return R.ok();
    }

    @PutMapping("/user/{userId}/password")
    public R resetPassword(@PathVariable Long userId, @RequestBody Map<String,Object> body) {
        jdbc.update("UPDATE sys_user SET password=? WHERE user_id=?", body.get("password"), userId);
        return R.ok();
    }

    @PutMapping("/user/{userId}/data-scope")
    public R updateDataScope(@PathVariable Long userId, @RequestBody Map<String,Object> body) {
        jdbc.update("UPDATE sys_user SET data_scope=? WHERE user_id=?", body.get("dataScope"), userId);
        return R.ok();
    }

    @PostMapping("/init-menu17")
    public R initMenu17() {
        try { jdbc.queryForObject("SELECT menu_id FROM sys_menu WHERE menu_id=17", Integer.class); }
        catch (Exception e) {
            jdbc.update("INSERT INTO sys_menu(menu_id,parent_id,menu_name,menu_type,perms,path,icon,order_num) VALUES(17,8,'Menu17','C','perm:project','','',17)");
            jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(2,17)");
        }
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id=3 AND menu_id=4");
        try { jdbc.queryForObject("SELECT id FROM sys_role_menu WHERE role_id=3 AND menu_id=17", Integer.class); } catch (Exception ex) { jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,17)"); }
        try { jdbc.queryForObject("SELECT id FROM sys_role_menu WHERE role_id=3 AND menu_id=10", Integer.class); } catch (Exception ex) { jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,10)"); }
        try { jdbc.queryForObject("SELECT id FROM sys_role_menu WHERE role_id=3 AND menu_id=4", Integer.class); } catch (Exception ex) { jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(3,4)"); }
        return R.ok();
    }

    /** List orgs */
    @GetMapping("/orgs")
    public R listOrgs() { return R.ok(jdbc.queryForList("SELECT DISTINCT org_name FROM audit_project", String.class)); }

    /** === Role CRUD === */
    @GetMapping("/roles")
    public R listRoles() {
        List<SysRole> roles = roleMapper.selectList(null);
        List<Map<String,Object>> result = new ArrayList<>();
        for (SysRole r : roles) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", r.getRoleId()); m.put("code", r.getRoleCode()); m.put("name", r.getRoleName());
            m.put("level", r.getRoleLevel()<=2?"high":r.getRoleLevel()<=3?"mid":"basic");
            m.put("dataScope", r.getDataScope());
            m.put("perms", userMapper.selectPermsByRoleId(r.getRoleId()));
            m.put("userCount", 0); m.put("description", "");
            result.add(m);
        }
        return R.ok(result);
    }

    @PostMapping("/role")
    public R saveRole(@RequestBody Map<String,Object> body) {
        String code = (String) body.get("code"); String name = (String) body.get("name"); String level = (String) body.get("level");
        int rl = "high".equals(level)?1:"mid".equals(level)?3:5;
        List<String> perms = (List<String>) body.get("perms");
        Object idObj = body.get("id");
        SysRole role;
        if (idObj != null) { role = roleMapper.selectById(Long.parseLong(String.valueOf(idObj))); if (role==null) return R.fail("not found"); }
        else { role = new SysRole(); List<SysRole> dup = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code)); if (dup!=null && !dup.isEmpty()) return R.fail("role code already exists"); List<SysRole> dupName = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleName, name)); if (dupName!=null && !dupName.isEmpty()) return R.fail("role name already exists"); }
        role.setRoleCode(code); role.setRoleName(name); role.setRoleLevel(rl); role.setDataScope("self"); role.setStatus(1);
        if (idObj!=null) roleMapper.updateById(role); else roleMapper.insert(role);
        if (perms!=null) { jdbc.update("DELETE FROM sys_role_menu WHERE role_id=?", role.getRoleId()); for (String p:perms) { try { Integer mid = jdbc.queryForObject("SELECT menu_id FROM sys_menu WHERE perms=? LIMIT 1", Integer.class, p); if (mid!=null) jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(?,?)", role.getRoleId(), mid); } catch (Exception ex) { } } }
        return R.ok();
    }

    @DeleteMapping("/role/{roleId}")
    public R deleteRole(@PathVariable Long roleId) {
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id=?", roleId);
        jdbc.update("DELETE FROM sys_user_role WHERE role_id=?", roleId);
        roleMapper.deleteById(roleId);
        return R.ok();
    }
}
