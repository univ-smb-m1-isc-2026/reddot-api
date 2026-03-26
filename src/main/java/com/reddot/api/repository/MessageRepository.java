package com.reddot.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddot.api.entity.Message;
import com.reddot.api.entity.User;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTopicIdAndParentIsNull(Long topicId);
    List<Message> findByAuthor(User author);
}