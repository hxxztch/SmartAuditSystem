package com.shenji.modules.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shenji.common.utils.R;
import com.shenji.modules.system.domain.SysUser;
import com.shenji.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> body) {
        // Validate captcha
        String captchaKey = body.get("captchaKey");
        String captchaCode = body.get("captchaCode");
        if (captchaKey == null || captchaCode == null || !CaptchaController.validate(captchaKey, captchaCode)) {
            return R.fail(400, "验证码错误");
        }
        String username = body.get("username");
        String password = body.get("password");
        try {
            SysUser user = sysUserService.login(username, password);
            String token = StpUtil.getTokenValue();
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            return R.ok(data);
        } catch (IllegalArgumentException e) {
            return R.fail(401, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    @GetMapping("/userinfo")
    public R userinfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(sysUserService.getUserInfo(userId));
    }

    @GetMapping("/permissions")
    public R permissions() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> perms = sysUserService.getUserPerms(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("permissions", perms);
        return R.ok(data);
    }

    @GetMapping("/menus")
    public R menus() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> perms = sysUserService.getUserPerms(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("menus", perms);
        return R.ok(data);
    }
}