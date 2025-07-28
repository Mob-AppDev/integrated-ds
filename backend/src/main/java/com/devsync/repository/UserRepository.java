package com.devsync.repository;

import com.devsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isOnline = true")
    List<User> findOnlineUsers();
    
    @Modifying
    @Query("UPDATE User u SET u.isOnline = :isOnline, u.lastSeen = :lastSeen WHERE u.id = :userId")
    void updateUserOnlineStatus(@Param("userId") Long userId, 
                               @Param("isOnline") Boolean isOnline, 
                               @Param("lastSeen") LocalDateTime lastSeen);
    
    @Modifying
    @Query("UPDATE User u SET u.firebaseToken = :token WHERE u.id = :userId")
    void updateFirebaseToken(@Param("userId") Long userId, @Param("token") String token);
}