package com.devsync.controller;

import com.devsync.dto.FCMTokenDto;
import com.devsync.entity.User;
import com.devsync.payload.response.MessageResponse;
import com.devsync.repository.UserRepository;
import com.devsync.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fcm")
public class FCMController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/token")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateFCMToken(@Valid @RequestBody FCMTokenDto fcmTokenDto, 
                                           Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirebaseToken(fcmTokenDto.getToken());
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("FCM token updated successfully!"));
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/token")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> removeFCMToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirebaseToken(null);
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("FCM token removed successfully!"));
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getNotificationPreferences(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Return notification preferences (you can extend User entity to include these)
            return ResponseEntity.ok(user);
        }
        
        return ResponseEntity.notFound().build();
    }
}