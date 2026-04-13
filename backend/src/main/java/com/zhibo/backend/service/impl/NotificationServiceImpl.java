package com.zhibo.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Follow;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.LiveStartNotification;
import com.zhibo.backend.entity.UserNotificationSetting;
import com.zhibo.backend.mapper.FollowMapper;
import com.zhibo.backend.mapper.LiveStartNotificationMapper;
import com.zhibo.backend.mapper.UserNotificationSettingMapper;
import com.zhibo.backend.service.NotificationService;
import com.zhibo.backend.service.WebSocketBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务实现类
 *
 * @author zhibo
 * @since 2026-02-25
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<UserNotificationSettingMapper, UserNotificationSetting>
        implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private UserNotificationSettingMapper userNotificationSettingMapper;

    @Autowired
    private LiveStartNotificationMapper liveStartNotificationMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;

    @Override
    public UserNotificationSetting getOrCreateUserSetting(Long userId) {
        LambdaQueryWrapper<UserNotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotificationSetting::getUserId, userId);
        UserNotificationSetting setting = userNotificationSettingMapper.selectOne(wrapper);

        if (setting == null) {
            setting = new UserNotificationSetting();
            setting.setUserId(userId);
            setting.setFollowLiveStart(1);
            setting.setSystemNotification(1);
            setting.setGiftNotification(1);
            setting.setCreatedAt(LocalDateTime.now());
            setting.setUpdatedAt(LocalDateTime.now());
            userNotificationSettingMapper.insert(setting);
            logger.info("为用户 {} 创建默认通知设置", userId);
        }

        return setting;
    }

    @Override
    @Transactional
    public UserNotificationSetting updateUserSetting(Long userId, Integer followLiveStart,
                                                      Integer systemNotification, Integer giftNotification) {
        UserNotificationSetting setting = getOrCreateUserSetting(userId);

        if (followLiveStart != null) {
            setting.setFollowLiveStart(followLiveStart);
        }
        if (systemNotification != null) {
            setting.setSystemNotification(systemNotification);
        }
        if (giftNotification != null) {
            setting.setGiftNotification(giftNotification);
        }
        setting.setUpdatedAt(LocalDateTime.now());

        userNotificationSettingMapper.updateById(setting);
        logger.info("用户 {} 更新通知设置", userId);

        return setting;
    }

    @Override
    @Transactional
    public void sendLiveStartNotification(Live live) {
        if (live == null || live.getUserId() == null) {
            return;
        }

        Long anchorId = live.getUserId();

        // 获取主播的所有粉丝
        LambdaQueryWrapper<Follow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(Follow::getFollowedUserId, anchorId);
        List<Follow> followers = followMapper.selectList(followWrapper);

        if (followers.isEmpty()) {
            logger.info("主播 {} 没有粉丝，跳过开播提醒", anchorId);
            return;
        }

        logger.info("主播 {} 开播，准备向 {} 个粉丝发送提醒", anchorId, followers.size());

        int notificationCount = 0;
        for (Follow follow : followers) {
            Long userId = follow.getUserId();

            // 检查用户是否开启了开播提醒
            UserNotificationSetting setting = getOrCreateUserSetting(userId);
            if (setting.getFollowLiveStart() == null || setting.getFollowLiveStart() == 0) {
                continue;
            }

            // 创建开播提醒记录
            LiveStartNotification notification = new LiveStartNotification();
            notification.setUserId(userId);
            notification.setAnchorId(anchorId);
            notification.setLiveId(live.getId());
            notification.setLiveTitle(live.getTitle());
            notification.setLiveCover(live.getCover());
            notification.setStatus(1); // 已发送
            notification.setSentAt(LocalDateTime.now());
            notification.setCreatedAt(LocalDateTime.now());

            liveStartNotificationMapper.insert(notification);
            notificationCount++;

            // 通过WebSocket实时推送通知
            try {
                webSocketBroadcastService.sendNotificationToUser(userId, buildNotificationMessage(notification));
            } catch (Exception e) {
                logger.error("发送WebSocket通知失败，用户ID: {}", userId, e);
            }
        }

        logger.info("成功发送 {} 个开播提醒", notificationCount);
    }

    /**
     * 构建通知消息
     */
    private String buildNotificationMessage(LiveStartNotification notification) {
        return String.format("您关注的主播开播了：%s", notification.getLiveTitle());
    }

    @Override
    public List<LiveStartNotification> getUserNotifications(Long userId, int limit) {
        return liveStartNotificationMapper.getUserNotifications(userId, limit);
    }

    @Override
    public int getUnreadNotificationCount(Long userId) {
        return liveStartNotificationMapper.getUnreadCount(userId);
    }

    @Override
    @Transactional
    public boolean markNotificationAsRead(Long notificationId, Long userId) {
        LiveStartNotification notification = liveStartNotificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }

        notification.setStatus(2); // 已读
        notification.setReadAt(LocalDateTime.now());
        liveStartNotificationMapper.updateById(notification);

        return true;
    }

    @Override
    @Transactional
    public int markAllNotificationsAsRead(Long userId) {
        return liveStartNotificationMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        LiveStartNotification notification = liveStartNotificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }

        liveStartNotificationMapper.deleteById(notificationId);
        return true;
    }
}
