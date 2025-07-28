package com.devsync.payload.request;

import com.devsync.entity.MessageType;
import jakarta.validation.constraints.NotBlank;

public class MessageRequest {
    @NotBlank
    private String content;

    private MessageType type = MessageType.TEXT;

    private Long parentMessageId; // For thread replies

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Long getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
    }
}