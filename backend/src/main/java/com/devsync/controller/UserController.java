package com.devsync.controller;

import com.devsync.entity.User;
import com.devsync.entity.UserStatus;
import com.devsync.payload.response.MessageResponse;
import com.devsync.repository.UserRepository;
import com.devsync.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userRepository.findById(userDetails.getId());
        
        if (user.isPresent()) {
            User currentUser = user.get();
            // Remove password from response
            currentUser.setPassword(null);
            return ResponseEntity.ok(currentUser);
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/online")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<User>> getOnlineUsers() {
        List<User> onlineUsers = userRepository.findOnlineUsers();
        // Remove passwords from response
        onlineUsers.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(onlineUsers);
    }

    @PutMapping("/status")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @RequestParam UserStatus status,
            @RequestParam(required = false) Boolean isOnline,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status);
            if (isOnline != null) {
                user.setIsOnline(isOnline);
            }
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("Status updated successfully!"));
        }
        
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/firebase-token")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateFirebaseToken(
            @RequestParam String token,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userRepository.updateFirebaseToken(userDetails.getId(), token);
        
        return ResponseEntity.ok(new MessageResponse("Firebase token updated successfully!"));
    }
}