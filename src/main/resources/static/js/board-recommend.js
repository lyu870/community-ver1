// board-recommend.js
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

})();
