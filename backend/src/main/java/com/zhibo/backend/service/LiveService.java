package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.mapper.LiveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LiveService extends ServiceImpl<LiveMapper, Live> {

    @Autowired
    private StreamService streamService;

    /**
     * 创建直播
     */
    public Live createLive(Live live) {
        // 生成唯一的推流密钥
        String streamKey = UUID.randomUUID().toString().replace("-", "");
        live.setStreamKey(streamKey);
        // 设置默认值
        live.setStatus(0); // 初始状态为未开始
        live.setViewCount(0L); // 初始观看人数为0
        // 保存直播
        save(live);
        return live;
    }

    /**
     * 开始直播
     */
    public boolean startLive(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        if (live.getStatus() != 0) {
            throw new RuntimeException("直播状态不正确");
        }
        // 更新直播状态
        live.setStatus(1); // 设置为直播中
        live.setStartTime(LocalDateTime.now());
        boolean updated = updateById(live);
        
        // 开始转码直播流
        if (updated) {
            streamService.startStreaming(live.getStreamKey());
        }
        return updated;
    }

    /**
     * 结束直播
     */
    public boolean endLive(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        if (live.getStatus() != 1) {
            throw new RuntimeException("直播状态不正确");
        }
        // 停止转码直播流
        streamService.stopStreaming(live.getStreamKey());
        
        // 更新直播状态
        live.setStatus(2); // 设置为已结束
        live.setEndTime(LocalDateTime.now());
        return updateById(live);
    }

    /**
     * 获取直播列表
     */
    public List<Live> getLiveList(Integer status) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Live::getStatus, status);
        }
        wrapper.orderByDesc(Live::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 根据用户ID获取直播列表
     */
    public List<Live> getLiveListByUserId(Long userId) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Live::getUserId, userId);
        wrapper.orderByDesc(Live::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 根据分类ID获取直播列表
     */
    public List<Live> getLiveListByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Live::getCategoryId, categoryId);
        wrapper.eq(Live::getStatus, 1); // 只获取直播中的
        wrapper.orderByDesc(Live::getViewCount);
        return list(wrapper);
    }

    /**
     * 增加观看人数
     */
    public boolean increaseViewCount(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            return false;
        }
        live.setViewCount(live.getViewCount() + 1);
        return updateById(live);
    }

    /**
     * 根据推流密钥获取直播
     */
    public Live getByStreamKey(String streamKey) {
        LambdaQueryWrapper<Live> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Live::getStreamKey, streamKey);
        return getOne(wrapper);
    }

    /**
     * 获取直播推流地址
     */
    public String getPushUrl(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        return streamService.getPushUrl(live.getStreamKey());
    }

    /**
     * 获取直播播放地址
     */
    public String getPlayUrl(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        return streamService.getPlayUrl(live.getStreamKey());
    }

    /**
     * 删除直播
     */
    public boolean deleteLive(Long liveId) {
        Live live = getById(liveId);
        if (live == null) {
            throw new RuntimeException("直播不存在");
        }
        if (live.getStatus() == 1) {
            throw new RuntimeException("直播正在进行中，无法删除");
        }
        return removeById(liveId);
    }
}