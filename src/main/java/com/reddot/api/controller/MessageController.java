package com.reddot.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.dto.MessageRequest;
import com.reddot.api.dto.MessageResponse;
import com.reddot.api.entity.User;
import com.reddot.api.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/api/topics/{topicId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long topicId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(messageService.getMessages(topicId, currentUser));
    }

    @PostMapping("/api/topics/{topicId}/messages")
    public ResponseEntity<MessageResponse> createMessage(
            @PathVariable Long topicId,
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(messageService.createMessage(topicId, request, currentUser));
    }

    @PostMapping("/api/messages/{messageId}/replies")
    public ResponseEntity<MessageResponse> replyToMessage(
            @PathVariable Long messageId,
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(messageService.replyToMessage(messageId, request, currentUser));
    }
}