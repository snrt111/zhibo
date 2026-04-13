package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.service.FollowService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{followedUserId}")
    public Result<Void> follow(@RequestHeader("Authorization") String authorization, @PathVariable Long followedUserId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        followService.follow(userId, followedUserId);
        return Result.success("关注成功", null);
    }

    @DeleteMapping("/{followedUserId}")
    public Result<Void> unfollow(@RequestHeader("Authorization") String authorization, @PathVariable Long followedUserId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        followService.unfollow(userId, followedUserId);
        return Result.success("取消关注成功", null);
    }

    @GetMapping("/status/{followedUserId}")
    public Result<Boolean> isFollowing(@RequestHeader("Authorization") String authorization, @PathVariable Long followedUserId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        boolean isFollowing = followService.isFollowing(userId, followedUserId);
        return Result.success("获取关注状态成功", isFollowing);
    }

    @GetMapping("/following")
    public Result<Map<String, Object>> getFollowingList(@RequestHeader("Authorization") String authorization,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Map<String, Object>> list = followService.getFollowingList(userId, page, size);
        long total = followService.getFollowingCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("totalPages", (total + size - 1) / size);
        data.put("currentPage", page);
        data.put("size", size);
        return Result.success("获取关注列表成功", data);
    }

    @GetMapping("/followers")
    public Result<Map<String, Object>> getFollowerList(@RequestHeader("Authorization") String authorization,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Map<String, Object>> list = followService.getFollowerList(userId, page, size);
        long total = followService.getFollowerCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("totalPages", (total + size - 1) / size);
        data.put("currentPage", page);
        data.put("size", size);
        return Result.success("获取粉丝列表成功", data);
    }

    @GetMapping("/count/{userId}")
    public Result<Map<String, Object>> getFollowCount(@PathVariable Long userId) {
        long followingCount = followService.getFollowingCount(userId);
        long followerCount = followService.getFollowerCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("followingCount", followingCount);
        data.put("followerCount", followerCount);
        return Result.success("获取关注统计成功", data);
    }
}
