package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Comment;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    @Autowired
    private UserService userService;
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Autowired
    private ContentAuditService contentAuditService;

    public Comment createComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        
        String originalContent = comment.getContent().trim();
        
        ContentAuditService.AuditResult auditResult = contentAuditService.checkContent(originalContent);
        
        if (auditResult.shouldBlock) {
            throw new RuntimeException("评论内容包含违规信息，无法发布");
        }
        
        String filteredContent = originalContent;
        if (auditResult.shouldFilter) {
            filteredContent = sensitiveWordService.filterSensitiveWords(originalContent);
        }
        comment.setContent(filteredContent);
        
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        comment.setLikeCount(0);
        comment.setStatus(auditResult.status == 0 ? 0 : 1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        save(comment);
        
        contentAuditService.saveAuditRecord(3, comment.getId(), originalContent, auditResult);
        
        return comment;
    }

    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        return removeById(commentId);
    }

    public boolean likeComment(Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        comment.setLikeCount(comment.getLikeCount() + 1);
        return updateById(comment);
    }

    public boolean unlikeComment(Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
        }
        return updateById(comment);
    }

    public List<Map<String, Object>> getCommentList(Long liveId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getLiveId, liveId);
        wrapper.eq(Comment::getParentId, 0);
        wrapper.eq(Comment::getStatus, 1);
        wrapper.orderByDesc(Comment::getCreatedAt);
        wrapper.last("LIMIT " + page * size + ", " + size);
        
        List<Comment> comments = list(wrapper);
        
        List<Long> userIds = comments.stream()
            .map(Comment::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return List.of();
        }
        
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        
        return comments.stream()
            .map(comment -> {
                Map<String, Object> item = new HashMap<>();
                User user = userMap.get(comment.getUserId());
                item.put("id", comment.getId());
                item.put("userId", comment.getUserId());
                item.put("username", user != null ? user.getUsername() : "匿名用户");
                item.put("nickname", user != null ? user.getNickname() : "匿名用户");
                item.put("avatar", user != null ? user.getAvatar() : null);
                item.put("content", comment.getContent());
                item.put("likeCount", comment.getLikeCount());
                item.put("createdAt", comment.getCreatedAt());
                item.put("replyCount", getReplyCount(comment.getId()));
                return item;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getReplyList(Long commentId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        wrapper.eq(Comment::getStatus, 1);
        wrapper.orderByAsc(Comment::getCreatedAt);
        wrapper.last("LIMIT " + page * size + ", " + size);
        
        List<Comment> replies = list(wrapper);
        
        List<Long> userIds = replies.stream()
            .map(Comment::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return List.of();
        }
        
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        
        return replies.stream()
            .map(comment -> {
                Map<String, Object> item = new HashMap<>();
                User user = userMap.get(comment.getUserId());
                item.put("id", comment.getId());
                item.put("userId", comment.getUserId());
                item.put("username", user != null ? user.getUsername() : "匿名用户");
                item.put("nickname", user != null ? user.getNickname() : "匿名用户");
                item.put("avatar", user != null ? user.getAvatar() : null);
                item.put("content", comment.getContent());
                item.put("likeCount", comment.getLikeCount());
                item.put("createdAt", comment.getCreatedAt());
                return item;
            })
            .collect(Collectors.toList());
    }

    private int getReplyCount(Long commentId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        wrapper.eq(Comment::getStatus, 1);
        return (int) count(wrapper);
    }

    public long getCommentCount(Long liveId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getLiveId, liveId);
        wrapper.eq(Comment::getStatus, 1);
        return count(wrapper);
    }
}
