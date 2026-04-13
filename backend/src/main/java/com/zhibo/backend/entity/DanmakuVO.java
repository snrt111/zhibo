package com.zhibo.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DanmakuVO {
    private Long id;
    private Long liveId;
    private Long userId;
    private String username;
    private String content;
    private String color;
    private Integer fontSize;
    private LocalDateTime createdAt;

    public static DanmakuVO fromDanmaku(Danmaku danmaku, String username) {
        DanmakuVO vo = new DanmakuVO();
        vo.setId(danmaku.getId());
        vo.setLiveId(danmaku.getLiveId());
        vo.setUserId(danmaku.getUserId());
        vo.setUsername(username);
        vo.setContent(danmaku.getContent());
        vo.setColor(danmaku.getColor());
        vo.setFontSize(danmaku.getFontSize());
        vo.setCreatedAt(danmaku.getCreatedAt());
        return vo;
    }
}
