package com.reddot.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.entity.Message;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.Topic;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.ReportRepository;
import com.reddot.api.repository.TopicRepository;
import com.reddot.api.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TopicRepository topicRepository;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;
    private final ReportService reportService;

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports() {
        return ResponseEntity.ok(reportRepository.findByResolvedAtIsNull());
    }

    @PatchMapping("/topics/{id}")
    public ResponseEntity<Void> updateTopic(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        if (body.containsKey("locked")) topic.setLocked(body.get("locked"));
        if (body.containsKey("hidden")) topic.setHidden(body.get("hidden"));
        topicRepository.save(topic);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/messages/{id}")
    public ResponseEntity<Void> updateMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (body.containsKey("locked")) message.setLocked(body.get("locked"));
        if (body.containsKey("hidden")) message.setHidden(body.get("hidden"));
        messageRepository.save(message);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reports/{id}/resolve")
    public ResponseEntity<Void> resolveReport(@PathVariable Long id) {
        reportService.resolveReport(id);
        return ResponseEntity.ok().build();
    }
}