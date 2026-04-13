package com.zhibo.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibo.backend.entity.LiveStartNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 开播提醒记录Mapper接口
 *
 * @author zhibo
 * @since 2026-02-25
 */
@Mapper
public interface LiveStartNotificationMapper extends BaseMapper<LiveStartNotification> {

    /**
     * 获取用户的未读开播提醒数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    @Select("SELECT COUNT(*) FROM live_start_notification WHERE user_id = #{userId} AND status = 1")
    int getUnreadCount(@Param("userId") Long userId);

    /**
     * 获取用户的开播提醒列表
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 提醒列表
     */
    @Select("SELECT * FROM live_start_notification WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<LiveStartNotification> getUserNotifications(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 标记所有未读通知为已读
     *
     * @param userId 用户ID
     * @return 更新数量
     */
    @Update("UPDATE live_start_notification SET status = 2, read_at = NOW() WHERE user_id = #{userId} AND status = 1")
    int markAllAsRead(@Param("userId") Long userId);
}
