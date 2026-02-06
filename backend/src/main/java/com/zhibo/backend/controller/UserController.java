package com.zhibo.backend.controller;

import com.zhibo.backend.entity.User;
import com.zhibo.backend.service.UserService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/hello")
    public String hello() {
        return "hello...";
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "注册成功");
            response.put("data", registeredUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            Optional<User> userOptional = userService.login(username, password);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 生成JWT令牌
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "登录成功");
                response.put("token", token);
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 401);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authorization) {
        try {
            // 解析token
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "无效的token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String authorization, @RequestBody User user) {
        try {
            // 解析token
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            user.setId(userId);
            boolean success = userService.updateUserInfo(user);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "更新成功");
                response.put("data", userService.getById(userId));
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "更新失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "无效的token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> resetRequest) {
        try {
            // 解析token
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            String newPassword = resetRequest.get("newPassword");
            boolean success = userService.resetPassword(userId, newPassword);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "密码重置成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "密码重置失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "无效的token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}