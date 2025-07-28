package com.devsync.repository;

import com.devsync.entity.Channel;
import com.devsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByIsPrivate(Boolean isPrivate);
    
    @Query("SELECT c FROM Channel c JOIN c.members m WHERE m.id = :userId")
    List<Channel> findChannelsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Channel c WHERE c.isPrivate = false OR (c.isPrivate = true AND :user MEMBER OF c.members)")
    List<Channel> findAccessibleChannels(@Param("user") User user);
    
    @Query("SELECT u FROM Channel c JOIN c.members u WHERE c.id = :channelId AND u.id NOT IN :onlineUserIds")
    List<User> findOfflineChannelMembers(@Param("channelId") Long channelId, @Param("onlineUserIds") java.util.Set<Long> onlineUserIds);
}