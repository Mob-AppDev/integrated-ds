package com.devsync.service;

import com.devsync.dto.ChatMessageDto;
import com.devsync.entity.*;
import com.devsync.repository.ChannelRepository;
import com.devsync.repository.MessageRepository;
import com.devsync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PresenceService presenceService;

    public Message saveMessage(ChatMessageDto chatMessageDto, User sender) {
        Message message = new Message();
        message.setContent(chatMessageDto.getContent());
        message.setSender(sender);
        message.setType(MessageType.valueOf(chatMessageDto.getMessageType()));
        message.setCreatedAt(LocalDateTime.now());

        if ("DIRECT".equals(chatMessageDto.getType())) {
            // Direct message
            Optional<User> recipient = userRepository.findById(chatMessageDto.getRecipientId());
            if (recipient.isPresent()) {
                message.setRecipient(recipient.get());
            } else {
                throw new RuntimeException("Recipient not found");
            }
        } else {
            // Channel message
            Optional<Channel> channel = channelRepository.findById(chatMessageDto.getChannelId());
            if (channel.isPresent()) {
                message.setChannel(channel.get());
                
                // Check if sender is member of channel
                if (!channel.get().getMembers().contains(sender)) {
                    throw new RuntimeException("User is not a member of this channel");
                }
            } else {
                throw new RuntimeException("Channel not found");
            }
        }

        return messageRepository.save(message);
    }

    public Page<Message> getChannelMessages(Long channelId, Pageable pageable) {
        Optional<Channel> channel = channelRepository.findById(channelId);
        if (channel.isPresent()) {
            return messageRepository.findByChannelOrderByCreatedAtDesc(channel.get(), pageable);
        }
        throw new RuntimeException("Channel not found");
    }

    public Page<Message> getDirectMessages(Long userId1, Long userId2, Pageable pageable) {
        Optional<User> user1 = userRepository.findById(userId1);
        Optional<User> user2 = userRepository.findById(userId2);
        
        if (user1.isPresent() && user2.isPresent()) {
            return messageRepository.findDirectMessages(user1.get(), user2.get(), pageable);
        }
        throw new RuntimeException("One or both users not found");
    }

    public void sendPushNotificationIfOffline(Message message) {
        if (message.getRecipient() != null) {
            // Check if recipient is online
            boolean isOnline = presenceService.isUserOnline(message.getRecipient().getId());
            if (!isOnline) {
                notificationService.sendDirectMessageNotification(
                    message.getRecipient(), 
                    message.getSender(), 
                    message.getContent()
                );
            }
        }
    }

    public void sendChannelPushNotifications(Message message) {
        if (message.getChannel() != null) {
            List<User> offlineMembers = presenceService.getOfflineChannelMembers(message.getChannel().getId());
            for (User member : offlineMembers) {
                if (!member.getId().equals(message.getSender().getId())) {
                    notificationService.sendChannelNotification(
                        message.getChannel(),
                        message.getSender(),
                        message.getContent(),
                        member
                    );
                }
            }
        }
    }

    public List<Message> getThreadReplies(Long parentMessageId) {
        return messageRepository.findThreadReplies(parentMessageId);
    }
}