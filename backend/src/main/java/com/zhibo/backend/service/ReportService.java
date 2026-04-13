package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Comment;
import com.zhibo.backend.entity.Danmaku;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.Report;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.mapper.ReportMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportService extends ServiceImpl<ReportMapper, Report> {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private LiveService liveService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DanmakuService danmakuService;

    public Report createReport(Long reporterId, Integer targetType, Long targetId, String reason, String description, String evidence) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDescription(description);
        report.setEvidence(evidence);
        report.setStatus(0);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        save(report);
        logger.info("用户 {} 举报了 {} {}", reporterId, targetType == 1 ? "直播" : targetType == 2 ? "用户" : targetType == 3 ? "评论" : "弹幕", targetId);
        return report;
    }

    public boolean handleReport(Long reportId, Long handlerId, Integer status, String handleResult) {
        Report report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus() != 0) {
            throw new RuntimeException("该举报已处理");
        }

        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleTime(LocalDateTime.now());
        report.setHandleResult(handleResult);
        report.setUpdatedAt(LocalDateTime.now());

        updateById(report);

        if (status == 1) {
            executeHandleAction(report);
        }

        logger.info("处理举报 {}，状态：{}", reportId, status);
        return true;
    }

    private void executeHandleAction(Report report) {
        Integer targetType = report.getTargetType();
        Long targetId = report.getTargetId();

        switch (targetType) {
            case 1:
                handleLiveReport(targetId);
                break;
            case 2:
                handleUserReport(targetId);
                break;
            case 3:
                handleCommentReport(targetId);
                break;
            case 4:
                handleDanmakuReport(targetId);
                break;
            default:
                logger.warn("未知的举报类型：{}", targetType);
        }
    }

    private void handleLiveReport(Long liveId) {
        Live live = liveService.getById(liveId);
        if (live == null) {
            logger.warn("直播 {} 不存在，无法处理举报", liveId);
            return;
        }

        if (live.getStatus() == 1) {
            liveService.endLive(liveId);
            logger.info("已结束直播 {}", liveId);
        } else {
            liveService.deleteLive(liveId);
            logger.info("已删除直播 {}", liveId);
        }
    }

    private void handleUserReport(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            logger.warn("用户 {} 不存在，无法处理举报", userId);
            return;
        }

        user.setStatus(0);
        userService.updateById(user);
        logger.info("已禁用用户 {}", userId);
    }

    private void handleCommentReport(Long commentId) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            logger.warn("评论 {} 不存在，无法处理举报", commentId);
            return;
        }

        commentService.removeById(commentId);
        logger.info("已删除评论 {}", commentId);
    }

    private void handleDanmakuReport(Long danmakuId) {
        Danmaku danmaku = danmakuService.getById(danmakuId);
        if (danmaku == null) {
            logger.warn("弹幕 {} 不存在，无法处理举报", danmakuId);
            return;
        }

        danmakuService.removeById(danmakuId);
        logger.info("已删除弹幕 {}", danmakuId);
    }

    public Page<Report> getPendingList(int page, int size) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, 0);
        wrapper.orderByAsc(Report::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<Report> getReportList(int page, int size, Integer status, Integer targetType) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        if (targetType != null) {
            wrapper.eq(Report::getTargetType, targetType);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        return page(pageObj, wrapper);
    }

    public Page<Report> getMyReportList(Long reporterId, int page, int size) {
        Page<Report> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId);
        wrapper.orderByDesc(Report::getCreatedAt);
        return page(pageObj, wrapper);
    }
}