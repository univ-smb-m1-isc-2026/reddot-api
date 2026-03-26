package com.reddot.api.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.dto.ReportResponse;
import com.reddot.api.entity.Message;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.ReportRepository;
import com.reddot.api.repository.TopicRepository;
import com.reddot.api.repository.UserRepository;
import com.reddot.api.service.MessageService;
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
    private final UserRepository userRepository;
    private final MessageService messageService;

    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(defaultValue = "false") boolean resolved
    ) {
        List<Report> reports = resolved
                ? reportRepository.findByResolvedAtIsNotNull()
                : reportRepository.findByResolvedAtIsNull();

        List<ReportResponse> response = reports.stream()
                .map(report -> {
                    if (report.getTargetType() == Report.TargetType.TOPIC) {
                        return topicRepository.findById(report.getTargetId())
                                .map(t -> (ReportResponse) new ReportResponse(report, t))
                                .orElse(null);
                    } else {
                        return messageRepository.findById(report.getTargetId())
                                .map(m -> (ReportResponse) new ReportResponse(report, m))
                                .orElse(null);
                    }
                })
                .filter(r -> r != null)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/topics/{id}")
    public ResponseEntity<Map<String, Object>> updateTopic(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        if (body.containsKey("locked")) topic.setLocked(body.get("locked"));
        if (body.containsKey("hidden")) topic.setHidden(body.get("hidden"));
        topicRepository.save(topic);
        return ResponseEntity.ok(Map.of(
                "id", topic.getId(),
                "locked", topic.isLocked(),
                "hidden", topic.isHidden()
        ));
    }

    @PatchMapping("/messages/{id}")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        messageService.moderateMessage(
                id,
                body.containsKey("hidden") ? body.get("hidden") : null,
                body.containsKey("locked") ? body.get("locked") : null
        );
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return ResponseEntity.ok(Map.of(
                "id", message.getId(),
                "locked", message.isLocked(),
                "hidden", message.isHidden()
        ));
    }

    @PatchMapping("/reports/{id}/resolve")
    public ResponseEntity<Void> resolveReport(@PathVariable Long id) {
        reportService.resolveReport(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(defaultValue = "") String q
    ) {
        if (q.isEmpty()) {
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.ok(userRepository.findByUsernameContainingIgnoreCase(q));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}