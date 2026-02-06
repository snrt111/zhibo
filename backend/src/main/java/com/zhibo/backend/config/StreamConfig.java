package com.zhibo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamConfig {

    // RTMP 服务器地址
    @Value("${stream.rtmp.server:rtmp://localhost:1935/live}")
    private String rtmpServer;

    // HLS 播放地址
    @Value("${stream.hls.server:http://localhost:8080/hls}")
    private String hlsServer;

    // FFmpeg 可执行文件路径
    @Value("${stream.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    // 直播流存储路径
    @Value("${stream.storage.path:./stream}")
    private String storagePath;

    // 获取推流地址
    public String getPushUrl(String streamKey) {
        return rtmpServer + "/" + streamKey;
    }

    // 获取播放地址
    public String getPlayUrl(String streamKey) {
        return hlsServer + "/" + streamKey + ".m3u8";
    }

    // 获取FFmpeg路径
    public String getFfmpegPath() {
        return ffmpegPath;
    }

    // 获取存储路径
    public String getStoragePath() {
        return storagePath;
    }
}