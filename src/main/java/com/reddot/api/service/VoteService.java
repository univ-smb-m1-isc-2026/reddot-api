package com.reddot.api.service;

import org.springframework.stereotype.Service;

import com.reddot.api.dto.VoteRequest;
import com.reddot.api.entity.Message;
import com.reddot.api.entity.User;
import com.reddot.api.entity.Vote;
import com.reddot.api.entity.VoteId;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final MessageRepository messageRepository;

    public void vote(Long messageId, VoteRequest request, User user) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.isLocked()) {
            throw new RuntimeException("Message is locked");
        }

        VoteId voteId = new VoteId(user.getId(), messageId);

        Vote vote = voteRepository.findById(voteId)
                .orElse(Vote.builder()
                        .id(voteId)
                        .user(user)
                        .message(message)
                        .build());

        vote.setValue(request.getValue());
        voteRepository.save(vote);
    }

    public void removeVote(Long messageId, User user) {
        VoteId voteId = new VoteId(user.getId(), messageId);
        voteRepository.deleteById(voteId);
    }
}