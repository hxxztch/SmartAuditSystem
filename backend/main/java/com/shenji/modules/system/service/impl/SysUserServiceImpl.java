package com.shenji.modules.system.service.impl;
 
 import cn.dev33.satoken.secure.BCrypt;
 import cn.dev33.satoken.stp.StpUtil;
 import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
 import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
 import com.shenji.modules.system.domain.SysUser;
 import com.shenji.modules.system.mapper.SysUserMapper;
 import com.shenji.modules.system.service.SysUserService;
 import org.springframework.stereotype.Service;
 
 import java.util.*;
 
 import javax.annotation.Resource;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 @Service
 public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
 
     @Resource
     private JdbcTemplate jdbcTemplate;
 
     @Override
     public SysUser login(String username, String password) {
         SysUser user = this.getOne(
             new LambdaQueryWrapper<SysUser>()
                 .eq(SysUser::getUsername, username)
                 .eq(SysUser::getStatus, 1)
         );
         if (user == null) {
             throw new IllegalArgumentException("用户名或密码错误");
         }
         // 密码验证（初始用 BCrypt，兼容明文）
         if (!BCrypt.checkpw(password, user.getPassword()) && !password.equals(user.getPassword())) {
             throw new IllegalArgumentException("用户名或密码错误");
         }
         StpUtil.login(user.getUserId());
         return user;
     }
 
     @Override
     public List<String> getUserRoles(Long userId) {
         return baseMapper.selectRoleCodesByUserId(userId);
     }
 
     @Override
     public List<String> getUserPerms(Long userId) {
         List<String> perms = new ArrayList<>(baseMapper.selectPermsByUserId(userId));
         for (String p : getTempGrantPerms(userId)) {
             if (!perms.contains(p)) perms.add(p);
         }
         return perms;
     }
 
     private List<String> getTempGrantPerms(Long userId) {
         try {
             List<Map<String, Object>> grants = jdbcTemplate.queryForList(
                 "SELECT DISTINCT target_id FROM temp_grant WHERE user_id = ? AND type = 'DATA_PERM' AND status = 'ACTIVE'",
                 userId
             );
             List<String> result = new ArrayList<>();
             for (Map<String, Object> g : grants) {
                 Object tid = g.get("TARGET_ID");
                 if (tid != null) result.add(tid.toString());
             }
             return result;
         } catch (Exception e) {
             return new ArrayList<>();
         }
     }
 
     @Override
     public Map<String, Object> getUserInfo(Long userId) {
         SysUser user = this.getById(userId);
         List<String> roles = getUserRoles(userId);
         Map<String, Object> info = new LinkedHashMap<>();
         info.put("userId", user.getUserId());
         info.put("username", user.getUsername());
         info.put("realName", user.getRealName());
         info.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
         info.put("orgId", user.getOrgId());
         info.put("orgName", user.getOrgName() != null ? user.getOrgName() : "");
         info.put("roles", roles);
         info.put("roleLabel", roles.isEmpty() ? "" : roles.get(0));
         info.put("dataScope", resolveDataScope(roles));
         info.put("isTempAuth", user.getIsTempAuth() != null && user.getIsTempAuth() == 1);
         info.put("tempAuthExpireAt", user.getTempAuthExpire());
         return info;
     }
 
     private String resolveDataScope(List<String> roles) {
         if (roles.contains("school_leader") || roles.contains("audit_director")) return "all";
         if (roles.contains("auditee_head") || roles.contains("auditee_liaison")) return "dept";
         if (roles.contains("audit_manager") || roles.contains("auditor") || roles.contains("intermediary")) return "project";
         return "self";
     }
 }
