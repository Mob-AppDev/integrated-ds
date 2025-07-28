package com.devsync.service;

import com.devsync.entity.*;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void sendDirectMessageNotification(User recipient, User sender, String messageContent) {
        if (recipient.getFirebaseToken() == null || recipient.getFirebaseToken().isEmpty()) {
            logger.debug("No Firebase token found for user: {}", recipient.getUsername());
            return;
        }

        try {
            com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                    .setToken(recipient.getFirebaseToken())
                    .setNotification(Notification.builder()
                            .setTitle(sender.getUsername())
                            .setBody(truncateMessage(messageContent))
                            .setImage(sender.getProfilePicture())
                            .build())
                    .putData("type", "direct_message")
                    .putData("senderId", sender.getId().toString())
                    .putData("senderUsername", sender.getUsername())
                    .putData("recipientId", recipient.getId().toString())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            logger.debug("Successfully sent DM notification: {}", response);
        } catch (Exception e) {
            logger.error("Error sending direct message notification: {}", e.getMessage());
        }
    }

    public void sendChannelNotification(Channel channel, User sender, String messageContent, User recipient) {
        if (recipient.getFirebaseToken() == null || recipient.getFirebaseToken().isEmpty()) {
            logger.debug("No Firebase token found for user: {}", recipient.getUsername());
            return;
        }

        try {
            com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                    .setToken(recipient.getFirebaseToken())
                    .setNotification(Notification.builder()
                            .setTitle("#" + channel.getName())
                            .setBody(sender.getUsername() + ": " + truncateMessage(messageContent))
                            .setImage(sender.getProfilePicture())
                            .build())
                    .putData("type", "channel_message")
                    .putData("channelId", channel.getId().toString())
                    .putData("channelName", channel.getName())
                    .putData("senderId", sender.getId().toString())
                    .putData("senderUsername", sender.getUsername())
                    .putData("recipientId", recipient.getId().toString())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            logger.debug("Successfully sent channel notification: {}", response);
        } catch (Exception e) {
            logger.error("Error sending channel notification: {}", e.getMessage());
        }
    }

    public void sendMentionNotification(User mentionedUser, User sender, Channel channel, String messageContent) {
        if (mentionedUser.getFirebaseToken() == null || mentionedUser.getFirebaseToken().isEmpty()) {
            logger.warn("No Firebase token found for mentioned user: {}", mentionedUser.getUsername());
            return;
        }

        try {
            com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                    .setToken(mentionedUser.getFirebaseToken())
                    .setNotification(Notification.builder()
                            .setTitle("Mentioned in #" + channel.getName())
                            .setBody(sender.getUsername() + ": " + truncateMessage(messageContent))
                            .setImage(sender.getProfilePicture())
                            .build())
                    .putData("type", "mention")
                    .putData("channelId", channel.getId().toString())
                    .putData("channelName", channel.getName())
                    .putData("senderId", sender.getId().toString())
                    .putData("senderUsername", sender.getUsername())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            logger.debug("Successfully sent mention notification: {}", response);
        } catch (Exception e) {
            logger.error("Error sending mention notification: {}", e.getMessage());
        }
    }

    private String truncateMessage(String content) {
        if (content.length() > 100) {
            return content.substring(0, 97) + "...";
        }
        return content;
    }
}