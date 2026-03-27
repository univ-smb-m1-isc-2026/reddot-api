package com.reddot.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddot.api.dto.VoteRequest;
import com.reddot.api.entity.User;
import com.reddot.api.service.VoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/{messageId}/vote")
    public ResponseEntity<Void> vote(
            @PathVariable Long messageId,
            @RequestBody VoteRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        voteService.vote(messageId, request, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{messageId}/vote")
    public ResponseEntity<Void> removeVote(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser
    ) {
        voteService.removeVote(messageId, currentUser);
        return ResponseEntity.noContent().build();
    }
}