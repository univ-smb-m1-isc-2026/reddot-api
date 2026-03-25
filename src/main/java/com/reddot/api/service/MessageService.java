package com.reddot.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reddot.api.dto.MessageRequest;
import com.reddot.api.dto.MessageResponse;
import com.reddot.api.entity.Message;
import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final TopicRepository topicRepository;

    public List<MessageResponse> getMessages(Long topicId) {
        return messageRepository.findByTopicIdAndParentIsNull(topicId)
                .stream()
                .filter(m -> !m.isHidden())
                .map(MessageResponse::new)
                .toList();
    }

    public MessageResponse createMessage(Long topicId, MessageRequest request, User author) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.isLocked()) {
            throw new RuntimeException("Topic is locked");
        }

        Message message = Message.builder()
                .content(request.getContent())
                .author(author)
                .topic(topic)
                .build();

        return new MessageResponse(messageRepository.save(message));
    }

    public MessageResponse replyToMessage(Long messageId, MessageRequest request, User author) {
        Message parent = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (parent.isLocked()) {
            throw new RuntimeException("Message is locked");
        }

        Message reply = Message.builder()
                .content(request.getContent())
                .author(author)
                .topic(parent.getTopic())
                .parent(parent)
                .build();

        return new MessageResponse(messageRepository.save(reply));
    }
}