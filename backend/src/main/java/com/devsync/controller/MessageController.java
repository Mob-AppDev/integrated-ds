package com.devsync.controller;

import com.devsync.entity.Channel;
import com.devsync.entity.Message;
import com.devsync.entity.User;
import com.devsync.payload.request.MessageRequest;
import com.devsync.repository.ChannelRepository;
import com.devsync.repository.MessageRepository;
import com.devsync.repository.UserRepository;
import com.devsync.security.UserDetailsImpl;
import com.devsync.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/channel/{channelId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<Message>> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        if (channelOpt.isPresent()) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageRepository.findByChannelOrderByCreatedAtDesc(channelOpt.get(), pageable);
            return ResponseEntity.ok(messages);
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/direct/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<Message>> getDirectMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> currentUser = userRepository.findById(userDetails.getId());
        Optional<User> otherUser = userRepository.findById(userId);
        
        if (currentUser.isPresent() && otherUser.isPresent()) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageRepository.findDirectMessages(
                currentUser.get(), otherUser.get(), pageable);
            return ResponseEntity.ok(messages);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/channel/{channelId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> sendChannelMessage(
            @PathVariable Long channelId,
            @Valid @RequestBody MessageRequest messageRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        
        if (userOpt.isPresent() && channelOpt.isPresent()) {
            User user = userOpt.get();
            Channel channel = channelOpt.get();
            
            // Check if user is member of channel
            if (!channel.getMembers().contains(user)) {
                return ResponseEntity.badRequest().build();
            }
            
            Message message = new Message(messageRequest.getContent(), user, channel);
            if (messageRequest.getType() != null) {
                message.setType(messageRequest.getType());
            }
            
            Message savedMessage = messageRepository.save(message);
            
            // Send push notifications to channel members
            for (User member : channel.getMembers()) {
                if (!member.getId().equals(user.getId())) { // Don't send notification to sender
                    notificationService.sendChannelNotification(channel, user, messageRequest.getContent(), member);
                }
            }
            
            return ResponseEntity.ok(savedMessage);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/direct/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> sendDirectMessage(
            @PathVariable Long userId,
            @Valid @RequestBody MessageRequest messageRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> senderOpt = userRepository.findById(userDetails.getId());
        Optional<User> recipientOpt = userRepository.findById(userId);
        
        if (senderOpt.isPresent() && recipientOpt.isPresent()) {
            User sender = senderOpt.get();
            User recipient = recipientOpt.get();
            
            Message message = new Message(messageRequest.getContent(), sender, recipient);
            if (messageRequest.getType() != null) {
                message.setType(messageRequest.getType());
            }
            
            Message savedMessage = messageRepository.save(message);
            
            // Send push notification to recipient
            notificationService.sendDirectMessageNotification(recipient, sender, messageRequest.getContent());
            
            return ResponseEntity.ok(savedMessage);
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{messageId}/replies")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Message>> getThreadReplies(@PathVariable Long messageId) {
        List<Message> replies = messageRepository.findThreadReplies(messageId);
        return ResponseEntity.ok(replies);
    }
}