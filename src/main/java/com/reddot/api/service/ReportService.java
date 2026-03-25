package com.reddot.api.service;

import org.springframework.stereotype.Service;

import com.reddot.api.dto.ReportRequest;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.User;
import com.reddot.api.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public void report(Long targetId, Report.TargetType targetType, ReportRequest request, User user) {
        Report report = Report.builder()
                .reporter(user)
                .targetId(targetId)
                .targetType(targetType)
                .reason(request.getReason())
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