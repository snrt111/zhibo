package com.zhibo.backend.controller;

import com.zhibo.backend.entity.Danmaku;
import com.zhibo.backend.service.DanmakuService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/danmaku")
public class DanmakuController {

    @Autowired
    private DanmakuService danmakuService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 发送弹幕
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendDanmaku(@RequestHeader("Authorization") String authorization, @RequestBody Danmaku danmaku) {
        try {
            // 解析token获取用户ID
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            danmaku.setUserId(userId);
            // 发送弹幕
            Danmaku sentDanmaku = danmakuService.sendDanmaku(danmaku);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "发送弹幕成功");
            response.put("data", sentDanmaku);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取弹幕列表
     */
    @GetMapping("/list/{liveId}")
    public ResponseEntity<?> getDanmakuList(@PathVariable Long liveId, @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Danmaku> danmakuList = danmakuService.getDanmakuList(liveId, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取弹幕列表成功");
            response.put("data", danmakuList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 增加直播间连接数
     */
    @PostMapping("/connect/{liveId}")
    public ResponseEntity<?> addConnection(@PathVariable Long liveId) {
        try {
            danmakuService.addConnection(liveId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "连接成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 减少直播间连接数
     */
    @PostMapping("/disconnect/{liveId}")
    public ResponseEntity<?> removeConnection(@PathVariable Long liveId) {
        try {
            danmakuService.removeConnection(liveId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "断开连接成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}