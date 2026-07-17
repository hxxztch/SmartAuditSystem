 package com.shenji.common.exception;
 
 import cn.dev33.satoken.exception.NotLoginException;
 import cn.dev33.satoken.exception.NotPermissionException;
 import cn.dev33.satoken.exception.NotRoleException;
 import com.shenji.common.utils.R;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RestControllerAdvice;
 
 @RestControllerAdvice
 public class GlobalExceptionHandler {
     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
 
     @ExceptionHandler(NotLoginException.class)
     public R handleNotLogin(NotLoginException e) {
         return R.fail(401, "登录已过期，请重新登录");
     }
 
     @ExceptionHandler(NotPermissionException.class)
     public R handleNotPermission(NotPermissionException e) {
         return R.fail(403, "权限不足：" + e.getPermission());
     }
 
     @ExceptionHandler(NotRoleException.class)
     public R handleNotRole(NotRoleException e) {
         return R.fail(403, "角色权限不足");
     }
 
     @ExceptionHandler(IllegalArgumentException.class)
     public R handleIllegalArgument(IllegalArgumentException e) {
         return R.fail(400, e.getMessage());
     }
 
     @ExceptionHandler(Exception.class)
     public R handleException(Exception e) {
         log.error("系统异常", e);
         return R.fail("服务器异常，请联系管理员");
     }
 }
