package com.reddot.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reddot.api.dto.ReportRequest;
import com.reddot.api.dto.TopicRequest;
import com.reddot.api.dto.TopicResponse;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.User;
import com.reddot.api.service.ReportService;
import com.reddot.api.service.TopicService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<Page<TopicResponse>> getTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sort,
            @AuthenticationPrincipal User currentUser
    ) {
        Sort sorting = sort.equals("popular")
                ? Sort.by(Sort.Direction.DESC, "views")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sorting);
        return ResponseEntity.ok(topicService.getTopics(pageable, currentUser));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TopicResponse>> searchTopics(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(topicService.searchTopics(q, pageable, currentUser));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        topicService.deleteTopic(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<Void> reportTopic(
            @PathVariable Long id,
            @RequestBody(required = false) ReportRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        reportService.report(id, Report.TargetType.TOPIC, request, currentUser);
        return ResponseEntity.ok().build();
    }
}