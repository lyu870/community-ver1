// NotificationService.java
package com.hello.community.notification;

import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import com.hello.community.notification.NotificationRepository;
import com.hello.community.notification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final BoardSubscriptionRepository boardSubscriptionRepository;
    private final MemberRepository memberRepository;
    private final NotificationCommandService notificationCommandService;

    @Transactional(readOnly = true)
    public NotificationListResponseDto getRecentNotifications(Long memberId, int size) {
        int pageSize = Math.max(1, Math.min(size, 50));

        PageRequest page = PageRequest.of(
                0,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<NotificationItemDto> items = notificationRepository.findByMemberId(memberId, page)
                .getContent()
                .stream()
                .map(n -> new NotificationItemDto(
                        n.getId(),
                        n.getType().name(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getLinkUrl(),
                        n.getCreatedAt(),
                        n.getReadAt(),
                        n.isRead()
                ))
                .collect(Collectors.toList());

        return new NotificationListResponseDto(items);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(Long memberId) {
        long count = notificationRepository.countByMemberIdAndReadAtIsNull(memberId);
        return new UnreadCountResponseDto(count);
    }

    @Transactional
    public void markRead(Long memberId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndMemberId(notificationId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("notification not found"));

        n.markRead();
    }

    @Transactional
    public int markReadBulk(Long memberId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        List<Long> unique = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (unique.isEmpty()) {
            return 0;
        }

        List<Notification> list = notificationRepository.findByMemberIdAndIdIn(memberId, unique);

        int updated = 0;
        for (Notification n : list) {
            if (!n.isRead()) {
                n.markRead();
                updated += 1;
            }
        }

        return updated;
    }

    @Transactional
    public NotificationSettingResponseDto getSettings(Long memberId) {
        NotificationSetting s = getOrCreateSetting(memberId);
        return new NotificationSettingResponseDto(s.isPostCommentEnabled(), s.isCommentReplyEnabled());
    }

    @Transactional
    public NotificationSettingResponseDto updateSettings(Long memberId, NotificationSettingUpdateRequestDto req) {
        NotificationSetting s = getOrCreateSetting(memberId);

        Boolean postCommentEnabled = (req != null) ? req.getPostCommentEnabled() : null;
        Boolean commentReplyEnabled = (req != null) ? req.getCommentReplyEnabled() : null;

        s.update(postCommentEnabled, commentReplyEnabled);

        return new NotificationSettingResponseDto(s.isPostCommentEnabled(), s.isCommentReplyEnabled());
    }

    @Transactional
    public BoardSubscriptionResponseDto getSubscriptions(Long memberId) {
        ensureDefaultSubscriptions(memberId);

        List<BoardSubscription> subs = boardSubscriptionRepository.findByMemberId(memberId);

        List<BoardSubscriptionItemDto> items = subs.stream()
                .sorted(Comparator.comparing(s -> s.getBoardType().name()))
                .map(s -> new BoardSubscriptionItemDto(s.getBoardType().toPath(), s.isEnabled()))
                .collect(Collectors.toList());

        return new BoardSubscriptionResponseDto(items);
    }

    @Transactional
    public BoardSubscriptionItemDto updateSubscription(Long memberId, BoardType boardType, Boolean enabled) {
        ensureDefaultSubscriptions(memberId);

        BoardSubscription sub = boardSubscriptionRepository.findByMemberIdAndBoardType(memberId, boardType)
                .orElseThrow(() -> new IllegalArgumentException("subscription not found"));

        if (enabled == null) {
            sub.setEnabled(!sub.isEnabled());
        } else {
            sub.setEnabled(enabled);
        }

        return new BoardSubscriptionItemDto(sub.getBoardType().toPath(), sub.isEnabled());
    }

    @Transactional
    public Notification createNotification(Long targetMemberId, NotificationType type, String message, String linkUrl, String eventId) {
        if (targetMemberId == null) {
            return null;
        }

        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        if (eventId != null && !eventId.trim().isEmpty()) {
            if (notificationRepository.existsByEventId(eventId)) {
                return null;
            }
        }

        if (!isAllowedBySetting(targetMemberId, type)) {
            return null;
        }

        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        String title = buildTitle(type);

        Notification n = Notification.of(member, type, title, message, linkUrl, eventId);

        try {
            return notificationCommandService.save(n);
        } catch (Exception e) {
            return null;
        }
    }

    public String buildPostDetailLink(BoardType boardType, Long postId) {
        if (boardType == null || postId == null) {
            return "";
        }

        return "/" + boardType.toPath() + "/detail/" + postId;
    }

    public String buildPostDetailLinkWithFocus(BoardType boardType, Long postId, Long focusCommentId) {
        String base = buildPostDetailLink(boardType, postId);
        if (base.isEmpty()) {
            return "";
        }

        if (focusCommentId == null) {
            return base;
        }

        return base + "?focusCommentId=" + encode(String.valueOf(focusCommentId));
    }

    public String buildPostDetailLinkWithOpenReply(BoardType boardType, Long postId, List<Long> openReplyPath, Long focusCommentId) {
        String base = buildPostDetailLink(boardType, postId);
        if (base.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(base);

        boolean first = true;

        if (openReplyPath != null && !openReplyPath.isEmpty()) {
            String joined = openReplyPath.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            if (!joined.isEmpty()) {
                sb.append(first ? "?" : "&");
                sb.append("openReplyPath=").append(encode(joined));
                first = false;
            }
        }

        if (focusCommentId != null) {
            sb.append(first ? "?" : "&");
            sb.append("focusCommentId=").append(encode(String.valueOf(focusCommentId)));
        }

        return sb.toString();
    }

    @Transactional
    public Notification createPostCommentNotification(Long targetMemberId,
                                                      BoardType boardType,
                                                      Long postId,
                                                      Long focusCommentId,
                                                      String message,
                                                      String eventId) {

        String linkUrl = buildPostDetailLinkWithFocus(boardType, postId, focusCommentId);
        return createNotification(targetMemberId, NotificationType.POST_COMMENT, message, linkUrl, eventId);
    }

    @Transactional
    public Notification createCommentReplyNotification(Long targetMemberId,
                                                       BoardType boardType,
                                                       Long postId,
                                                       List<Long> openReplyPath,
                                                       Long focusCommentId,
                                                       String message,
                                                       String eventId) {

        String linkUrl = buildPostDetailLinkWithOpenReply(boardType, postId, openReplyPath, focusCommentId);
        return createNotification(targetMemberId, NotificationType.COMMENT_REPLY, message, linkUrl, eventId);
    }

    private String buildTitle(NotificationType type) {
        if (type == null) {
            return "알림";
        }

        if (type == NotificationType.BOARD_POST) {
            return "새 글 알림";
        }

        if (type == NotificationType.POST_COMMENT) {
            return "댓글 알림";
        }

        if (type == NotificationType.COMMENT_REPLY) {
            return "답글 알림";
        }

        return "알림";
    }

    private String encode(String raw) {
        try {
            return URLEncoder.encode(String.valueOf(raw), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return String.valueOf(raw);
        }
    }

    private boolean isAllowedBySetting(Long memberId, NotificationType type) {
        if (type == NotificationType.BOARD_POST) {
            return true;
        }

        NotificationSetting s = getOrCreateSetting(memberId);

        if (type == NotificationType.POST_COMMENT) {
            return s.isPostCommentEnabled();
        }

        if (type == NotificationType.COMMENT_REPLY) {
            return s.isCommentReplyEnabled();
        }

        return true;
    }

    private NotificationSetting getOrCreateSetting(Long memberId) {
        Optional<NotificationSetting> opt = notificationSettingRepository.findByMemberId(memberId);
        if (opt.isPresent()) {
            return opt.get();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        NotificationSetting created = NotificationSetting.of(member);
        return notificationSettingRepository.save(created);
    }

    private void ensureDefaultSubscriptions(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        for (BoardType t : BoardType.values()) {
            Optional<BoardSubscription> exists = boardSubscriptionRepository.findByMemberIdAndBoardType(memberId, t);
            if (exists.isEmpty()) {
                BoardSubscription created = BoardSubscription.of(member, t, false);
                boardSubscriptionRepository.save(created);
            }
        }
    }
}
