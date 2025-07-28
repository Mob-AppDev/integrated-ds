package com.devsync.controller;

import com.devsync.entity.Channel;
import com.devsync.entity.User;
import com.devsync.payload.request.ChannelRequest;
import com.devsync.payload.response.MessageResponse;
import com.devsync.repository.ChannelRepository;
import com.devsync.repository.UserRepository;
import com.devsync.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Channel>> getUserChannels(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userRepository.findById(userDetails.getId());
        
        if (user.isPresent()) {
            List<Channel> channels = channelRepository.findAccessibleChannels(user.get());
            return ResponseEntity.ok(channels);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createChannel(
            @Valid @RequestBody ChannelRequest channelRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Channel channel = new Channel(
                channelRequest.getName(),
                channelRequest.getDescription(),
                channelRequest.getIsPrivate(),
                user
            );
            
            // Add creator as member
            channel.getMembers().add(user);
            
            Channel savedChannel = channelRepository.save(channel);
            return ResponseEntity.ok(savedChannel);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{channelId}/join")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> joinChannel(
            @PathVariable Long channelId,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        
        if (userOpt.isPresent() && channelOpt.isPresent()) {
            User user = userOpt.get();
            Channel channel = channelOpt.get();
            
            if (!channel.getIsPrivate() || channel.getMembers().contains(user)) {
                channel.getMembers().add(user);
                channelRepository.save(channel);
                return ResponseEntity.ok(new MessageResponse("Joined channel successfully!"));
            } else {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Cannot join private channel!"));
            }
        }
        
        return ResponseEntity.notFound().build();
    }
}