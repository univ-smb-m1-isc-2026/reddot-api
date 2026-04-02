package com.reddot.api.service;

import com.reddot.api.dto.MessageRequest;
import com.reddot.api.dto.MessageResponse;
import com.reddot.api.entity.Message;
import com.reddot.api.entity.Report;
import com.reddot.api.entity.Topic;
import com.reddot.api.entity.User;
import com.reddot.api.repository.MessageRepository;
import com.reddot.api.repository.ReportRepository;
import com.reddot.api.repository.TopicRepository;
import com.reddot.api.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock MessageRepository messageRepository;
    @Mock TopicRepository topicRepository;
    @Mock VoteRepository voteRepository;
    @Mock ReportRepository reportRepository;

    @InjectMocks MessageService messageService;

    private User regularUser;
    private User otherUser;
    private User adminUser;
    private Topic openTopic;
    private Topic lockedTopic;

    @BeforeEach
    void setUp() {
        regularUser = user(1L, "alice", User.Role.USER);
        otherUser   = user(2L, "bob",   User.Role.USER);
        adminUser   = user(3L, "admin", User.Role.ADMIN);
        openTopic   = topic(10L, regularUser, false, false);
        lockedTopic = topic(11L, regularUser, true,  false);
    }

    // --- getMessages ---

    @Test
    void getMessages_hidesHiddenMessagesFromRegularUser() {
        Message visible = message(1L, regularUser, openTopic, false, false);
        Message hidden  = message(2L, regularUser, openTopic, false, true);
        when(messageRepository.findByTopicIdAndParentIsNull(10L)).thenReturn(List.of(visible, hidden));

        List<MessageResponse> result = messageService.getMessages(10L, regularUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getMessages_showsHiddenMessagesToAdmin() {
        Message visible = message(1L, regularUser, openTopic, false, false);
        Message hidden  = message(2L, regularUser, openTopic, false, true);
        when(messageRepository.findByTopicIdAndParentIsNull(10L)).thenReturn(List.of(visible, hidden));

        List<MessageResponse> result = messageService.getMessages(10L, adminUser);

        assertThat(result).hasSize(2);
    }

    @Test
    void getMessages_anonymousUserSeesOnlyVisible() {
        Message visible = message(1L, regularUser, openTopic, false, false);
        Message hidden  = message(2L, regularUser, openTopic, false, true);
        when(messageRepository.findByTopicIdAndParentIsNull(10L)).thenReturn(List.of(visible, hidden));

        List<MessageResponse> result = messageService.getMessages(10L, null);

        assertThat(result).hasSize(1);
    }

    // --- createMessage ---

    @Test
    void createMessage_throwsWhenTopicIsLocked() {
        when(topicRepository.findById(11L)).thenReturn(Optional.of(lockedTopic));

        assertThatThrownBy(() -> messageService.createMessage(11L, request("hello"), regularUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void createMessage_throwsWhenTopicNotFound() {
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.createMessage(99L, request("hello"), regularUser))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createMessage_savesAndReturnsResponse() {
        Message saved = message(5L, regularUser, openTopic, false, false);
        when(topicRepository.findById(10L)).thenReturn(Optional.of(openTopic));
        when(messageRepository.save(any())).thenReturn(saved);

        MessageResponse result = messageService.createMessage(10L, request("hello"), regularUser);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getContent()).isEqualTo("Test content");
        verify(messageRepository).save(any(Message.class));
    }

    // --- replyToMessage ---

    @Test
    void replyToMessage_throwsWhenParentIsLocked() {
        Message locked = message(1L, regularUser, openTopic, true, false);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(locked));

        assertThatThrownBy(() -> messageService.replyToMessage(1L, request("reply"), regularUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void replyToMessage_throwsWhenParentNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.replyToMessage(99L, request("reply"), regularUser))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void replyToMessage_savesAndReturnsResponse() {
        Message parent = message(1L, regularUser, openTopic, false, false);
        Message saved  = message(6L, otherUser,   openTopic, false, false);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(messageRepository.save(any())).thenReturn(saved);

        MessageResponse result = messageService.replyToMessage(1L, request("reply"), otherUser);

        assertThat(result.getId()).isEqualTo(6L);
        verify(messageRepository).save(any(Message.class));
    }

    // --- deleteMessage ---

    @Test
    void deleteMessage_throwsWhenNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.deleteMessage(99L, regularUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteMessage_throwsForbiddenWhenNotAuthorNorAdmin() {
        Message msg = message(1L, regularUser, openTopic, false, false);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThatThrownBy(() -> messageService.deleteMessage(1L, otherUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Forbidden");
    }

    @Test
    void deleteMessage_succeedsWhenAuthor() {
        Message msg = message(1L, regularUser, openTopic, false, false);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThatCode(() -> messageService.deleteMessage(1L, regularUser)).doesNotThrowAnyException();

        verify(voteRepository).deleteByMessage(msg);
        verify(reportRepository).deleteByTargetTypeAndTargetId(Report.TargetType.MESSAGE, 1L);
        verify(messageRepository).delete(msg);
    }

    @Test
    void deleteMessage_succeedsWhenAdmin() {
        Message msg = message(1L, regularUser, openTopic, false, false);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThatCode(() -> messageService.deleteMessage(1L, adminUser)).doesNotThrowAnyException();

        verify(messageRepository).delete(msg);
    }

    @Test
    void deleteMessage_deletesRepliesRecursively() {
        Message reply  = message(2L, otherUser, openTopic, false, false);
        Message parent = Message.builder()
                .id(1L).content("parent").author(regularUser).topic(openTopic)
                .locked(false).hidden(false)
                .replies(List.of(reply)).votes(List.of())
                .build();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(parent));

        messageService.deleteMessage(1L, regularUser);

        verify(messageRepository).delete(reply);
        verify(messageRepository).delete(parent);
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

    private Message message(Long id, User author, Topic topic, boolean locked, boolean hidden) {
        return Message.builder().id(id).content("Test content")
                .author(author).topic(topic).locked(locked).hidden(hidden)
                .replies(List.of()).votes(List.of()).build();
    }

    private MessageRequest request(String content) {
        MessageRequest r = new MessageRequest();
        r.setContent(content);
        return r;
    }
}
