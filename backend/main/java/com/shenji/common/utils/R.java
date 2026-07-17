 package com.shenji.common.utils;
 
 import java.util.HashMap;
 
 /** 统一响应体 */
 public class R extends HashMap<String, Object> {
     private static final long serialVersionUID = 1L;
 
     public static R ok() {
         R r = new R();
         r.put("code", 200);
         r.put("msg", "success");
         return r;
     }
 
     public static R ok(Object data) {
         R r = ok();
         r.put("data", data);
         return r;
     }
 
     public static R fail(String msg) {
         R r = new R();
         r.put("code", 500);
         r.put("msg", msg);
         return r;
     }
 
     public static R fail(int code, String msg) {
         R r = new R();
         r.put("code", code);
         r.put("msg", msg);
         return r;
     }
 }
