package com.zhibo.backend.service;

import com.zhibo.backend.config.StreamConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StreamService {

    @Autowired
    private StreamConfig streamConfig;

    public boolean startStreaming(String streamKey) {
        return true;
    }

    public boolean stopStreaming(String streamKey) {
        return true;
    }

    public boolean isStreaming(String streamKey) {
        return true;
    }

    public String getPushUrl(String streamKey) {
        return streamConfig.getPushUrl(streamKey);
    }

    public String getPlayUrl(String streamKey) {
        return streamConfig.getPlayUrl(streamKey);
    }
}
