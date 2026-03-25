package com.reddot.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.reddot.api.entity.Message;

import lombok.Getter;

@Getter
public class MessageResponse {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private boolean locked;
    private boolean hidden;
    private Long parentId;
    private List<MessageResponse> replies;

    public MessageResponse(Message message) {
        this.id = message.getId();
        this.content = message.getContent();
        this.author = message.getAuthor().getDeletedAt() != null
                ? "[deleted]"
                : message.getAuthor().getUsername();
        this.createdAt = message.getCreatedAt();
        this.locked = message.isLocked();
        this.hidden = message.isHidden();
        this.parentId = message.getParent() != null ? message.getParent().getId() : null;
        this.replies = message.getReplies() != null
                ? message.getReplies().stream()
                    .filter(r -> !r.isHidden())
                    .map(MessageResponse::new)
                    .toList()
                : List.of();
    }
}