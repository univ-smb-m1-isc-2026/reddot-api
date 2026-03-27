package com.reddot.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Page<Topic> findByHiddenFalse(Pageable pageable);
    Page<Topic> findByHiddenFalseAndTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Topic> findByAuthor(User author);
    Page<Topic> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}