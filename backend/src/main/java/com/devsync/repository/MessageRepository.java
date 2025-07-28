package com.devsync.repository;

import com.devsync.entity.Message;
import com.devsync.entity.Channel;
import com.devsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChannelOrderByCreatedAtDesc(Channel channel, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.createdAt DESC")
    Page<Message> findDirectMessages(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.parentMessage.id = :parentId ORDER BY m.createdAt ASC")
    List<Message> findThreadReplies(@Param("parentId") Long parentId);
    
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:keyword% AND m.channel = :channel ORDER BY m.createdAt DESC")
    List<Message> searchInChannel(@Param("keyword") String keyword, @Param("channel") Channel channel);
}