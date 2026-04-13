package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.LiveStartNotification;
import com.zhibo.backend.entity.UserNotificationSetting;
import com.zhibo.backend.service.NotificationService;
import com.zhibo.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器
 * 提供开播提醒、通知设置等功能
 *
 * @author zhibo
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户通知设置
     *
     * @param authorization 授权Token
     * @return 通知设置
     */
    @GetMapping("/setting")
    public Result<UserNotificationSetting> getUserSetting(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        UserNotificationSetting setting = notificationService.getOrCreateUserSetting(userId);
        return Result.success("获取成功", setting);
    }

    /**
     * 更新用户通知设置
     *
     * @param authorization 授权Token
     * @param request       包含设置项的请求体
     * @return 更新后的设置
     */
    @PutMapping("/setting")
    public Result<UserNotificationSetting> updateUserSetting(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Integer> request) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        Integer followLiveStart = request.get("followLiveStart");
        Integer systemNotification = request.get("systemNotification");
        Integer giftNotification = request.get("giftNotification");

        UserNotificationSetting setting = notificationService.updateUserSetting(
                userId, followLiveStart, systemNotification, giftNotification);

        logger.info("用户 {} 更新通知设置", userId);
        return Result.success("更新成功", setting);
    }

    /**
     * 获取用户的开播提醒列表
     *
     * @param authorization 授权Token
     * @param limit         限制数量（默认20）
     * @return 提醒列表
     */
    @GetMapping("/list")
    public Result<List<LiveStartNotification>> getUserNotifications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "20") int limit) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        List<LiveStartNotification> notifications = notificationService.getUserNotifications(userId, limit);
        return Result.success("获取成功", notifications);
    }

    /**
     * 获取用户未读通知数量
     *
     * @param authorization 授权Token
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        int count = notificationService.getUnreadNotificationCount(userId);
        return Result.success("获取成功", count);
    }

    /**
     * 标记通知为已读
     *
     * @param authorization  授权Token
     * @param notificationId 通知ID
     * @return 操作结果
     */
    @PostMapping("/read/{notificationId}")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long notificationId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        boolean success = notificationService.markNotificationAsRead(notificationId, userId);
        if (!success) {
            return Result.error(404, "通知不存在或无权访问");
        }

        return Result.success("标记成功", null);
    }

    /**
     * 标记所有通知为已读
     *
     * @param authorization 授权Token
     * @return 操作结果
     */
    @PostMapping("/read-all")
    public Result<Integer> markAllAsRead(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        int count = notificationService.markAllNotificationsAsRead(userId);
        return Result.success("标记成功，共 " + count + " 条", count);
    }

    /**
     * 删除通知
     *
     * @param authorization  授权Token
     * @param notificationId 通知ID
     * @return 操作结果
     */
    @DeleteMapping("/{notificationId}")
    public Result<Void> deleteNotification(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long notificationId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        boolean success = notificationService.deleteNotification(notificationId, userId);
        if (!success) {
            return Result.error(404, "通知不存在或无权访问");
        }

        return Result.success("删除成功", null);
    }
}
