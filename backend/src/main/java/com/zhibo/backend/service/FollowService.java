package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Follow;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.FollowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FollowService extends ServiceImpl<FollowMapper, Follow> {

    @Autowired
    private UserService userService;

    public boolean follow(Long userId, Long followedUserId) {
        if (userId.equals(followedUserId)) {
            throw new RuntimeException("不能关注自己");
        }
        
        User followedUser = userService.getById(followedUserId);
        if (followedUser == null) {
            throw new RuntimeException("关注的用户不存在");
        }
        
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getFollowedUserId, followedUserId);
        Follow existingFollow = getOne(wrapper);
        
        if (existingFollow != null) {
            throw new RuntimeException("已经关注过该用户");
        }
        
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowedUserId(followedUserId);
        follow.setCreatedAt(LocalDateTime.now());
        return save(follow);
    }

    public boolean unfollow(Long userId, Long followedUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getFollowedUserId, followedUserId);
        return remove(wrapper);
    }

    public boolean isFollowing(Long userId, Long followedUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.eq(Follow::getFollowedUserId, followedUserId);
        return count(wrapper) > 0;
    }

    public List<Map<String, Object>> getFollowingList(Long userId, int page, int size) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        wrapper.orderByDesc(Follow::getCreatedAt);
        wrapper.last("LIMIT " + page * size + ", " + size);
        
        List<Follow> follows = list(wrapper);
        
        List<Long> followedUserIds = follows.stream()
            .map(Follow::getFollowedUserId)
            .collect(Collectors.toList());
        
        if (followedUserIds.isEmpty()) {
            return List.of();
        }
        
        Map<Long, User> userMap = userService.listByIds(followedUserIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        
        return follows.stream()
            .map(follow -> {
                Map<String, Object> item = new HashMap<>();
                User user = userMap.get(follow.getFollowedUserId());
                item.put("id", follow.getId());
                item.put("followedUserId", follow.getFollowedUserId());
                item.put("followedUsername", user != null ? user.getUsername() : "未知用户");
                item.put("followedNickname", user != null ? user.getNickname() : "未知用户");
                item.put("followedAvatar", user != null ? user.getAvatar() : null);
                item.put("followedAt", follow.getCreatedAt());
                return item;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getFollowerList(Long userId, int page, int size) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowedUserId, userId);
        wrapper.orderByDesc(Follow::getCreatedAt);
        wrapper.last("LIMIT " + page * size + ", " + size);
        
        List<Follow> follows = list(wrapper);
        
        List<Long> userIds = follows.stream()
            .map(Follow::getUserId)
            .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return List.of();
        }
        
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        
        return follows.stream()
            .map(follow -> {
                Map<String, Object> item = new HashMap<>();
                User user = userMap.get(follow.getUserId());
                item.put("id", follow.getId());
                item.put("followerUserId", follow.getUserId());
                item.put("followerUsername", user != null ? user.getUsername() : "未知用户");
                item.put("followerNickname", user != null ? user.getNickname() : "未知用户");
                item.put("followerAvatar", user != null ? user.getAvatar() : null);
                item.put("followedAt", follow.getCreatedAt());
                return item;
            })
            .collect(Collectors.toList());
    }

    public long getFollowingCount(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        return count(wrapper);
    }

    public long getFollowerCount(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowedUserId, userId);
        return count(wrapper);
    }
}
