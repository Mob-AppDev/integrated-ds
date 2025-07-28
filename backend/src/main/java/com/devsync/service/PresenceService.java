package com.devsync.service;

import com.devsync.dto.PresenceUpdateDto;
import com.devsync.entity.User;
import com.devsync.entity.UserStatus;
import com.devsync.repository.ChannelRepository;
import com.devsync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    // In-memory store for online users (in production, use Redis)
    private final Map<Long, String> onlineUsers = new ConcurrentHashMap<>();

    public void userConnected(Long userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        updateUserOnlineStatus(userId, true);
        broadcastPresenceUpdate(userId, true);
    }

    public void userDisconnected(Long userId) {
        onlineUsers.remove(userId);
        updateUserOnlineStatus(userId, false);
        broadcastPresenceUpdate(userId, false);
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    public List<User> getOfflineChannelMembers(Long channelId) {
        return channelRepository.findOfflineChannelMembers(channelId, onlineUsers.keySet());
    }

    private void updateUserOnlineStatus(Long userId, boolean isOnline) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(isOnline);
            user.setLastSeen(LocalDateTime.now());
            if (isOnline) {
                user.setStatus(UserStatus.ACTIVE);
            }
            userRepository.save(user);
        });
    }

    private void broadcastPresenceUpdate(Long userId, boolean isOnline) {
        userRepository.findById(userId).ifPresent(user -> {
            PresenceUpdateDto presenceUpdate = new PresenceUpdateDto();
            presenceUpdate.setUserId(userId);
            presenceUpdate.setUsername(user.getUsername());
            presenceUpdate.setOnline(isOnline);
            presenceUpdate.setStatus(user.getStatus().toString());
            presenceUpdate.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/presence", presenceUpdate);
        });
    }

    public void updateUserStatus(Long userId, UserStatus status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(status);
            userRepository.save(user);
            broadcastPresenceUpdate(userId, isUserOnline(userId));
        });
    }
}