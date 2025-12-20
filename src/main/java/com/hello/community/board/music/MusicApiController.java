// MusicApiController.java
package com.hello.community.board.music;

import com.hello.community.board.common.ApiResponseDto;
import com.hello.community.board.recommend.PostRecommendService;
import com.hello.community.board.recommend.RecommendResponseDto;
import com.hello.community.member.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/music")
public class MusicApiController {

    private final MusicService musicService;
    private final PostRecommendService postRecommendService;

    @PostMapping("/{id}/recommend")
    public ResponseEntity<ApiResponseDto<RecommendResponseDto>> recommend(@PathVariable Long id,
                                                                          @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        Long memberId = user.getId();
        boolean nowRecommended = postRecommendService.toggleRecommend(id, memberId);

        if (nowRecommended) {
            musicService.increaseRecommendCount(id);
        } else {
            musicService.decreaseRecommendCount(id);
        }

        Music updated = musicService.findById(id);

        RecommendResponseDto data = new RecommendResponseDto(
                nowRecommended,
                updated.getRecommendCount()
        );

        return ResponseEntity.ok(ApiResponseDto.ok(data));
    }
}
