package com.reddot.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.dto.ReportRequest;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.User;
import com.reddot.api.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{messageId}/report")
    public ResponseEntity<Void> reportMessage(
            @PathVariable Long messageId,
            @RequestBody(required = false) ReportRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        reportService.report(messageId, Report.TargetType.MESSAGE, request, currentUser);
        return ResponseEntity.ok().build();
    }
}