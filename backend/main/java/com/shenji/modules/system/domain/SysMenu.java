 package com.shenji.modules.system.domain;
 
 import com.baomidou.mybatisplus.annotation.*;
 import lombok.Data;
 
 @Data
 @TableName("sys_menu")
 public class SysMenu {
     @TableId(type = IdType.AUTO)
     private Long menuId;
     private Long parentId;
     private String menuName;
     private String menuType;
     private String perms;
     private String path;
     private String icon;
     private Integer orderNum;
 }
