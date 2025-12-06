// NewsService.java
package com.hello.community.board.news;

import com.hello.community.board.common.BasePostService;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final BasePostService<News> newsPostService;
    private final NewsRepository newsRepository;

    @Transactional
    public News increaseViewCount(Long id) {
        return newsPostService.increaseViewCount(id);
    }

    @Transactional
    public void increaseRecommendCount(Long id) {
        newsPostService.increaseRecommendCount(id);
    }

    @Transactional
    public void decreaseRecommendCount(Long id) {
        newsPostService.decreaseRecommendCount(id);
    }

    public News findById(Long id) {
        return newsPostService.findById(id);
    }

    @Transactional
    public void saveNews(String title, String content, Member writer) {
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setWriter(writer);
        newsPostService.save(news);
    }

    @Transactional
    public void editNews(Long id, String title, String content, Long loginUserId) {
        News news = findById(id);

        if (!news.getWriter().getId().equals(loginUserId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        news.setTitle(title);
        news.setContent(content);
    }

    @Transactional
    public void delete(Long id, Long loginUserId) {
        newsPostService.delete(id, loginUserId);
    }

    // 회원탈퇴용: 특정 회원이 작성한 모든 뉴스 게시글 삭제
    @Transactional
    public void deleteAllByWriter(Long writerId) {

        List<News> newsList = newsRepository.findAllByWriterId(writerId);

        for (News news : newsList) {
            newsPostService.delete(news.getId(), writerId, true);
        }
    }

    public Page<News> findPage(int num, int pageSize) {
        return newsPostService.findPage(
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    public Page<News> search(String keyword, int num, int pageSize) {
        return newsRepository.searchByKeyword(
                keyword,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    // 로그인 사용자가 작성한 게시글만 조회
    public Page<News> findMyPage(Long writerId, int num, int pageSize) {
        return newsRepository.findByWriterId(
                writerId,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }
}
