package com.devsync.service;

import com.devsync.entity.ERole;
import com.devsync.entity.Role;
import com.devsync.entity.User;
import com.devsync.entity.UserStatus;
import com.devsync.repository.RoleRepository;
import com.devsync.repository.UserRepository;
import com.devsync.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        return processOAuth2User(userRequest, oauth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String username = generateUsername(email, name);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update user info if needed
            updateExistingUser(user, attributes, registrationId);
        } else {
            user = createNewUser(email, username, name, attributes, registrationId);
        }
        
        return new OAuth2User() {
            @Override
            public Map<String, Object> getAttributes() {
                return attributes;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return UserDetailsImpl.build(user).getAuthorities();
            }

            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("email");
            case "github":
                return (String) attributes.get("email");
            case "facebook":
                return (String) attributes.get("email");
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }

    private String extractName(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                return (String) attributes.get("name");
            case "facebook":
                return (String) attributes.get("name");
            default:
                return "Unknown";
        }
    }

    private String generateUsername(String email, String name) {
        String baseUsername = email.split("@")[0];
        
        // Check if username exists
        if (!userRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }
        
        // Generate unique username
        int counter = 1;
        String username = baseUsername + counter;
        while (userRepository.existsByUsername(username)) {
            counter++;
            username = baseUsername + counter;
        }
        
        return username;
    }

    private User createNewUser(String email, String username, String name, 
                              Map<String, Object> attributes, String provider) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(""); // OAuth users don't have passwords
        user.setStatus(UserStatus.ACTIVE);
        user.setIsOnline(true);
        
        // Extract first and last name
        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        user.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            user.setLastName(nameParts[1]);
        }
        
        // Extract profile picture
        String profilePicture = extractProfilePicture(provider, attributes);
        user.setProfilePicture(profilePicture);
        
        // Set default role
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    private void updateExistingUser(User user, Map<String, Object> attributes, String provider) {
        // Update profile picture if available
        String profilePicture = extractProfilePicture(provider, attributes);
        if (profilePicture != null && !profilePicture.isEmpty()) {
            user.setProfilePicture(profilePicture);
        }
        
        user.setIsOnline(true);
        userRepository.save(user);
    }

    @SuppressWarnings("unchecked")
    private String extractProfilePicture(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            case "facebook":
                Object pictureObj = attributes.get("picture");
                Map<String, Object> picture = (pictureObj instanceof Map) ? (Map<String, Object>) pictureObj : null;
                if (picture != null) {
                    Object dataObj = picture.get("data");
                    Map<String, Object> data = (dataObj instanceof Map) ? (Map<String, Object>) dataObj : null;
                    if (data != null) {
                        return (String) data.get("url");
                    }
                }
                return null;
            default:
                return null;
        }
    }
}