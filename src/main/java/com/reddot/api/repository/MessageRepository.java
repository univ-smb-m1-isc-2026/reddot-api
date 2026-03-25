package com.reddot.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddot.api.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTopicIdAndParentIsNull(Long topicId);
}