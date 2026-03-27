package com.reddot.api.dto;

import com.reddot.api.entity.Report;
import com.reddot.api.entity.Message;
import com.reddot.api.entity.Topic;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportResponse {
    private Long id;
    private String reporter;
    private Report.TargetType targetType;
    private Long targetId;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // contenu du target
    private String targetContent;
    private String targetAuthor;
    private Boolean targetHidden;
    private Boolean targetLocked;
    private Integer targetScore;
    private String targetTopicTitle;
    private Long targetTopicId;

    public ReportResponse(Report report, Topic topic) {
        base(report);
        this.targetContent = topic.getTitle() + (topic.getDescription() != null ? " — " + topic.getDescription() : "");
        this.targetAuthor = topic.getAuthor().getUsername();
        this.targetHidden = topic.isHidden();
        this.targetLocked = topic.isLocked();
        this.targetScore = null;
        this.targetTopicTitle = topic.getTitle();
        this.targetTopicId = topic.getId();
    }

    public ReportResponse(Report report, Message message) {
        base(report);
        this.targetContent = message.getContent();
        this.targetAuthor = message.getAuthor().getUsername();
        this.targetHidden = message.isHidden();
        this.targetLocked = message.isLocked();
        this.targetScore = message.getVotes() != null
                ? message.getVotes().stream().mapToInt(v -> v.getValue()).sum()
                : 0;
        this.targetTopicTitle = message.getTopic().getTitle();
        this.targetTopicId = message.getTopic().getId();
    }

    private void base(Report report) {
        this.id = report.getId();
        this.reporter = report.getReporter().getUsername();
        this.targetType = report.getTargetType();
        this.targetId = report.getTargetId();
        this.reason = report.getReason();
        this.createdAt = report.getCreatedAt();
        this.resolvedAt = report.getResolvedAt();
    }
}