package com.zhibo.backend.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    
    // 在线用户数
    private final AtomicInteger onlineUserCount = new AtomicInteger(0);
    
    // 直播流数量
    private final AtomicInteger liveStreamCount = new AtomicInteger(0);
    
    // 弹幕数量
    private final AtomicInteger danmakuCount = new AtomicInteger(0);
    
    // 礼物数量
    private final AtomicInteger giftCount = new AtomicInteger(0);

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // 注册在线用户数指标
        Gauge.builder("user_online_count", onlineUserCount, AtomicInteger::get)
                .description("当前在线用户数")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);

        // 注册直播流数量指标
        Gauge.builder("live_stream_count", liveStreamCount, AtomicInteger::get)
                .description("当前直播流数量")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);

        // 注册弹幕数量指标
        Gauge.builder("danmaku_count", danmakuCount, AtomicInteger::get)
                .description("当前弹幕数量")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);

        // 注册礼物数量指标
        Gauge.builder("gift_count", giftCount, AtomicInteger::get)
                .description("当前礼物数量")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);

        // 注册系统CPU使用率指标
        Gauge.builder("system_cpu_usage", () -> {
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemCpuLoad();
        })
                .description("系统CPU使用率")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);

        // 注册系统内存使用率指标
        Gauge.builder("system_memory_usage", () -> {
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            long totalMemory = osBean.getTotalPhysicalMemorySize();
            long freeMemory = osBean.getFreePhysicalMemorySize();
            return (double) (totalMemory - freeMemory) / totalMemory;
        })
                .description("系统内存使用率")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);

        // 注册系统磁盘使用率指标
        Gauge.builder("system_disk_usage", () -> {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            return (double) (totalSpace - freeSpace) / totalSpace;
        })
                .description("系统磁盘使用率")
                .tag("application", "zhibo-backend")
                .register(meterRegistry);
    }

    // 增加在线用户数
    public void incrementOnlineUserCount() {
        onlineUserCount.incrementAndGet();
    }

    // 减少在线用户数
    public void decrementOnlineUserCount() {
        if (onlineUserCount.get() > 0) {
            onlineUserCount.decrementAndGet();
        }
    }

    // 获取在线用户数
    public int getOnlineUserCount() {
        return onlineUserCount.get();
    }

    // 增加直播流数量
    public void incrementLiveStreamCount() {
        liveStreamCount.incrementAndGet();
    }

    // 减少直播流数量
    public void decrementLiveStreamCount() {
        if (liveStreamCount.get() > 0) {
            liveStreamCount.decrementAndGet();
        }
    }

    // 获取直播流数量
    public int getLiveStreamCount() {
        return liveStreamCount.get();
    }

    // 增加弹幕数量
    public void incrementDanmakuCount() {
        danmakuCount.incrementAndGet();
    }

    // 获取弹幕数量
    public int getDanmakuCount() {
        return danmakuCount.get();
    }

    // 增加礼物数量
    public void incrementGiftCount() {
        giftCount.incrementAndGet();
    }

    // 获取礼物数量
    public int getGiftCount() {
        return giftCount.get();
    }
}
