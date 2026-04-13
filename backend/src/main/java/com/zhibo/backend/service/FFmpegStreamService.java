package com.zhibo.backend.service;

import com.zhibo.backend.config.StreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FFmpegStreamService {

    private static final Logger logger = LoggerFactory.getLogger(FFmpegStreamService.class);

    @Autowired
    private StreamConfig streamConfig;

    private final Map<String, Process> ffmpegProcesses = new ConcurrentHashMap<>();
    private final Map<String, Boolean> streamingStatus = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public boolean startBrowserStreaming(String streamKey) {
        if (ffmpegProcesses.containsKey(streamKey)) {
            logger.warn("Stream {} is already running", streamKey);
            return false;
        }

        try {
            String rtmpUrl = streamConfig.getPushUrl(streamKey);
            
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", "pipe:0",
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-tune", "zerolatency",
                "-c:a", "aac",
                "-f", "flv",
                rtmpUrl
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            ffmpegProcesses.put(streamKey, process);
            streamingStatus.put(streamKey, true);
            
            executorService.submit(() -> monitorProcess(streamKey, process));
            
            logger.info("FFmpeg process started for stream: {}", streamKey);
            return true;
        } catch (IOException e) {
            logger.error("Failed to start FFmpeg for stream {}: {}", streamKey, e.getMessage());
            return false;
        }
    }

    public void writeData(String streamKey, byte[] data) {
        Process process = ffmpegProcesses.get(streamKey);
        if (process == null || !process.isAlive()) {
            logger.warn("FFmpeg process not found or not alive for stream: {}", streamKey);
            return;
        }

        try {
            process.getOutputStream().write(data);
            process.getOutputStream().flush();
        } catch (IOException e) {
            logger.error("Error writing data to FFmpeg for stream {}: {}", streamKey, e.getMessage());
        }
    }

    public void stopBrowserStreaming(String streamKey) {
        Process process = ffmpegProcesses.get(streamKey);
        if (process != null) {
            try {
                process.getOutputStream().close();
            } catch (IOException e) {
                logger.error("Error closing FFmpeg input stream for {}: {}", streamKey, e.getMessage());
            }
            
            process.destroy();
            try {
                process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
            
            ffmpegProcesses.remove(streamKey);
            streamingStatus.remove(streamKey);
            logger.info("FFmpeg process stopped for stream: {}", streamKey);
        }
    }

    public boolean isStreaming(String streamKey) {
        Process process = ffmpegProcesses.get(streamKey);
        return process != null && process.isAlive();
    }

    private void monitorProcess(String streamKey, Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("[FFmpeg-{}] {}", streamKey, line);
            }
        } catch (IOException e) {
            logger.debug("FFmpeg output stream closed for {}", streamKey);
        }

        try {
            int exitCode = process.waitFor();
            logger.info("FFmpeg process for {} exited with code: {}", streamKey, exitCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ffmpegProcesses.remove(streamKey);
        streamingStatus.remove(streamKey);
    }

    public void shutdown() {
        for (String streamKey : ffmpegProcesses.keySet()) {
            stopBrowserStreaming(streamKey);
        }
        executorService.shutdown();
    }
}