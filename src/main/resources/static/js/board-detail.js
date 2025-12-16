// /js/board-detail.js
(function () {
// 스크랩이나 북마크기능 안넣을거면 나중에 아래 위치 메모해둔거 지우기.
    const rootMain = document.querySelector('main.board-container');
    const isLoggedIn = !!(rootMain && rootMain.dataset.login === 'true');

    function requireLogin(message) {
        const msg = message || '로그인이 필요합니다.\n로그인 페이지로 이동합니다.';

        function goLogin() {
            window.location.href = '/login';
        }

        if (window.showInfoConfirm) {
            showInfoConfirm(msg, goLogin);
        } else if (window.showAppConfirm) {
            showAppConfirm(msg, goLogin);
        } else if (window.showDangerConfirm) {
            showDangerConfirm(msg, goLogin);
        } else {
            if (confirm(msg)) {
                goLogin();
            }
        }
    }

    // 액션 디스패처 (추천/주문 등)
    document.addEventListener('click', function (e) {
        const actionBtn = e.target.closest('.js-post-action');
        if (!actionBtn) {
            return;
        }

        const action = actionBtn.dataset.action;
        if (!action) {
            return;
        }

        if (action === 'recommend') {
            handleRecommend(actionBtn);
        } else if (action === 'order') {
            handleOrder(actionBtn);
        }
        // 향후 scrap / bookmark 등도 여기서 분기 ㄱㄱ
    });

    // 로그인 필요 링크 (글쓰기 / 내가 쓴 글 등)
    document.addEventListener('click', function (e) {
        const loginLink = e.target.closest('.js-login-required-link');
        if (!loginLink) {
            return;
        }

        // 로그인 상태면 그냥 통과
        if (isLoggedIn) {
            return;
        }

        // 비로그인 상태면 기본 이동 막고 로그인 안내
        e.preventDefault();

        const msg = loginLink.dataset.loginMessage
            || '로그인이 필요한 기능입니다.\n로그인 페이지로 이동합니다.';

        requireLogin(msg);
    });

    // 추천 처리
    async function handleRecommend(button) {
        const postId = button.dataset.postId;
        const boardType = button.dataset.boardType;

        if (!postId || !boardType) {
            return;
        }

        if (!isLoggedIn) {
            requireLogin('추천 기능은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return;
        }

        try {
            const res = await fetch(`/${boardType}/${postId}/recommend`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });

            if (res.status === 401) {
                requireLogin('추천 기능은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
                return;
            }

            if (!res.ok) {
                if (window.showAppAlert) {
                    showAppAlert('요청 처리 중 오류가 발생했습니다.');
                } else {
                    alert('요청 처리 중 오류가 발생했습니다.');
                }
                return;
            }

            const body = await res.json();

            // 추천 상태에 따라 스타일 토글
            button.classList.toggle('recommend-box-active', !!body.recommended);

            // 추천 수 갱신 (헤더 + 추천 박스 모두)
            if (typeof body.recommendCount === 'number') {
                const countEls = document.querySelectorAll(
                        '.js-recommend-count[data-post-id="' + postId + '"]'
                );
                countEls.forEach(function (el) {
                    el.textContent = body.recommendCount;
                });
            }

        } catch (e) {
            if (window.showAppAlert) {
                showAppAlert('네트워크 오류가 발생했습니다.');
            } else {
                alert('네트워크 오류가 발생했습니다.');
            }
        }
    }

    // 주문 처리 (item)
    function handleOrder(button) {
        const form = button.closest('form');
        if (!form) {
            return;
        }

        if (!isLoggedIn) {
            requireLogin('주문 기능은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return;
        }

        function doOrder() {
            form.submit();
        }

        const msg = '주문하시겠습니까?\n선택하신 상품을 주문합니다.';

        if (window.showDangerConfirm) {
            showDangerConfirm(msg, doOrder);
        } else if (window.AppModal && window.AppModal.confirm) {
            AppModal.confirm(msg, doOrder);
        } else {
            if (confirm(msg)) {
                doOrder();
            }
        }
    }

    // 게시글 삭제 공통 모달
    const deleteForms = document.querySelectorAll('.post-delete-form');

    deleteForms.forEach(function (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();

            function doSubmit() {
                form.submit();
            }

            const msg = '정말로 삭제하시겠습니까?\n삭제 후에는 되돌릴 수 없습니다.';

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
    });

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

    // 댓글 작성시간 “몇분전/몇시간전/며칠전” 표기
    function formatTimeAgo(fromDate, nowDate) {
        const diffMs = nowDate.getTime() - fromDate.getTime();
        if (diffMs < 0) {
            return '방금 전';
        }

        const diffMin = Math.floor(diffMs / 60000);
        if (diffMin < 1) return '방금 전';
        if (diffMin < 60) return diffMin + '분 전';

        const diffHour = Math.floor(diffMin / 60);
        if (diffHour < 24) return diffHour + '시간 전';

        const diffDay = Math.floor(diffHour / 24);
        return diffDay + '일 전';
    }

    function updateTimeAgoAll() {
        const els = document.querySelectorAll('.js-timeago[data-time]');
        if (!els.length) {
            return;
        }

        const now = new Date();

        els.forEach(function (el) {
            const raw = el.getAttribute('data-time');
            if (!raw) {
                return;
            }

            const dt = new Date(raw);
            if (isNaN(dt.getTime())) {
                return;
            }

            el.textContent = formatTimeAgo(dt, now);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        updateTimeAgoAll();
        setInterval(updateTimeAgoAll, 60000);
    });

})();
