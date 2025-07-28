package com.devsync.controller;

import com.devsync.dto.ChatMessageDto;
import com.devsync.dto.TypingIndicatorDto;
import com.devsync.entity.Message;
import com.devsync.entity.User;
import com.devsync.service.ChatService;
import com.devsync.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto chatMessage, Principal principal) {
        try {
            // Get sender user
            User sender = userService.findByUsername(principal.getName());
            
            // Save message to database
            Message savedMessage = chatService.saveMessage(chatMessage, sender);
            
            // Create response DTO
            ChatMessageDto responseMessage = ChatMessageDto.fromEntity(savedMessage);
            
            // Determine destination based on message type
            String destination;
            if (chatMessage.getType().equals("DIRECT")) {
                // Send to both sender and recipient for DM
                messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipientId().toString(), 
                    "/queue/messages", 
                    responseMessage
                );
                messagingTemplate.convertAndSendToUser(
                    sender.getId().toString(), 
                    "/queue/messages", 
                    responseMessage
                );
                
                // Send push notification if recipient is offline
                chatService.sendPushNotificationIfOffline(savedMessage);
            } else {
                // Send to channel
                destination = "/topic/channel." + chatMessage.getChannelId();
                messagingTemplate.convertAndSend(destination, responseMessage);
                
                // Send push notifications to offline channel members
                chatService.sendChannelPushNotifications(savedMessage);
            }
            
            logger.info("Message sent successfully from user: {}", principal.getName());
            
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicatorDto typingIndicator, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            typingIndicator.setUserId(user.getId());
            typingIndicator.setUsername(user.getUsername());
            typingIndicator.setTimestamp(LocalDateTime.now());
            
            String destination;
            if (typingIndicator.getType().equals("DIRECT")) {
                // Send typing indicator to specific user
                messagingTemplate.convertAndSendToUser(
                    typingIndicator.getTargetId().toString(),
                    "/queue/typing",
                    typingIndicator
                );
            } else {
                // Send typing indicator to channel
                destination = "/topic/typing." + typingIndicator.getTargetId();
                messagingTemplate.convertAndSend(destination, typingIndicator);
            }
            
        } catch (Exception e) {
            logger.error("Error handling typing indicator: {}", e.getMessage(), e);
        }
    }
}