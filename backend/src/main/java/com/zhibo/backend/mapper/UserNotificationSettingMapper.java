package com.zhibo.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibo.backend.entity.UserNotificationSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知设置Mapper接口
 *
 * @author zhibo
 * @since 2026-02-25
 */
@Mapper
public interface UserNotificationSettingMapper extends BaseMapper<UserNotificationSetting> {
}
