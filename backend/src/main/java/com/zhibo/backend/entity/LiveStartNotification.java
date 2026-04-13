package com.zhibo.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开播提醒记录实体类
 * 存储主播开播时向粉丝发送的通知记录
 *
 * @author zhibo
 * @since 2026-02-25
 */
@Data
@TableName("live_start_notification")
public class LiveStartNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收通知的用户ID
     */
    private Long userId;

    /**
     * 主播ID
     */
    private Long anchorId;

    /**
     * 直播ID（开播后填充）
     */
    private Long liveId;

    /**
     * 直播标题
     */
    private String liveTitle;

    /**
     * 直播封面
     */
    private String liveCover;

    /**
     * 状态：0-待发送，1-已发送，2-已读
     */
    private Integer status;

    /**
     * 发送时间
     */
    private LocalDateTime sentAt;

    /**
     * 读取时间
     */
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
