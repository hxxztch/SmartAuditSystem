 package com.shenji.modules.system.domain;
 
 import com.baomidou.mybatisplus.annotation.*;
 import lombok.Data;
 import java.time.LocalDateTime;
 
 @Data
 @TableName("sys_user")
 public class SysUser {
     @TableId(type = IdType.AUTO)
     private Long userId;
     private String username;
     private String password;
     private String realName;
     private String avatar;
     private Long orgId;
     private String orgName;
     private Integer status;
     private String dataScope;
    private Integer isTempAuth;
     private LocalDateTime tempAuthExpire;
     @TableField(fill = FieldFill.INSERT)
     private LocalDateTime createTime;
 }
