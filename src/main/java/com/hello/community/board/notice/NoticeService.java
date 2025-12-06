// NoticeService.java
package com.hello.community.board.notice;

import com.hello.community.board.common.BasePostService;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final BasePostService<Notice> noticePostService;
    private final NoticeRepository noticeRepository;

    @Transactional
    public Notice increaseViewCount(Long id) {
        return noticePostService.increaseViewCount(id);
    }

    public Notice findById(Long id) {
        return noticePostService.findById(id);
    }

    @Transactional
    public void saveNotice(String title, String content, Member writer) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setWriter(writer);
        noticePostService.save(notice);
    }

    @Transactional
    public void editNotice(Long id, String title, String content, Long loginUserId) {
        Notice notice = findById(id);

        if (!notice.getWriter().getId().equals(loginUserId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        notice.setTitle(title);
        notice.setContent(content);
    }

    @Transactional
    public void delete(Long id, Long loginUserId) {
        noticePostService.delete(id, loginUserId);
    }

    public Page<Notice> findPage(int num, int pageSize) {
        return noticePostService.findPage(
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    public Page<Notice> search(String keyword, int num, int pageSize) {
        return noticeRepository.searchByKeyword(
                keyword,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    // 로그인 사용자가 작성한 공지 목록
    public Page<Notice> findMyPage(Long writerId, int num, int pageSize) {
        return noticeRepository.findByWriterId(
                writerId,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }
}
