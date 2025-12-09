// board-comment.js
(function () {

    const rootMain = document.querySelector('main.board-container');
    const isLoggedIn = !!(rootMain && rootMain.dataset.login === 'true');

    function requireLogin(message) {
        const msg = message || '로그인이 필요합니다.\n로그인 페이지로 이동합니다.';
        if (window.showAppAlert) {
            showAppAlert(msg, function () {
                window.location.href = '/login';
            });
        } else {
            alert(msg);
            window.location.href = '/login';
        }
    }

    // 댓글 삭제 공통 모달
    document.addEventListener('click', function (e) {
        if (!e.target.classList.contains('comment-delete-btn')) {
            return;
        }

        const form = e.target.closest('form');
        if (!form) {
            return;
        }

        e.preventDefault();

        function doSubmit() {
            form.submit();
        }

        const msg = '댓글을 삭제하시겠습니까?';

        if (window.showDangerConfirm) {
            showDangerConfirm(msg, doSubmit);
        } else if (window.AppModal && window.AppModal.confirm) {
            AppModal.confirm(msg, doSubmit);
        } else {
            if (confirm(msg)) {
                doSubmit();
            }
        }
    });

    // 댓글 수정/답글 토글 공통 처리
    document.addEventListener('click', function (e) {

        // 댓글 수정 버튼
        if (e.target.classList.contains('comment-edit-btn')) {
            const id = e.target.dataset.commentId;
            const contentEl = document.querySelector('.comment-content[data-comment-id="' + id + '"]');
            const formEl = document.querySelector('.comment-edit-form[data-comment-id="' + id + '"]');
            if (contentEl && formEl) {
                contentEl.style.display = 'none';
                formEl.style.display = 'flex';
            }
            return;
        }

        // 댓글 수정 취소
        if (e.target.classList.contains('comment-edit-cancel')) {
            const form = e.target.closest('.comment-edit-form');
            if (!form) {
                return;
            }
            const id = form.dataset.commentId;
            const contentEl = document.querySelector('.comment-content[data-comment-id="' + id + '"]');
            form.style.display = 'none';
            if (contentEl) {
                contentEl.style.display = '';
            }
            return;
        }

        // 답글 버튼
        if (e.target.classList.contains('comment-reply-btn')) {
            const id = e.target.dataset.commentId;
            const replyForm = document.querySelector('.comment-reply-form[data-parent-id="' + id + '"]');
            if (replyForm) {
                replyForm.style.display = 'block';
            }
            return;
        }

        // 답글 취소
        if (e.target.classList.contains('comment-reply-cancel')) {
            const replyForm = e.target.closest('.comment-reply-form');
            if (replyForm) {
                replyForm.style.display = 'none';
            }
            return;
        }

    });

    // 댓글 작성/답글: 비로그인 시 공통 로그인 안내
    document.addEventListener('focusin', function (e) {
        if (isLoggedIn) {
            return;
        }

        const wrapper = e.target.closest('[data-require-login="comment"]');
        if (!wrapper) {
            return;
        }

        e.preventDefault();
        requireLogin('댓글 작성은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
    });

})();
