package com.reddot.api.service;

import com.reddot.api.dto.TopicRequest;
import com.reddot.api.dto.TopicResponse;
import com.reddot.api.entity.Message;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.ReportRepository;
import com.reddot.api.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock TopicRepository topicRepository;
    @Mock MessageRepository messageRepository;
    @Mock ReportRepository reportRepository;
    @Mock MessageService messageService;

    @InjectMocks TopicService topicService;

    private User regularUser;
    private User otherUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        regularUser = user(1L, "alice", User.Role.USER);
        otherUser   = user(2L, "bob",   User.Role.USER);
        adminUser   = user(3L, "admin", User.Role.ADMIN);
    }

    // --- getTopics ---

    @Test
    void getTopics_filtersHiddenForRegularUser() {
        Topic visible = topic(1L, regularUser, false, false);
        PageImpl<Topic> page = new PageImpl<>(List.of(visible));
        when(topicRepository.findByHiddenFalse(any())).thenReturn(page);

        Page<TopicResponse> result = topicService.getTopics(PageRequest.of(0, 10), regularUser);

        assertThat(result.getContent()).hasSize(1);
        verify(topicRepository).findByHiddenFalse(any());
        verify(topicRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void getTopics_showsAllToAdmin() {
        Topic visible = topic(1L, regularUser, false, false);
        Topic hidden  = topic(2L, regularUser, false, true);
        PageImpl<Topic> page = new PageImpl<>(List.of(visible, hidden));
        when(topicRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        Page<TopicResponse> result = topicService.getTopics(PageRequest.of(0, 10), adminUser);

        assertThat(result.getContent()).hasSize(2);
    }

    // --- getTopic ---

    @Test
    void getTopic_throwsWhenNotFound() {
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.getTopic(99L, regularUser))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getTopic_throwsWhenHiddenAndNotAdmin() {
        Topic hidden = topic(1L, regularUser, false, true);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(hidden));

        assertThatThrownBy(() -> topicService.getTopic(1L, regularUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getTopic_throwsWhenHiddenAndAnonymous() {
        Topic hidden = topic(1L, regularUser, false, true);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(hidden));

        assertThatThrownBy(() -> topicService.getTopic(1L, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getTopic_succeedsWhenHiddenAndAdmin() {
        Topic hidden = topic(1L, regularUser, false, true);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(hidden));
        when(topicRepository.save(any())).thenReturn(hidden);

        assertThatCode(() -> topicService.getTopic(1L, adminUser)).doesNotThrowAnyException();
    }

    @Test
    void getTopic_incrementsViewCount() {
        Topic t = topic(1L, regularUser, false, false);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(t));
        when(topicRepository.save(any())).thenReturn(t);

        topicService.getTopic(1L, regularUser);

        assertThat(t.getViews()).isEqualTo(1);
        verify(topicRepository).save(t);
    }

    // --- createTopic ---

    @Test
    void createTopic_savesAndReturnsResponse() {
        Topic saved = topic(5L, regularUser, false, false);
        when(topicRepository.save(any())).thenReturn(saved);

        TopicRequest req = new TopicRequest();
        req.setTitle("My Topic");
        req.setDescription("desc");

        TopicResponse result = topicService.createTopic(req, regularUser);

        assertThat(result.getId()).isEqualTo(5L);
        verify(topicRepository).save(any(Topic.class));
    }

    // --- deleteTopic ---

    @Test
    void deleteTopic_throwsWhenNotFound() {
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.deleteTopic(99L, regularUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteTopic_throwsForbiddenWhenNotAuthorNorAdmin() {
        Topic t = topic(1L, regularUser, false, false);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> topicService.deleteTopic(1L, otherUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Forbidden");
    }

    @Test
    void deleteTopic_succeedsWhenAuthor() {
        Topic t = topic(1L, regularUser, false, false);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(t));
        when(messageRepository.findByTopicIdAndParentIsNull(1L)).thenReturn(List.of());

        assertThatCode(() -> topicService.deleteTopic(1L, regularUser)).doesNotThrowAnyException();

        verify(reportRepository).deleteByTargetTypeAndTargetId(Report.TargetType.TOPIC, 1L);
        verify(topicRepository).delete(t);
    }

    @Test
    void deleteTopic_succeedsWhenAdmin() {
        Topic t = topic(1L, regularUser, false, false);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(t));
        when(messageRepository.findByTopicIdAndParentIsNull(1L)).thenReturn(List.of());

        assertThatCode(() -> topicService.deleteTopic(1L, adminUser)).doesNotThrowAnyException();

        verify(topicRepository).delete(t);
    }

    @Test
    void deleteTopic_deletesAllMessagesBeforeDeletingTopic() {
        Topic t = topic(1L, regularUser, false, false);
        Message m1 = message(1L, regularUser, t);
        Message m2 = message(2L, otherUser,   t);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(t));
        when(messageRepository.findByTopicIdAndParentIsNull(1L)).thenReturn(List.of(m1, m2));

        topicService.deleteTopic(1L, regularUser);

        verify(messageService).deleteMessageTree(m1);
        verify(messageService).deleteMessageTree(m2);
        verify(topicRepository).delete(t);
    }

    // --- Helpers ---

    private User user(Long id, String username, User.Role role) {
        return User.builder().id(id).username(username)
                .email(username + "@test.com").password("pass").role(role).build();
    }

    private Topic topic(Long id, User author, boolean locked, boolean hidden) {
        return Topic.builder().id(id).title("Test Topic")
                .author(author).locked(locked).hidden(hidden).build();
    }

    private Message message(Long id, User author, Topic topic) {
        return Message.builder().id(id).content("content")
                .author(author).topic(topic).locked(false).hidden(false)
                .replies(List.of()).votes(List.of()).build();
    }
}
