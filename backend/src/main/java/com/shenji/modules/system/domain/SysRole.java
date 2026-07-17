 package com.shenji.modules.system.domain;
 
 import com.baomidou.mybatisplus.annotation.*;
 import lombok.Data;
 
 @Data
 @TableName("sys_role")
 public class SysRole {
     @TableId(type = IdType.AUTO)
     private Long roleId;
     private String roleCode;
     private String roleName;
     private Integer roleLevel;
     private String dataScope;
     private Integer status;
 }
