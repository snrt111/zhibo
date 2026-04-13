package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Danmaku;
import com.zhibo.backend.entity.DanmakuVO;
import com.zhibo.backend.service.DanmakuService;
import com.zhibo.backend.service.MetricsService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/danmaku")
public class DanmakuController {

    @Autowired
    private DanmakuService danmakuService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MetricsService metricsService;

    @PostMapping("/send")
    public Result<Danmaku> sendDanmaku(@RequestHeader("Authorization") String authorization, @RequestBody Danmaku danmaku) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        danmaku.setUserId(userId);
        Danmaku sentDanmaku = danmakuService.sendDanmaku(danmaku);
        // 增加弹幕数量
        metricsService.incrementDanmakuCount();
        return Result.success("发送弹幕成功", sentDanmaku);
    }

    @GetMapping("/list/{liveId}")
    public Result<List<DanmakuVO>> getDanmakuList(@PathVariable Long liveId, @RequestParam(defaultValue = "50") int limit) {
        List<DanmakuVO> danmakuList = danmakuService.getDanmakuList(liveId, limit);
        return Result.success("获取弹幕列表成功", danmakuList);
    }

    @PostMapping("/connect/{liveId}")
    public Result<Void> addConnection(@PathVariable Long liveId) {
        danmakuService.addConnection(liveId);
        return Result.success("连接成功", null);
    }

    @PostMapping("/disconnect/{liveId}")
    public Result<Void> removeConnection(@PathVariable Long liveId) {
        danmakuService.removeConnection(liveId);
        return Result.success("断开连接成功", null);
    }
}
