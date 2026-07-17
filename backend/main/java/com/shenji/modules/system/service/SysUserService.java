 package com.shenji.modules.system.service;
 
 import com.baomidou.mybatisplus.extension.service.IService;
 import com.shenji.modules.system.domain.SysUser;
 
 import java.util.List;
 import java.util.Map;
 
 public interface SysUserService extends IService<SysUser> {
     SysUser login(String username, String password);
     List<String> getUserRoles(Long userId);
     List<String> getUserPerms(Long userId);
     Map<String, Object> getUserInfo(Long userId);
 }
