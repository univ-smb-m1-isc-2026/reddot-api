package com.reddot.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.TopicRepository;
import com.reddot.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final MessageRepository messageRepository;

    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, Object>> topics = topicRepository.findByAuthor(user)
                .stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "title", t.getTitle(),
                        "views", t.getViews(),
                        "createdAt", t.getCreatedAt()
                ))
                .toList();

        List<Map<String, Object>> messages = messageRepository.findByAuthor(user)
                .stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "content", m.getContent(),
                        "createdAt", m.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt(),
                "topics", topics,
                "messages", messages
        ));
    }
}