package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Comment;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.service.CommentService;
import com.zhibo.backend.service.UserService;
import com.zhibo.backend.service.WebSocketBroadcastService;
import com.zhibo.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Comment> createComment(@RequestHeader("Authorization") String authorization, @RequestBody Comment comment) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        comment.setUserId(userId);
        Comment createdComment = commentService.createComment(comment);
        
        User user = userService.getById(userId);
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("id", createdComment.getId());
        commentData.put("userId", userId);
        commentData.put("username", user != null ? user.getUsername() : "匿名用户");
        commentData.put("nickname", user != null ? user.getNickname() : "匿名用户");
        commentData.put("userAvatar", user != null ? user.getAvatar() : null);
        commentData.put("content", createdComment.getContent());
        commentData.put("likeCount", 0);
        commentData.put("createdAt", createdComment.getCreatedAt().toString());
        
        System.out.println("准备广播评论，liveId: " + comment.getLiveId() + ", data: " + commentData);
        webSocketBroadcastService.broadcastComment(comment.getLiveId(), commentData);
        
        return Result.success("评论成功", createdComment);
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@RequestHeader("Authorization") String authorization, @PathVariable Long commentId) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        if (userType != null && userType == 1) {
            commentService.removeById(commentId);
        } else {
            commentService.deleteComment(commentId, userId);
        }
        return Result.success("删除评论成功", null);
    }

    @PostMapping("/like/{commentId}")
    public Result<Void> likeComment(@PathVariable Long commentId) {
        commentService.likeComment(commentId);
        return Result.success("点赞成功", null);
    }

    @PostMapping("/unlike/{commentId}")
    public Result<Void> unlikeComment(@PathVariable Long commentId) {
        commentService.unlikeComment(commentId);
        return Result.success("取消点赞成功", null);
    }

    @GetMapping("/list/{liveId}")
    public Result<Map<String, Object>> getCommentList(@PathVariable Long liveId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> list = commentService.getCommentList(liveId, page, size);
        long total = commentService.getCommentCount(liveId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("totalPages", (total + size - 1) / size);
        data.put("currentPage", page);
        data.put("size", size);
        return Result.success("获取评论列表成功", data);
    }

    @GetMapping("/replies/{commentId}")
    public Result<Map<String, Object>> getReplyList(@PathVariable Long commentId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> list = commentService.getReplyList(commentId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("currentPage", page);
        data.put("size", size);
        return Result.success("获取回复列表成功", data);
    }
}
