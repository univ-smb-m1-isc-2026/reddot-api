package com.reddot.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reddot.api.dto.TopicRequest;
import com.reddot.api.dto.TopicResponse;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.ReportRepository;
import com.reddot.api.repository.TopicRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopicService {

    private static final Logger log = LoggerFactory.getLogger(TopicService.class);
    private final TopicRepository topicRepository;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;
    private final MessageService messageService;

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    public Page<TopicResponse> getTopics(Pageable pageable, User currentUser) {
        log.debug("Fetching topics page {}", pageable.getPageNumber());
        if (isAdmin(currentUser)) {
            return topicRepository.findAll(pageable).map(TopicResponse::new);
        }
        return topicRepository.findByHiddenFalse(pageable).map(TopicResponse::new);
    }

    public TopicResponse getTopic(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.isHidden() && !isAdmin(currentUser)) {
            log.warn("User {} tried to access hidden topic {}",
                    currentUser != null ? currentUser.getUsername() : "anonymous", id);
            throw new RuntimeException("Topic not found");
        }

        topic.setViews(topic.getViews() + 1);
        topicRepository.save(topic);
        log.debug("Topic {} viewed, total views: {}", id, topic.getViews());
        return new TopicResponse(topic);
    }

    public TopicResponse createTopic(TopicRequest request, User author) {
        Topic topic = Topic.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(author)
                .build();

        TopicResponse response = new TopicResponse(topicRepository.save(topic));
        log.info("Topic created by {}: {}", author.getUsername(), request.getTitle());
        return response;
    }

    public Page<TopicResponse> searchTopics(String query, Pageable pageable, User currentUser) {
        log.debug("Searching topics with query: {}", query);
        if (isAdmin(currentUser)) {
            return topicRepository.findByTitleContainingIgnoreCase(query, pageable)
                    .map(TopicResponse::new);
        }
        return topicRepository.findByHiddenFalseAndTitleContainingIgnoreCase(query, pageable)
                .map(TopicResponse::new);
    }

    @Transactional
    public void deleteTopic(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (!isAdmin(currentUser) && !topic.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Forbidden");
        }

        log.warn("Topic {} deleted by {}", id, currentUser.getUsername());
        messageRepository.findByTopicIdAndParentIsNull(id)
                .forEach(messageService::deleteMessageTree);
        reportRepository.deleteByTargetTypeAndTargetId(Report.TargetType.TOPIC, id);
        topicRepository.delete(topic);
    }

    public TopicResponse moderateTopic(Long id, Boolean hidden, Boolean locked) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        if (hidden != null) {
            topic.setHidden(hidden);
            log.warn("Topic {} hidden: {}", id, hidden);
        }
        if (locked != null) {
            topic.setLocked(locked);
            log.warn("Topic {} locked: {}", id, locked);
        }
        return new TopicResponse(topicRepository.save(topic));
    }
}