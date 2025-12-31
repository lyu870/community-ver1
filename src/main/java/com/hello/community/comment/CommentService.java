// CommentService.java
package com.hello.community.comment;

import com.hello.community.board.common.BasePost;
import com.hello.community.board.common.PostFinder;
import com.hello.community.comment.CommentRepository;
import com.hello.community.member.Member;
import com.hello.community.notification.BoardType;
import com.hello.community.notification.NotificationType;
import com.hello.community.notification.event.NotificationEvent;
import com.hello.community.notification.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ClassUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostFinder postFinder;
    private final NotificationEventPublisher notificationEventPublisher;

    // 게시글 상세: 루트 댓글만 가져오고, replyCount(전체 답글 수)만 세팅
    public List<Comment> getComment(Long postId) {
        BasePost post = postFinder.findPost(postId);

        List<Comment> roots = commentRepository.findRootByPostWithWriter(post);
        Map<Long, Long> replyCountMap = buildReplyCountMap(post);

        for (Comment c : roots) {
            long cnt = replyCountMap.getOrDefault(c.getId(), 0L);
            c.setReplyCount(cnt);
        }

        return roots;
    }

    // lazy 로딩: 특정 부모의 직계 답글만 반환 (각 자식댓글도 replyCount 세팅)
    public List<Comment> getChildren(Long postId, Long parentId) {
        BasePost post = postFinder.findPost(postId);

        List<Comment> children = commentRepository.findChildrenByPostAndParentIdWithWriter(post, parentId);
        Map<Long, Long> replyCountMap = buildReplyCountMap(post);

        for (Comment c : children) {
            long cnt = replyCountMap.getOrDefault(c.getId(), 0L);
            c.setReplyCount(cnt);
        }

        return children;
    }

    // lazy 로딩: 특정 부모의 직계 답글 페이징 반환 (각 자식댓글도 replyCount 세팅)
    public Page<Comment> getChildrenPage(Long postId, Long parentId, int page, int size) {
        BasePost post = postFinder.findPost(postId);

        Page<Comment> childrenPage = commentRepository.findChildrenPageByPostAndParentId(
                post,
                parentId,
                PageRequest.of(page, size)
        );

        Map<Long, Long> replyCountMap = buildReplyCountMap(post);

        for (Comment c : childrenPage.getContent()) {
            long cnt = replyCountMap.getOrDefault(c.getId(), 0L);
            c.setReplyCount(cnt);
        }

        return childrenPage;
    }

    // 답글/댓글 수정 후 "어느 답글보기(parent)를 열어야 하는지" 판단용
    public Long findParentId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다.")); // 댓글/게시글 저장 시 사용

        Comment parent = comment.getParent();
        if (parent == null) {
            return null;
        }

        return parent.getId();
    }

    // openReplyPath용: parentId의 조상 경로(root -> ... -> parentId) 생성
    public List<Long> buildAncestorPathInclusive(Long parentId) {
        Comment current = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다.")); // 댓글/게시글 저장 시 사용

        List<Long> path = new ArrayList<>();
        while (current != null) {
            path.add(current.getId());
            current = current.getParent();
        }

        Collections.reverse(path);
        return path;
    }

    @Transactional
    public Long addComment(Long postId, Member writer, String content, Long parentId) {

        BasePost post = postFinder.findPost(postId);

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다.")); // 댓글/게시글 저장 시 사용
        }

        Comment c = Comment.create(post, writer, content, parent);
        commentRepository.save(c);

        createNotificationAfterSave(post, writer, c, parent);

        return c.getId();
    }

    @Transactional
    public Comment addCommentAndReturn(Long postId, Member writer, String content, Long parentId) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        BasePost post = postFinder.findPost(postId);

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다.")); // 댓글/게시글 저장 시 사용
        }

        Comment c = Comment.create(post, writer, content, parent);
        commentRepository.save(c);

        c.setReplyCount(0L);

        createNotificationAfterSave(post, writer, c, parent);

        return c;
    }

    @Transactional
    public void deleteComment(Long commentId, Member loginUser) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다.")); // 댓글/게시글 저장 시 사용

        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제 가능합니다.");
        }

        // 자식 댓글이 있어도 해당 댓글 트리 전체 삭제
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Member loginUser, boolean isAdmin) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다.")); // 댓글/게시글 저장 시 사용

        if (!isAdmin && !comment.getWriter().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제 가능합니다.");
        }

        // 자식 댓글이 있어도 해당 댓글 트리 전체 삭제
        commentRepository.delete(comment);
    }

    // 회원탈퇴 시: 해당 회원이 작성한 댓글/대댓글 트리 전체 삭제
    @Transactional
    public void deleteCommentsForWithdraw(Long memberId) {

        List<Comment> comments = commentRepository.findByWriterId(memberId);

        for (Comment comment : comments) {
            Comment parent = comment.getParent();

            // 부모가 없거나, 부모 작성자가 탈퇴 회원이 아닌 경우에만 루트로 보고 삭제
            Long parentWriterId = null;
            if (parent != null && parent.getWriter() != null) {
                parentWriterId = parent.getWriter().getId();
            }

            if (parent == null || !memberId.equals(parentWriterId)) {
                commentRepository.delete(comment);
            }
        }
    }

    @Transactional
    public void editComment(Long commentId, Member loginUser, String newContent) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다.")); // 댓글/게시글 저장 시 사용

        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정 가능합니다.");
        }

        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public Comment editCommentAndReturn(Long commentId, Member loginUser, String newContent) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다.")); // 댓글/게시글 저장 시 사용

        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정 가능합니다.");
        }

        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());

        return comment;
    }

    private void publishAfterCommit(NotificationEvent event) {
        if (event == null) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        notificationEventPublisher.publish(event);
                    } catch (Exception e) {
                        return;
                    }
                }
            });
            return;
        }

        try {
            notificationEventPublisher.publish(event);
        } catch (Exception e) {
            return;
        }
    }

    private void createNotificationAfterSave(BasePost post, Member writer, Comment saved, Comment parent) {
        if (post == null || writer == null || saved == null) {
            return;
        }

        BoardType boardType = resolveBoardType(post);
        if (boardType == null) {
            return;
        }

        Long postWriterId = null;
        if (post.getWriter() != null) {
            postWriterId = post.getWriter().getId();
        }

        Long writerId = writer.getId();

        try {
            // 루트 댓글: 내 게시글에 달린 댓글 알림
            if (parent == null) {
                if (postWriterId == null) {
                    return;
                }

                if (writerId != null && String.valueOf(writerId).equals(String.valueOf(postWriterId))) {
                    return;
                }

                String msg = buildPostCommentMessage(writer, saved.getContent());
                String eventId = "post_comment:" + saved.getId();

                NotificationEvent event = new NotificationEvent(
                        postWriterId,
                        NotificationType.POST_COMMENT,
                        boardType,
                        post.getId(),
                        saved.getId(),
                        null,
                        msg,
                        eventId
                );

                publishAfterCommit(event);
                return;
            }

            // 답글: 내 댓글에 달린 답글 알림
            Long parentWriterId = null;
            if (parent.getWriter() != null) {
                parentWriterId = parent.getWriter().getId();
            }

            if (parentWriterId == null) {
                return;
            }

            if (writerId != null && String.valueOf(writerId).equals(String.valueOf(parentWriterId))) {
                return;
            }

            List<Long> path = buildAncestorPathInclusive(parent.getId());

            String msg = buildCommentReplyMessage(writer, saved.getContent());
            String eventId = "comment_reply:" + saved.getId();

            NotificationEvent event = new NotificationEvent(
                    parentWriterId,
                    NotificationType.COMMENT_REPLY,
                    boardType,
                    post.getId(),
                    saved.getId(),
                    path,
                    msg,
                    eventId
            );

            publishAfterCommit(event);

        } catch (Exception e) {
            return;
        }
    }

    private BoardType resolveBoardType(BasePost post) {
        Class<?> cls = ClassUtils.getUserClass(post);
        String name = (cls != null) ? cls.getSimpleName() : "";

        if ("Music".equalsIgnoreCase(name)) {
            return BoardType.MUSIC;
        }

        if ("News".equalsIgnoreCase(name)) {
            return BoardType.NEWS;
        }

        if ("Notice".equalsIgnoreCase(name)) {
            return BoardType.NOTICE;
        }

        return null;
    }

    private String buildPostCommentMessage(Member writer, String content) {
        String who = (writer.getDisplayName() != null && !writer.getDisplayName().isBlank())
                ? writer.getDisplayName()
                : "누군가";

        String preview = buildContentPreview(content);

        if (preview.isEmpty()) {
            return who + "님이 내 게시글에 댓글을 남겼습니다.";
        }

        return who + "님이 내 게시글에 댓글을 남겼습니다.\n" + preview;
    }

    private String buildCommentReplyMessage(Member writer, String content) {
        String who = (writer.getDisplayName() != null && !writer.getDisplayName().isBlank())
                ? writer.getDisplayName()
                : "누군가";

        String preview = buildContentPreview(content);

        if (preview.isEmpty()) {
            return who + "님이 내 댓글에 답글을 남겼습니다.";
        }

        return who + "님이 내 댓글에 답글을 남겼습니다.\n" + preview;
    }

    private String buildContentPreview(String content) {
        if (content == null) {
            return "";
        }

        String s = content.replace("\r", "").trim();
        if (s.isEmpty()) {
            return "";
        }

        if (s.length() <= 80) {
            return s;
        }

        return s.substring(0, 80) + "...";
    }

    // 답글 "전체 개수" 계산 (해당 댓글의 모든 자손 개수)
    private Map<Long, Long> buildReplyCountMap(BasePost post) {
        List<CommentRepository.CommentIdParentView> rows = commentRepository.findIdAndParentIdByPost(post);

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        Set<Long> allIds = new HashSet<>();

        for (var r : rows) {
            Long id = r.getId();
            Long parentId = r.getParentId();
            allIds.add(id);

            if (parentId != null) {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(id);
            }
        }

        Map<Long, Long> memo = new HashMap<>();
        for (Long id : allIds) {
            dfsCount(id, childrenMap, memo);
        }

        return memo;
    }

    private long dfsCount(Long id,
                          Map<Long, List<Long>> childrenMap,
                          Map<Long, Long> memo) {

        if (memo.containsKey(id)) {
            return memo.get(id);
        }

        long sum = 0L;
        List<Long> children = childrenMap.get(id);
        if (children != null) {
            for (Long childId : children) {
                sum += 1L; // 직계 1개
                sum += dfsCount(childId, childrenMap, memo); // 자손들
            }
        }

        memo.put(id, sum);
        return sum;
    }
}
