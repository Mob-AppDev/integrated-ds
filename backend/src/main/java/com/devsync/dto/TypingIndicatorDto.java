package com.devsync.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class TypingIndicatorDto {
    private Long userId;
    private String username;
    private String type; // "CHANNEL" or "DIRECT"
    private Long targetId; // channelId or recipientId
    private boolean isTyping;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructors
    public TypingIndicatorDto() {}

    public TypingIndicatorDto(Long userId, String username, String type, Long targetId, boolean isTyping) {
        this.userId = userId;
        this.username = username;
        this.type = type;
        this.targetId = targetId;
        this.isTyping = isTyping;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}