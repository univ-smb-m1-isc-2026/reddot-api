package com.reddot.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.reddot.api.entity.Message;
import com.reddot.api.entity.User;
import com.reddot.api.entity.Vote;

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
    private int score;
    private Integer userVote;

    public MessageResponse(Message message, User currentUser) {
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
                .filter(r -> !r.isHidden() || (currentUser != null && currentUser.getRole() == User.Role.ADMIN))
                .map(r -> new MessageResponse(r, currentUser))
                .toList()
            : List.of();
        this.score = message.getVotes() != null
                ? message.getVotes().stream()
                    .mapToInt(Vote::getValue)
                    .sum()
                : 0;
        this.userVote = message.getVotes() != null && currentUser != null
                ? message.getVotes().stream()
                    .filter(v -> v.getUser().getId().equals(currentUser.getId()))
                    .map(Vote::getValue)
                    .findFirst()
                    .orElse(null)
                : null;
    }
}