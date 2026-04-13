package com.zhibo.backend.controller;

import com.zhibo.backend.service.FFmpegStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stream")
public class BrowserStreamController {

    @Autowired
    private FFmpegStreamService ffmpegStreamService;

    @PostMapping("/start/{streamKey}")
    public ResponseEntity<?> startStream(@PathVariable String streamKey) {
        boolean started = ffmpegStreamService.startBrowserStreaming(streamKey);
        if (started) {
            Map<String, Object> data = new HashMap<>();
            data.put("streamKey", streamKey);
            data.put("wsUrl", "/ws/stream");
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "启动推流成功");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "启动推流失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/stop/{streamKey}")
    public ResponseEntity<?> stopStream(@PathVariable String streamKey) {
        ffmpegStreamService.stopBrowserStreaming(streamKey);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "停止推流成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{streamKey}")
    public ResponseEntity<?> getStreamStatus(@PathVariable String streamKey) {
        boolean isStreaming = ffmpegStreamService.isStreaming(streamKey);
        Map<String, Boolean> data = new HashMap<>();
        data.put("isStreaming", isStreaming);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取状态成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}