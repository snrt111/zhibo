package com.zhibo.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Report;
import com.zhibo.backend.service.ReportService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Report> createReport(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Long reporterId = jwtUtil.getUserIdFromToken(token);

        Integer targetType = (Integer) request.get("targetType");
        Long targetId = Long.valueOf(request.get("targetId").toString());
        String reason = (String) request.get("reason");
        String description = (String) request.get("description");
        String evidence = (String) request.get("evidence");

        Report report = reportService.createReport(reporterId, targetType, targetId, reason, description, evidence);
        logger.info("用户 {} 举报了 {} {}", reporterId, targetType == 1 ? "直播" : targetType == 2 ? "用户" : targetType == 3 ? "评论" : "弹幕", targetId);
        return Result.success("举报成功", report);
    }

    @GetMapping("/pending")
    public Result<Page<Report>> getPendingList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<Report> result = reportService.getPendingList(page, size);
        return Result.success("获取待处理举报成功", result);
    }

    @GetMapping("/list")
    public Result<Page<Report>> getReportList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Page<Report> result = reportService.getReportList(page, size, status, targetType);
        return Result.success("获取举报列表成功", result);
    }

    @GetMapping("/my-list")
    public Result<Page<Report>> getMyReportList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Long reporterId = jwtUtil.getUserIdFromToken(token);
        Page<Report> result = reportService.getMyReportList(reporterId, page, size);
        return Result.success("获取我的举报成功", result);
    }

    @PostMapping("/handle/{reportId}")
    public Result<Void> handleReport(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }

        Long handlerId = jwtUtil.getUserIdFromToken(token);
        Integer status = (Integer) request.get("status");
        String handleResult = (String) request.get("handleResult");

        reportService.handleReport(reportId, handlerId, status, handleResult);
        logger.info("处理举报 {}，状态：{}", reportId, status);
        return Result.success("处理成功", null);
    }

    @GetMapping("/{reportId}")
    public Result<Report> getReportDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long reportId) {
        String token = authorization.replace("Bearer ", "");
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != 1) {
            return Result.error(403, "无权访问");
        }
        Report report = reportService.getById(reportId);
        if (report == null) {
            return Result.error(404, "举报记录不存在");
        }
        return Result.success("获取成功", report);
    }
}