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

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    public List<MessageResponse> getMessages(Long topicId, User currentUser) {
        return messageRepository.findByTopicIdAndParentIsNull(topicId)
                .stream()
                .filter(m -> !m.isHidden() || isAdmin(currentUser))
                .map(m -> new MessageResponse(m, currentUser))
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

        return new MessageResponse(messageRepository.save(message), author);
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

        return new MessageResponse(messageRepository.save(reply), author);
    }

    public void moderateMessage(Long messageId, Boolean hidden, Boolean locked) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
    moderateRecursive(message, hidden, locked);
}

    private void moderateRecursive(Message message, Boolean hidden, Boolean locked) {
        if (hidden != null) message.setHidden(hidden);
        if (locked != null) message.setLocked(locked);
        messageRepository.save(message);

        if (message.getReplies() != null) {
            for (Message reply : message.getReplies()) {
                moderateRecursive(reply, hidden, locked);
            }
        }
    }
}