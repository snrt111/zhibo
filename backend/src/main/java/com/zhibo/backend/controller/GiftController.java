package com.zhibo.backend.controller;

import com.zhibo.backend.entity.Gift;
import com.zhibo.backend.entity.GiftRecord;
import com.zhibo.backend.service.GiftService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gift")
public class GiftController {

    @Autowired
    private GiftService giftService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取礼物列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getGiftList() {
        try {
            List<Gift> giftList = giftService.getGiftList();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取礼物列表成功");
            response.put("data", giftList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 赠送礼物
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendGift(@RequestHeader("Authorization") String authorization, @RequestBody GiftRecord giftRecord) {
        try {
            // 解析token获取用户ID
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            giftRecord.setFromUserId(userId);
            // 发送礼物
            GiftRecord sentGiftRecord = giftService.sendGift(giftRecord);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "赠送礼物成功");
            response.put("data", sentGiftRecord);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取直播间的礼物记录
     */
    @GetMapping("/record/{liveId}")
    public ResponseEntity<?> getGiftRecordList(@PathVariable Long liveId, @RequestParam(defaultValue = "50") int limit) {
        try {
            List<GiftRecord> giftRecordList = giftService.getGiftRecordList(liveId, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取礼物记录成功");
            response.put("data", giftRecordList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户的礼物记录
     */
    @GetMapping("/my-record")
    public ResponseEntity<?> getMyGiftRecord(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "50") int limit) {
        try {
            // 解析token获取用户ID
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            List<GiftRecord> giftRecordList = giftService.getGiftRecordByUserId(userId, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取礼物记录成功");
            response.put("data", giftRecordList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}