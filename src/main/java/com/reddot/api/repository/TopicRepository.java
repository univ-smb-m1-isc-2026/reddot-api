package com.reddot.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reddot.api.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Page<Topic> findByHiddenFalse(Pageable pageable);
    Page<Topic> findByHiddenFalseAndTitleContainingIgnoreCase(String title, Pageable pageable);
}