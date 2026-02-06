package com.zhibo.backend.controller;

import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.Category;
import com.zhibo.backend.service.LiveService;
import com.zhibo.backend.service.CategoryService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/live")
public class LiveController {

    @Autowired
    private LiveService liveService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建直播
     */
    @PostMapping("/create")
    public ResponseEntity<?> createLive(@RequestHeader("Authorization") String authorization, @RequestBody Live live) {
        try {
            // 解析token获取用户ID
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            live.setUserId(userId);
            // 创建直播
            Live createdLive = liveService.createLive(live);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "创建直播成功");
            response.put("data", createdLive);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 开始直播
     */
    @PostMapping("/start/{liveId}")
    public ResponseEntity<?> startLive(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        try {
            // 验证用户权限（确保只有主播本人可以开始直播）
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            Live live = liveService.getById(liveId);
            if (live == null || !live.getUserId().equals(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权操作此直播");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            // 开始直播
            boolean success = liveService.startLive(liveId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "开始直播成功");
            response.put("data", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 结束直播
     */
    @PostMapping("/end/{liveId}")
    public ResponseEntity<?> endLive(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        try {
            // 验证用户权限（确保只有主播本人可以结束直播）
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            Live live = liveService.getById(liveId);
            if (live == null || !live.getUserId().equals(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权操作此直播");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            // 结束直播
            boolean success = liveService.endLive(liveId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "结束直播成功");
            response.put("data", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取直播列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getLiveList(@RequestParam(required = false) Integer status) {
        try {
            List<Live> liveList = liveService.getLiveList(status);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取直播列表成功");
            response.put("data", liveList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户的直播列表
     */
    @GetMapping("/my-list")
    public ResponseEntity<?> getMyLiveList(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            List<Live> liveList = liveService.getLiveListByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取直播列表成功");
            response.put("data", liveList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/category/list")
    public ResponseEntity<?> getCategoryList() {
        try {
            List<Category> categoryList = categoryService.getCategoryList();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取分类列表成功");
            response.put("data", categoryList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据分类获取直播列表
     */
    @GetMapping("/list-by-category/{categoryId}")
    public ResponseEntity<?> getLiveListByCategory(@PathVariable Long categoryId) {
        try {
            List<Live> liveList = liveService.getLiveListByCategoryId(categoryId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取直播列表成功");
            response.put("data", liveList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 增加观看人数
     */
    @PostMapping("/increase-view/{liveId}")
    public ResponseEntity<?> increaseViewCount(@PathVariable Long liveId) {
        try {
            boolean success = liveService.increaseViewCount(liveId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "增加观看人数成功");
            response.put("data", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除直播
     */
    @DeleteMapping("/delete/{liveId}")
    public ResponseEntity<?> deleteLive(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            Live live = liveService.getById(liveId);
            if (live == null || !live.getUserId().equals(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权操作此直播");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            boolean success = liveService.deleteLive(liveId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除直播成功");
            response.put("data", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取直播详情
     */
    @GetMapping("/detail/{liveId}")
    public ResponseEntity<?> getLiveDetail(@PathVariable Long liveId) {
        try {
            Live live = liveService.getById(liveId);
            if (live == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "直播间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取直播详情成功");
            response.put("data", live);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取推流地址
     */
    @GetMapping("/push-url/{liveId}")
    public ResponseEntity<?> getPushUrl(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            Live live = liveService.getById(liveId);
            if (live == null || !live.getUserId().equals(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权获取此直播推流地址");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            String pushUrl = "rtmp://localhost:1935/live/" + live.getStreamKey();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取推流地址成功");
            response.put("data", pushUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取播放地址
     */
    @GetMapping("/play-url/{liveId}")
    public ResponseEntity<?> getPlayUrl(@PathVariable Long liveId) {
        try {
            Live live = liveService.getById(liveId);
            if (live == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "直播间不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            String playUrl = "http://localhost:8080/hls/" + live.getStreamKey() + ".m3u8";
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取播放地址成功");
            response.put("data", playUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试端点
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "测试成功");
        response.put("data", "Hello, World!");
        return ResponseEntity.ok(response);
    }
}