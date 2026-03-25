package com.reddot.api.dto;

import java.time.LocalDateTime;

import com.reddot.api.entity.Topic;

import lombok.Getter;

@Getter
public class TopicResponse {
    private Long id;
    private String title;
    private String description;
    private String author;
    private LocalDateTime createdAt;
    private boolean locked;
    private boolean hidden;
    private int views;

    public TopicResponse(Topic topic) {
        this.id = topic.getId();
        this.title = topic.getTitle();
        this.description = topic.getDescription();
        this.author = topic.getAuthor().getDeletedAt() != null ? "[deleted]" : topic.getAuthor().getUsername();
        this.createdAt = topic.getCreatedAt();
        this.locked = topic.isLocked();
        this.hidden = topic.isHidden();
        this.views = topic.getViews();
    }
}