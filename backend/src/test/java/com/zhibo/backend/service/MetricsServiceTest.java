package com.zhibo.backend.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricsServiceTest {

    private MetricsService metricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        // 使用 SimpleMeterRegistry 进行测试
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    public void testOnlineUserCount() {
        // 初始值应该为 0
        assertEquals(0, metricsService.getOnlineUserCount());

        // 增加在线用户数
        metricsService.incrementOnlineUserCount();
        assertEquals(1, metricsService.getOnlineUserCount());

        // 再次增加
        metricsService.incrementOnlineUserCount();
        assertEquals(2, metricsService.getOnlineUserCount());

        // 减少在线用户数
        metricsService.decrementOnlineUserCount();
        assertEquals(1, metricsService.getOnlineUserCount());

        // 减少到 0 后再减少，应该保持为 0
        metricsService.decrementOnlineUserCount();
        assertEquals(0, metricsService.getOnlineUserCount());
        metricsService.decrementOnlineUserCount();
        assertEquals(0, metricsService.getOnlineUserCount());
    }

    @Test
    public void testLiveStreamCount() {
        // 初始值应该为 0
        assertEquals(0, metricsService.getLiveStreamCount());

        // 增加直播流数量
        metricsService.incrementLiveStreamCount();
        assertEquals(1, metricsService.getLiveStreamCount());

        // 再次增加
        metricsService.incrementLiveStreamCount();
        assertEquals(2, metricsService.getLiveStreamCount());

        // 减少直播流数量
        metricsService.decrementLiveStreamCount();
        assertEquals(1, metricsService.getLiveStreamCount());

        // 减少到 0 后再减少，应该保持为 0
        metricsService.decrementLiveStreamCount();
        assertEquals(0, metricsService.getLiveStreamCount());
        metricsService.decrementLiveStreamCount();
        assertEquals(0, metricsService.getLiveStreamCount());
    }

    @Test
    public void testDanmakuCount() {
        // 初始值应该为 0
        assertEquals(0, metricsService.getDanmakuCount());

        // 增加弹幕数量
        metricsService.incrementDanmakuCount();
        assertEquals(1, metricsService.getDanmakuCount());

        // 再次增加
        metricsService.incrementDanmakuCount();
        assertEquals(2, metricsService.getDanmakuCount());
    }

    @Test
    public void testGiftCount() {
        // 初始值应该为 0
        assertEquals(0, metricsService.getGiftCount());

        // 增加礼物数量
        metricsService.incrementGiftCount();
        assertEquals(1, metricsService.getGiftCount());

        // 再次增加
        metricsService.incrementGiftCount();
        assertEquals(2, metricsService.getGiftCount());
    }
}
