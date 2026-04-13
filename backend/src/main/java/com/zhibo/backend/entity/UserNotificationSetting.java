package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户通知设置实体类
 * 存储用户的各类通知偏好设置
 *
 * @author zhibo
 * @since 2026-02-25
 */
@Data
@TableName("user_notification_setting")
public class UserNotificationSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关注主播开播提醒：0-关闭，1-开启
     */
    private Integer followLiveStart;

    /**
     * 系统通知：0-关闭，1-开启
     */
    private Integer systemNotification;

    /**
     * 礼物通知：0-关闭，1-开启
     */
    private Integer giftNotification;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
