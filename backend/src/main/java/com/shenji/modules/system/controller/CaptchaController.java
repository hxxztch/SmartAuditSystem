package com.shenji.modules.system.controller;

import com.shenji.common.utils.R;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

@RestController
public class CaptchaController {

    private static final Map<String, String> CAPTCHA_STORE = new ConcurrentHashMap<>();
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    static {
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(300_000); CAPTCHA_STORE.clear(); }
                catch (InterruptedException e) { break; }
            }
        }).start();
    }

    @GetMapping("/captcha")
    public R getCaptcha() {
        String code = generateCode(4);
        String key = UUID.randomUUID().toString().replace("-", "");
        CAPTCHA_STORE.put(key, code);
        String base64 = generateCaptchaImage(code);
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", "data:image/png;base64," + base64);
        return R.ok(result);
    }

    public static boolean validate(String key, String code) {
        if (key == null || code == null) return false;
        String stored = CAPTCHA_STORE.remove(key);
        return stored != null && stored.equalsIgnoreCase(code);
    }

    private String generateCode(int len) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(CHARS.charAt(r.nextInt(CHARS.length())));
        return sb.toString();
    }

    private String generateCaptchaImage(String code) {
        int w = 120, h = 42;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(240, 242, 245));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(200, 210, 220));
        Random r = new Random();
        for (int i = 0; i < 5; i++) {
            g.drawLine(r.nextInt(w), r.nextInt(h), r.nextInt(w), r.nextInt(h));
        }
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + r.nextInt(60), 40 + r.nextInt(80), 120 + r.nextInt(60)));
            int x = 10 + i * 26 + r.nextInt(5);
            int y = 30 + r.nextInt(6);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
        g.dispose();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}