//PostFinder.java
package com.hello.community.board.common;

import com.hello.community.board.music.MusicRepository;
import com.hello.community.board.news.NewsRepository;
import com.hello.community.board.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFinder {

    private final MusicRepository musicRepository;
    private final NewsRepository newsRepository;
    private final NoticeRepository noticeRepository;

    /*
     * 댓글에서 어떤 게시판의 글인지 모르기 때문에
     * 모든 게시판 Repository를 순차적으로 조회해서 BasePost찾음.
     */
    public BasePost findPost(Long id) {

        return musicRepository.findById(id)
                .map(post -> (BasePost) post)

                .or(() -> newsRepository.findById(id)
                        .map(post -> (BasePost) post))

                .or(() -> noticeRepository.findById(id)
                        .map(post -> (BasePost) post))

                .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));
    }

    // 댓글 redirect URL 자동 생성
    public String buildDetailUrl(Long id) {

        if (musicRepository.findById(id).isPresent()) {
            return "/music/detail/" + id;
        }
        if (newsRepository.findById(id).isPresent()) {
            return "/news/detail/" + id;
        }
        if (noticeRepository.findById(id).isPresent()) {
            return "/notice/detail/" + id;
        }

        throw new IllegalArgumentException("해당 게시물을 찾을 수 없습니다.");
    }
}
