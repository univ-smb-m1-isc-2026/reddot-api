package com.reddot.api.service;

import org.springframework.stereotype.Service;

import com.reddot.api.dto.ReportRequest;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.ReportRepository;
import com.reddot.api.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final TopicRepository topicRepository;
    private final MessageRepository messageRepository;

    public void report(Long targetId, Report.TargetType targetType, ReportRequest request, User user) {
        if (targetType == Report.TargetType.TOPIC) {
            topicRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Topic not found"));
        } else {
            messageRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));
        }

        Report report = Report.builder()
                .reporter(user)
                .targetId(targetId)
                .targetType(targetType)
                .reason(request != null ? request.getReason() : null)
                .build();

        reportRepository.save(report);
    }

    public void resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setResolvedAt(java.time.LocalDateTime.now());
        reportRepository.save(report);
    }
}