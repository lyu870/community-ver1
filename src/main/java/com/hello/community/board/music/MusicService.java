// MusicService.java
package com.hello.community.board.music;

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
public class MusicService {

    private final BasePostService<Music> musicPostService;
    private final MusicRepository musicRepository;

    @Transactional
    public Music increaseViewCount(Long id) {
        return musicPostService.increaseViewCount(id);
    }

    @Transactional
    public void increaseRecommendCount(Long id) {
        musicPostService.increaseRecommendCount(id);
    }

    @Transactional
    public void decreaseRecommendCount(Long id) {
        musicPostService.decreaseRecommendCount(id);
    }

    public Music findById(Long id) {
        return musicPostService.findById(id);
    }

    @Transactional
    public void saveMusic(String title, String content, Member writer) {
        Music music = new Music();
        music.setTitle(title);
        music.setContent(content);
        music.setWriter(writer);
        musicPostService.save(music);
    }

    @Transactional
    public void editMusic(Long id, String title, String content, Long loginUserId) {
        Music music = findById(id);

        if (!music.getWriter().getId().equals(loginUserId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        music.setTitle(title);
        music.setContent(content);
    }

    @Transactional
    public void delete(Long id, Long loginUserId) {
        delete(id, loginUserId, false);
    }

    @Transactional
    public void delete(Long id, Long loginUserId, boolean isAdmin) {
        musicPostService.delete(id, loginUserId, isAdmin);
    }

    // 회원탈퇴용: 특정 회원이 작성한 모든 음악 게시글 삭제
    @Transactional
    public void deleteAllByWriter(Long writerId) {

        List<Music> musics = musicRepository.findAllByWriterId(writerId);

        for (Music music : musics) {
            musicPostService.delete(music.getId(), writerId, true);
        }
    }

    public Page<Music> findPage(int num, int pageSize) {
        return musicPostService.findPage(
                PageRequest.of(num - 1, pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public Page<Music> search(String keyword, int num, int pageSize) {
        return musicRepository.searchByKeyword(
                keyword,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    public Page<Music> findMyPage(Long writerId, int num, int pageSize) {
        return musicRepository.findByWriterId(
                writerId,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }
}
