package com.zhibo.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.LiveStartNotification;
import com.zhibo.backend.entity.UserNotificationSetting;

import java.util.List;

/**
 * 通知服务接口
 * 处理开播提醒、用户通知设置等功能
 *
 * @author zhibo
 * @since 2026-02-25
 */
public interface NotificationService extends IService<UserNotificationSetting> {

    /**
     * 获取或创建用户的通知设置
     *
     * @param userId 用户ID
     * @return 通知设置
     */
    UserNotificationSetting getOrCreateUserSetting(Long userId);

    /**
     * 更新用户通知设置
     *
     * @param userId             用户ID
     * @param followLiveStart    关注主播开播提醒
     * @param systemNotification 系统通知
     * @param giftNotification   礼物通知
     * @return 更新后的设置
     */
    UserNotificationSetting updateUserSetting(Long userId, Integer followLiveStart,
                                               Integer systemNotification, Integer giftNotification);

    /**
     * 当主播开播时，向粉丝发送开播提醒
     *
     * @param live 直播信息
     */
    void sendLiveStartNotification(Live live);

    /**
     * 获取用户的开播提醒列表
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 提醒列表
     */
    List<LiveStartNotification> getUserNotifications(Long userId, int limit);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadNotificationCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean markNotificationAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新的数量
     */
    int markAllNotificationsAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long userId);
}
