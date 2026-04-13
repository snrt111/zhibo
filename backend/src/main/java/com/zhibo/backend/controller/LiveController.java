package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Category;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.exception.BusinessException;
import com.zhibo.backend.service.CategoryService;
import com.zhibo.backend.service.LiveSearchService;
import com.zhibo.backend.service.LiveService;
import com.zhibo.backend.service.MetricsService;
import com.zhibo.backend.service.UserService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/live")
public class LiveController {

    private static final Logger logger = LoggerFactory.getLogger(LiveController.class);

    @Autowired
    private LiveService liveService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private LiveSearchService liveSearchService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MetricsService metricsService;

    private Map<String, Object> convertLiveToMap(Live live) {
        User user = userService.getById(live.getUserId());
        Map<String, Object> liveData = new HashMap<>();
        liveData.put("id", live.getId());
        liveData.put("userId", live.getUserId());
        liveData.put("title", live.getTitle());
        liveData.put("description", live.getDescription());
        liveData.put("cover", live.getCover());
        liveData.put("categoryId", live.getCategoryId());
        liveData.put("streamKey", live.getStreamKey());
        liveData.put("status", live.getStatus());
        liveData.put("viewCount", live.getViewCount());
        liveData.put("startTime", live.getStartTime());
        liveData.put("endTime", live.getEndTime());
        liveData.put("createdAt", live.getCreatedAt());
        liveData.put("updatedAt", live.getUpdatedAt());
        if (user != null) {
            liveData.put("userNickname", user.getNickname());
            liveData.put("userAvatar", user.getAvatar());
            liveData.put("userUsername", user.getUsername());
        } else {
            liveData.put("userNickname", "用户" + live.getUserId());
            liveData.put("userAvatar", null);
            liveData.put("userUsername", "用户" + live.getUserId());
        }
        return liveData;
    }

    @PostMapping("/create")
    public Result<Live> createLive(@RequestHeader("Authorization") String authorization, @RequestBody Live live) {
        logger.info("收到创建直播请求，authorization: {}, live: {}", authorization != null ? "有token" : "无token", live.getTitle());
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        logger.info("解析token成功，userId: {}", userId);
        live.setUserId(userId);
        Live createdLive = liveService.createLive(live);
        logger.info("创建直播成功，liveId: {}", createdLive.getId());
        return Result.success("创建直播成功", createdLive);
    }

    @PostMapping("/start/{liveId}")
    public Result<Boolean> startLive(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        Live live = liveService.getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播不存在");
        }
        if (userType != 1 && !live.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此直播");
        }
        boolean success = liveService.startLive(liveId);
        if (success) {
            // 增加直播流数量
            metricsService.incrementLiveStreamCount();
        }
        return Result.success("开始直播成功", success);
    }

    @PostMapping("/end/{liveId}")
    public Result<Boolean> endLive(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        Live live = liveService.getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播不存在");
        }
        if (userType != 1 && !live.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此直播");
        }
        boolean success = liveService.endLive(liveId);
        if (success) {
            // 减少直播流数量
            metricsService.decrementLiveStreamCount();
        }
        return Result.success("结束直播成功", success);
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getLiveList(@RequestParam(required = false) Integer status) {
        List<Live> liveList = liveService.getLiveList(status);
        List<Map<String, Object>> result = liveList.stream()
                .map(this::convertLiveToMap)
                .collect(java.util.stream.Collectors.toList());
        return Result.success("获取直播列表成功", result);
    }

    @GetMapping("/admin/list")
    public Result<Page<Map<String, Object>>> getAdminLiveList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Map<String, Object>> result = liveService.getAdminLiveList(status, categoryId, keyword, page, size);
        return Result.success("获取直播列表成功", result);
    }

    @GetMapping("/my-list")
    public Result<List<Map<String, Object>>> getMyLiveList(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Live> liveList = liveService.getLiveListByUserId(userId);
        List<Map<String, Object>> result = liveList.stream()
                .map(this::convertLiveToMap)
                .collect(java.util.stream.Collectors.toList());
        return Result.success("获取直播列表成功", result);
    }

    @GetMapping("/category/list")
    public Result<List<Category>> getCategoryList() {
        List<Category> categoryList = categoryService.getCategoryList();
        return Result.success("获取分类列表成功", categoryList);
    }

    @GetMapping("/list-by-category/{categoryId}")
    public Result<List<Map<String, Object>>> getLiveListByCategory(@PathVariable Long categoryId) {
        List<Live> liveList = liveService.getLiveListByCategoryId(categoryId);
        List<Map<String, Object>> result = liveList.stream()
                .map(this::convertLiveToMap)
                .collect(java.util.stream.Collectors.toList());
        return Result.success("获取直播列表成功", result);
    }

    @PostMapping("/increase-view/{liveId}")
    public Result<Boolean> increaseViewCount(@PathVariable Long liveId) {
        boolean success = liveService.increaseViewCount(liveId);
        return Result.success("增加观看人数成功", success);
    }

    @DeleteMapping("/delete/{liveId}")
    public Result<Boolean> deleteLive(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        Live live = liveService.getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播不存在");
        }
        if (userType != 1 && !live.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此直播");
        }
        boolean success = liveService.deleteLive(liveId);
        return Result.success("删除直播成功", success);
    }

    @GetMapping("/detail/{liveId}")
    public Result<Map<String, Object>> getLiveDetail(@PathVariable Long liveId) {
        Live live = liveService.getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播间不存在");
        }
        Map<String, Object> liveData = convertLiveToMap(live);
        return Result.success("获取直播详情成功", liveData);
    }

    @GetMapping("/push-url/{liveId}")
    public Result<String> getPushUrl(@RequestHeader("Authorization") String authorization, @PathVariable Long liveId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Live live = liveService.getById(liveId);
        if (live == null || !live.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权获取此直播推流地址");
        }
        String pushUrl = "rtmp://localhost:1935/live/" + live.getStreamKey();
        return Result.success("获取推流地址成功", pushUrl);
    }

    @GetMapping("/play-url/{liveId}")
    public Result<String> getPlayUrl(@PathVariable Long liveId) {
        Live live = liveService.getById(liveId);
        if (live == null) {
            throw new BusinessException(404, "直播间不存在");
        }
        String playUrl = "/hls/" + live.getStreamKey() + ".m3u8" + "?t=" + System.currentTimeMillis();
        return Result.success("获取播放地址成功", playUrl);
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> searchLives(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = liveSearchService.searchLives(keyword, status, page, size);
        return Result.success("搜索成功", result);
    }

    @PostMapping("/sync/{liveId}")
    public Result<Void> syncLiveToSearch(@PathVariable Long liveId) {
        liveSearchService.syncLiveToElasticsearch(liveId);
        return Result.success("同步成功", null);
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("测试成功", "Hello, World!");
    }

    @PostMapping("/batch-end")
    public Result<Integer> batchEndLive(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要结束的直播");
        }
        int count = liveService.batchEndLive(ids);
        // 减少直播流数量
        for (int i = 0; i < count; i++) {
            metricsService.decrementLiveStreamCount();
        }
        return Result.success("批量结束直播成功，共处理 " + count + " 个直播", count);
    }

    @PostMapping("/batch-delete")
    public Result<Integer> batchDeleteLive(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的直播");
        }
        int count = liveService.batchDeleteLive(ids);
        return Result.success("批量删除直播成功，共删除 " + count + " 个直播", count);
    }
}
