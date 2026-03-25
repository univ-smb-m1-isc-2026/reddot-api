package com.reddot.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.dto.TopicRequest;
import com.reddot.api.dto.TopicResponse;
import com.reddot.api.entity.User;
import com.reddot.api.service.TopicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<Page<TopicResponse>> getTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        Sort sorting = sort.equals("popular")
                ? Sort.by(Sort.Direction.DESC, "views")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sorting);
        return ResponseEntity.ok(topicService.getTopics(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TopicResponse>> searchTopics(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(topicService.searchTopics(q, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> getTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(topicService.getTopic(id, currentUser));
    }

    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(
            @RequestBody TopicRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(topicService.createTopic(request, currentUser));
    }
}