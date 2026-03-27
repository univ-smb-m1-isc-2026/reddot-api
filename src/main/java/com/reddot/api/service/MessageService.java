package com.reddot.api.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final TopicRepository topicRepository;

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    public List<MessageResponse> getMessages(Long topicId, User currentUser) {
        log.debug("Fetching messages for topic {} (user: {})", topicId,
                currentUser != null ? currentUser.getUsername() : "anonymous");
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
            log.warn("User {} tried to post on locked topic {}", author.getUsername(), topicId);
            throw new RuntimeException("Topic is locked");
        }

        Message message = Message.builder()
                .content(request.getContent())
                .author(author)
                .topic(topic)
                .build();

        MessageResponse response = new MessageResponse(messageRepository.save(message), author);
        log.info("Message created by {} on topic {}", author.getUsername(), topicId);
        return response;
    }

    public MessageResponse replyToMessage(Long messageId, MessageRequest request, User author) {
        Message parent = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (parent.isLocked()) {
            log.warn("User {} tried to reply to locked message {}", author.getUsername(), messageId);
            throw new RuntimeException("Message is locked");
        }

        Message reply = Message.builder()
                .content(request.getContent())
                .author(author)
                .topic(parent.getTopic())
                .parent(parent)
                .build();

        MessageResponse response = new MessageResponse(messageRepository.save(reply), author);
        log.info("Reply created by {} on message {}", author.getUsername(), messageId);
        return response;
    }

    public void moderateMessage(Long messageId, Boolean hidden, Boolean locked) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        log.warn("Moderating message {} - hidden: {}, locked: {}", messageId, hidden, locked);
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