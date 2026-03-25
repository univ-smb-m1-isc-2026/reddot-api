package com.reddot.api.service;

import com.reddot.api.dto.TopicRequest;
import com.reddot.api.dto.TopicResponse;
import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;
import com.reddot.api.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public Page<TopicResponse> getTopics(Pageable pageable) {
        return topicRepository.findByHiddenFalse(pageable)
                .map(TopicResponse::new);
    }

    public TopicResponse getTopic(Long id, User currentUser) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        boolean isAdmin = currentUser != null &&
                currentUser.getRole() == User.Role.ADMIN;

        if (topic.isHidden() && !isAdmin) {
            throw new RuntimeException("Topic not found");
        }

        // incrémenter les vues
        topic.setViews(topic.getViews() + 1);
        topicRepository.save(topic);

        return new TopicResponse(topic);
    }

    public TopicResponse createTopic(TopicRequest request, User author) {
        Topic topic = Topic.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(author)
                .build();

        return new TopicResponse(topicRepository.save(topic));
    }

    public Page<TopicResponse> searchTopics(String query, Pageable pageable) {
        return topicRepository.findByHiddenFalseAndTitleContainingIgnoreCase(query, pageable)
                .map(TopicResponse::new);
    }
}