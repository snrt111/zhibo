package com.zhibo.backend.controller;

import com.zhibo.backend.service.FFmpegStreamService;
import com.zhibo.backend.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(StreamWebSocketHandler.class);

    @Autowired
    private FFmpegStreamService ffmpegStreamService;

    @Autowired
    private MetricsService metricsService;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToStreamKey = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        // 增加在线用户数
        metricsService.incrementOnlineUserCount();
        logger.info("WebSocket connection established: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String sessionId = session.getId();

        if (payload.startsWith("START:")) {
            String streamKey = payload.substring(6);
            sessionToStreamKey.put(sessionId, streamKey);
            
            boolean started = ffmpegStreamService.startBrowserStreaming(streamKey);
            if (started) {
                session.sendMessage(new TextMessage("STARTED:" + streamKey));
                logger.info("Stream started for session: {}, streamKey: {}", sessionId, streamKey);
            } else {
                session.sendMessage(new TextMessage("ERROR:Failed to start stream"));
            }
        } else if (payload.equals("STOP")) {
            String streamKey = sessionToStreamKey.get(sessionId);
            if (streamKey != null) {
                ffmpegStreamService.stopBrowserStreaming(streamKey);
                sessionToStreamKey.remove(sessionId);
                session.sendMessage(new TextMessage("STOPPED"));
                logger.info("Stream stopped for session: {}, streamKey: {}", sessionId, streamKey);
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        String streamKey = sessionToStreamKey.get(sessionId);

        if (streamKey == null) {
            logger.warn("Binary message received without active stream for session: {}", sessionId);
            return;
        }

        byte[] data = new byte[message.getPayloadLength()];
        message.getPayload().get(data);
        
        ffmpegStreamService.writeData(streamKey, data);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        String streamKey = sessionToStreamKey.get(sessionId);

        if (streamKey != null) {
            ffmpegStreamService.stopBrowserStreaming(streamKey);
            sessionToStreamKey.remove(sessionId);
        }

        sessions.remove(sessionId);
        // 减少在线用户数
        metricsService.decrementOnlineUserCount();
        logger.info("WebSocket connection closed: {}, status: {}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session: {}", session.getId(), exception);
    }
}