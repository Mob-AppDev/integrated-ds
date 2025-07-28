package com.devsync.dto;

import com.devsync.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private Long id;
    private String content;
    private String type; // "CHANNEL" or "DIRECT"
    private String messageType; // "TEXT", "IMAGE", "FILE", etc.
    private Long senderId;
    private String senderUsername;
    private String senderAvatar;
    private Long channelId;
    private String channelName;
    private Long recipientId;
    private String recipientUsername;
    private Long parentMessageId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructors
    public ChatMessageDto() {}

    public static ChatMessageDto fromEntity(Message message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getType().toString());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setSenderAvatar(message.getSender().getProfilePicture());
        dto.setTimestamp(message.getCreatedAt());
        
        if (message.getChannel() != null) {
            dto.setType("CHANNEL");
            dto.setChannelId(message.getChannel().getId());
            dto.setChannelName(message.getChannel().getName());
        } else if (message.getRecipient() != null) {
            dto.setType("DIRECT");
            dto.setRecipientId(message.getRecipient().getId());
            dto.setRecipientUsername(message.getRecipient().getUsername());
        }
        
        if (message.getParentMessage() != null) {
            dto.setParentMessageId(message.getParentMessage().getId());
        }
        
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public Long getParentMessageId() { return parentMessageId; }
    public void setParentMessageId(Long parentMessageId) { this.parentMessageId = parentMessageId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}