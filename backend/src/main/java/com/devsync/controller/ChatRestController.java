package com.devsync.controller;

import com.devsync.dto.ChatMessageDto;
import com.devsync.entity.Message;
import com.devsync.entity.User;
import com.devsync.security.UserDetailsImpl;
import com.devsync.service.ChatService;
import com.devsync.service.UserService;
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
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @GetMapping("/channels/{channelId}/messages")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = chatService.getChannelMessages(channelId, pageable);
            
            Page<ChatMessageDto> messageDtos = messages.map(ChatMessageDto::fromEntity);
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching channel messages: " + e.getMessage());
        }
    }

    @GetMapping("/direct/{userId}/messages")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getDirectMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = chatService.getDirectMessages(userDetails.getId(), userId, pageable);
            
            Page<ChatMessageDto> messageDtos = messages.map(ChatMessageDto::fromEntity);
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching direct messages: " + e.getMessage());
        }
    }

    @PostMapping("/messages")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody ChatMessageDto chatMessageDto,
            Authentication authentication) {
        
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User sender = userService.findById(userDetails.getId());
            
            Message savedMessage = chatService.saveMessage(chatMessageDto, sender);
            ChatMessageDto responseDto = ChatMessageDto.fromEntity(savedMessage);
            
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending message: " + e.getMessage());
        }
    }

    @GetMapping("/messages/{messageId}/replies")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getThreadReplies(@PathVariable Long messageId) {
        try {
            List<Message> replies = chatService.getThreadReplies(messageId);
            List<ChatMessageDto> replyDtos = replies.stream()
                    .map(ChatMessageDto::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(replyDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching thread replies: " + e.getMessage());
        }
    }
}