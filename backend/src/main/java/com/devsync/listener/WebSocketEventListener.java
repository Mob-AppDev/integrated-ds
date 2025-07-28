package com.devsync.listener;

import com.devsync.security.UserDetailsImpl;
import com.devsync.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private PresenceService presenceService;


    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            
            String sessionId = headerAccessor.getSessionId();
            Long userId = userDetails.getId();
            
            presenceService.userConnected(userId, sessionId);
            logger.info("User connected: {} with session: {}", userDetails.getUsername(), sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            
            Long userId = userDetails.getId();
            presenceService.userDisconnected(userId);
            logger.info("User disconnected: {}", userDetails.getUsername());
        }
    }
}