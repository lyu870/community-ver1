// NotificationApiController.java
package com.hello.community.notification;

import com.hello.community.board.common.ApiResponseDto;
import com.hello.community.member.CustomUser;
import com.hello.community.notification.NotificationService;
import com.hello.community.notification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationApiController {

    private final NotificationService notificationService;

    // 알림 목록 (최근 N개)
    @GetMapping("/notifications")
    public ApiResponseDto<NotificationListResponseDto> list(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = user.getId();
        return ApiResponseDto.ok(notificationService.getRecentNotifications(memberId, size));
    }

    // 미읽음 카운트
    @GetMapping("/notifications/unread-count")
    public ApiResponseDto<UnreadCountResponseDto> unreadCount(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long memberId = user.getId();
        return ApiResponseDto.ok(notificationService.getUnreadCount(memberId));
    }

    // 읽음 처리 (단건)
    @PostMapping("/notifications/{id}/read")
    public ApiResponseDto<Void> readOne(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long id
    ) {
        Long memberId = user.getId();
        notificationService.markRead(memberId, id);
        return ApiResponseDto.ok();
    }

    // 읽음 처리 (일괄)
    @PostMapping("/notifications/read")
    public ApiResponseDto<Void> readBulk(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody ReadNotificationsRequestDto req
    ) {
        Long memberId = user.getId();
        List<Long> ids = (req != null) ? req.getIds() : null;
        notificationService.markReadBulk(memberId, ids);
        return ApiResponseDto.ok();
    }

    // 알림 설정 조회 (마이페이지 토글)
    @GetMapping("/notification-settings")
    public ApiResponseDto<NotificationSettingResponseDto> getSettings(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long memberId = user.getId();
        return ApiResponseDto.ok(notificationService.getSettings(memberId));
    }

    // 알림 설정 변경 (마이페이지 토글)
    @PutMapping("/notification-settings")
    public ApiResponseDto<NotificationSettingResponseDto> updateSettings(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody NotificationSettingUpdateRequestDto req
    ) {
        Long memberId = user.getId();
        return ApiResponseDto.ok(notificationService.updateSettings(memberId, req));
    }

    // 게시판 구독 조회
    @GetMapping("/board-subscriptions")
    public ApiResponseDto<BoardSubscriptionResponseDto> getSubscriptions(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long memberId = user.getId();
        return ApiResponseDto.ok(notificationService.getSubscriptions(memberId));
    }

    // 게시판 구독 토글 (enabled 파라미터 없으면 토글)
    @PutMapping("/board-subscriptions/{boardType}")
    public ApiResponseDto<BoardSubscriptionItemDto> toggleSubscription(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable String boardType,
            @RequestParam(required = false) Boolean enabled
    ) {
        Long memberId = user.getId();

        BoardType type;
        try {
            type = BoardType.valueOf(boardType.toUpperCase());
        } catch (Exception e) {
            return ApiResponseDto.fail("invalid boardType");
        }

        return ApiResponseDto.ok(notificationService.updateSubscription(memberId, type, enabled));
    }
}
