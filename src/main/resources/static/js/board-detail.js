// board-detail.js
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

    function showReplyLoadFail() {
        if (window.showAppToast) {
            showAppToast('답글을 불러오지 못했습니다.', {
                variant: 'warning',
                duration: 2000
            });
        } else {
            alert('답글을 불러오지 못했습니다.');
        }
    }

    function showReplySubmitFail() {
        if (window.showAppToast) {
            showAppToast('답글을 등록하지 못했습니다.', {
                variant: 'warning',
                duration: 2000
            });
        } else {
            alert('답글을 등록하지 못했습니다.');
        }
    }

    function showCommentSubmitFail() {
        if (window.showAppToast) {
            showAppToast('댓글을 등록하지 못했습니다.', {
                variant: 'warning',
                duration: 2000
            });
        } else {
            alert('댓글을 등록하지 못했습니다.');
        }
    }

    function showCommentEditFail() {
        if (window.showAppToast) {
            showAppToast('댓글을 수정하지 못했습니다.', {
                variant: 'warning',
                duration: 2000
            });
        } else {
            alert('댓글을 수정하지 못했습니다.');
        }
    }

    function showCommentDeleteFail() {
        if (window.showAppToast) {
            showAppToast('댓글을 삭제하지 못했습니다.', {
                variant: 'warning',
                duration: 2000
            });
        } else {
            alert('댓글을 삭제하지 못했습니다.');
        }
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) {
            return '';
        }
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function getCommentContext() {
        const tree = document.querySelector('.comment-tree');
        if (!tree) {
            return null;
        }

        const postId = tree.dataset.postId;
        const loginUserId = tree.dataset.loginUserId || '';
        const isAdmin = tree.dataset.isAdmin || '';

        if (!postId) {
            return null;
        }

        return {
            postId: postId,
            loginUserId: loginUserId,
            isAdmin: isAdmin
        };
    }

    function canEdit(writerId) {
        const ctx = getCommentContext();
        if (!ctx) {
            return false;
        }

        if (!ctx.loginUserId) {
            return false;
        }

        return String(writerId) === String(ctx.loginUserId);
    }

    function canDelete(writerId) {
        const ctx = getCommentContext();
        if (!ctx) {
            return false;
        }

        const isAdmin = (String(ctx.isAdmin) === 'true');

        if (isAdmin) {
            return true;
        }

        if (!ctx.loginUserId) {
            return false;
        }

        return String(writerId) === String(ctx.loginUserId);
    }

    function isEdited(createdAt, updatedAt) {
        if (!createdAt || !updatedAt) {
            return false;
        }

        const c = new Date(createdAt);
        const u = new Date(updatedAt);

        if (isNaN(c.getTime()) || isNaN(u.getTime())) {
            return false;
        }

        return u.getTime() > c.getTime();
    }

    function getMetaCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        if (!meta) {
            return '';
        }
        return meta.getAttribute('content') || '';
    }

    function getMetaCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        if (!meta) {
            return '';
        }
        return meta.getAttribute('content') || '';
    }

    function applyCsrf(headers) {
        if (window.CsrfUtil && typeof window.CsrfUtil.apply === 'function') {
            CsrfUtil.apply(headers);
            return;
        }

        const token = getMetaCsrfToken();
        const headerName = getMetaCsrfHeader();

        if (!token || !headerName) {
            return;
        }

        headers[headerName] = token;
    }

    async function createCommentApi(postId, content, parentId) {
        const body = new URLSearchParams();
        body.append('postId', String(postId));
        body.append('content', String(content || ''));

        if (parentId !== null && parentId !== undefined && String(parentId) !== '') {
            body.append('parentId', String(parentId));
        }

        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            'X-Requested-With': 'XMLHttpRequest'
        };
        applyCsrf(headers);

        const res = await fetch('/api/comment', {
            method: 'POST',
            headers: headers,
            body: body.toString()
        });

        if (res.status === 401 || res.status === 403) {
            requireLogin('댓글 작성은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return null;
        }

        if (res.redirected && res.url && res.url.indexOf('/login') !== -1) {
            requireLogin('댓글 작성은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return null;
        }

        if (!res.ok) {
            return null;
        }

        const json = await res.json();
        if (!json || json.success !== true || !json.data) {
            return null;
        }

        return json.data;
    }

    async function updateCommentApi(commentId, content) {
        const body = new URLSearchParams();
        body.append('content', String(content || ''));

        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            'X-Requested-With': 'XMLHttpRequest'
        };
        applyCsrf(headers);

        const res = await fetch('/api/comment/' + encodeURIComponent(commentId), {
            method: 'PUT',
            headers: headers,
            body: body.toString()
        });

        if (res.status === 401 || res.status === 403) {
            requireLogin('댓글 수정은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return null;
        }

        if (res.redirected && res.url && res.url.indexOf('/login') !== -1) {
            requireLogin('댓글 수정은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return null;
        }

        if (!res.ok) {
            return null;
        }

        const json = await res.json();
        if (!json || json.success !== true || !json.data) {
            return null;
        }

        return json.data;
    }

    async function deleteCommentApi(commentId) {
        const headers = { 'X-Requested-With': 'XMLHttpRequest' };
        applyCsrf(headers);

        const res = await fetch('/api/comment/' + encodeURIComponent(commentId), {
            method: 'DELETE',
            headers: headers
        });

        if (res.status === 401 || res.status === 403) {
            requireLogin('댓글 삭제는 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return false;
        }

        if (res.redirected && res.url && res.url.indexOf('/login') !== -1) {
            requireLogin('댓글 삭제는 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return false;
        }

        if (!res.ok) {
            return false;
        }

        const json = await res.json();
        if (!json || json.success !== true) {
            return false;
        }

        return true;
    }

    function renderCommentNode(child, depth) {
        const ctx = getCommentContext();
        if (!ctx) {
            return '';
        }

        const id = child.id;
        const writerId = child.writerId;
        const writerName = escapeHtml(child.writerDisplayName || '');
        const contentRaw = (child.content || '');
        const content = escapeHtml(contentRaw).replaceAll('\n', '<br>');

        const createdAt = child.createdAt;
        const updatedAt = child.updatedAt;

        const replyCount = Number(child.replyCount || 0);
        const edited = isEdited(createdAt, updatedAt);

        if (contentRaw === '삭제된 댓글입니다.') {
            return `
    <div class="comment-node" style="margin-left:${depth * 20}px" data-comment-id="${id}" data-writer-id="${writerId}" data-created-at="${escapeHtml(createdAt || '')}">
        <div class="comment-item">
            <div class="comment-body">
                <div class="comment-deleted">삭제된 댓글입니다.</div>
            </div>
        </div>
    </div>
`;
        }

        const editBtnHtml = canEdit(writerId)
            ? `
                        <span>
                            <button type="button" class="comment-edit-btn" data-comment-id="${id}">수정</button>
                        </span>
`
            : '';

        const deleteBtnHtml = canDelete(writerId)
            ? `
                        <span>
                            <form action="/comment/delete" method="post" class="comment-delete-form" style="display:inline;">
                                <input type="hidden" name="id" value="${id}">
                                <input type="hidden" name="postId" value="${ctx.postId}">
                                <button type="button" class="comment-delete-btn">삭제</button>
                            </form>
                        </span>
`
            : '';

        const repliesToggleHtml = (replyCount > 0)
            ? `
                <div>
                    <button type="button"
                            class="comment-replies-toggle"
                            data-comment-id="${id}"
                            data-reply-count="${replyCount}"
                            data-depth="${depth + 1}">
                        답글 <span>${replyCount}</span>개
                    </button>

                    <div class="comment-children" data-parent-id="${id}"></div>
                </div>
`
            : '';

        return `
    <div class="comment-node" style="margin-left:${depth * 20}px" data-comment-id="${id}" data-writer-id="${writerId}" data-created-at="${escapeHtml(createdAt || '')}">
        <div class="comment-item">
            <div class="comment-body">

                <div class="comment-writer">
                    <strong>${writerName}</strong>
                    <span class="comment-time js-timeago" data-time="${escapeHtml(createdAt || '')}">방금 전</span>
                    ${edited ? '<span class="comment-edited">(수정됨)</span>' : ''}
                </div>

                <p class="comment-content" data-comment-id="${id}">${content}</p>

                ${canEdit(writerId) ? `
                <form class="comment-edit-form"
                      action="/comment/edit"
                      method="post"
                      data-comment-id="${id}"
                      style="display:none;">
                    <input type="hidden" name="id" value="${id}">
                    <input type="hidden" name="postId" value="${ctx.postId}">
                    <textarea name="content" rows="2">${escapeHtml(contentRaw)}</textarea>
                    <button type="submit">저장</button>
                    <button type="button" class="comment-edit-cancel">취소</button>
                </form>
                ` : ''}

                <div class="comment-actions">
                    <button type="button" class="comment-reply-btn" data-comment-id="${id}">답글</button>
                    ${editBtnHtml}
                    ${deleteBtnHtml}
                </div>

                <form class="comment-reply-form"
                      action="/comment"
                      method="post"
                      data-parent-id="${id}"
                      style="display:none;"
                      data-require-login="comment">
                    <input type="hidden" name="postId" value="${ctx.postId}">
                    <input type="hidden" name="parentId" value="${id}">
                    <textarea name="content" rows="2" placeholder="답글을 입력하세요" required></textarea>

                    <div class="comment-reply-actions">
                        <button type="button" class="comment-reply-cancel">취소</button>
                        <button type="submit">등록</button>
                    </div>
                </form>

                ${repliesToggleHtml}

            </div>
        </div>
    </div>
`;
    }

    function removeMoreButton(childrenBox) {
        const moreBtn = childrenBox.querySelector('.comment-children-more');
        if (moreBtn) {
            moreBtn.remove();
        }
    }

    function renderMoreButton(parentId, nextPage, depth, childrenBox) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'comment-children-more';
        btn.dataset.parentId = parentId;
        btn.dataset.nextPage = String(nextPage);
        btn.dataset.depth = String(depth);
        btn.textContent = '답글 더보기';
        childrenBox.appendChild(btn);
    }

    function invalidateChildrenCache(parentId) {
        const childrenBox = document.querySelector('.comment-children[data-parent-id="' + parentId + '"]');
        if (!childrenBox) {
            return;
        }

        childrenBox.dataset.loaded = '';
        childrenBox.dataset.pageMax = '';
        childrenBox.dataset.totalPages = '';
        childrenBox.dataset.hasNext = '';
        childrenBox.innerHTML = '';
    }

    async function loadChildrenIntoBox(parentId, depth, childrenBox, page) {
        const ctx = getCommentContext();
        if (!ctx) {
            showReplyLoadFail();
            return false;
        }

        const targetPage = (typeof page === 'number') ? page : 0;
        const size = 10;

        try {
            const url =
                '/api/comment/children?postId=' + encodeURIComponent(ctx.postId)
                + '&parentId=' + encodeURIComponent(parentId)
                + '&page=' + encodeURIComponent(targetPage)
                + '&size=' + encodeURIComponent(size);

            const res = await fetch(url, {
                method: 'GET',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });

            if (!res.ok) {
                showReplyLoadFail();
                return false;
            }

            const body = await res.json();

            if (!body || body.success !== true || !body.data || !Array.isArray(body.data.items)) {
                showReplyLoadFail();
                return false;
            }

            // 더보기 버튼 중복 방지
            removeMoreButton(childrenBox);

            const html = body.data.items.map(function (child) {
                return renderCommentNode(child, Number(depth));
            }).join('');

            if (targetPage === 0) {
                childrenBox.innerHTML = html;
                childrenBox.dataset.loaded = 'true';
                childrenBox.dataset.pageMax = '0';
            } else {
                childrenBox.insertAdjacentHTML('beforeend', html);
                childrenBox.dataset.loaded = 'true';
                childrenBox.dataset.pageMax = String(targetPage);
            }

            childrenBox.dataset.totalPages = String(Number(body.data.totalPages || 0));
            childrenBox.dataset.hasNext = (body.data.hasNext ? 'true' : 'false');

            if (body.data.hasNext) {
                renderMoreButton(parentId, targetPage + 1, depth, childrenBox);
            }

            // 새로 붙은 답글들 timeago 즉시 반영
            updateTimeAgoAll();

            return true;

        } catch (e) {
            showReplyLoadFail();
            return false;
        }
    }

    // 액션 디스패처 (추천 등)
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
            const headers = { 'X-Requested-With': 'XMLHttpRequest' };
            applyCsrf(headers);

            const res = await fetch(`/api/${boardType}/${postId}/recommend`, {
                method: 'POST',
                headers: headers
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

            if (!body || body.success !== true || !body.data) {
                if (window.showAppAlert) {
                    showAppAlert((body && body.error) ? body.error : '요청 처리 중 오류가 발생했습니다.');
                } else {
                    alert((body && body.error) ? body.error : '요청 처리 중 오류가 발생했습니다.');
                }
                return;
            }

            const data = body.data;

            // 추천 상태에 따라 스타일 토글
            button.classList.toggle('recommend-box-active', !!data.recommended);

            // 추천 수 갱신 (헤더 + 추천 박스 모두)
            if (typeof data.recommendCount === 'number') {
                const countEls = document.querySelectorAll(
                        '.js-recommend-count[data-post-id="' + postId + '"]'
                );
                countEls.forEach(function (el) {
                    el.textContent = data.recommendCount;
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

    function normalizeText(str) {
        if (str === null || str === undefined) {
            return '';
        }
        return String(str).replace(/\s+/g, ' ').trim();
    }

    function flashNode(targetNode) {
        if (!targetNode || !targetNode.classList) {
            return;
        }

        targetNode.classList.remove('comment-focus');
        targetNode.classList.remove('comment-flash');

        targetNode.offsetHeight;

        targetNode.classList.add('comment-focus');
        targetNode.classList.add('comment-flash');

        setTimeout(function () {
            targetNode.classList.remove('comment-flash');
        }, 900);

        setTimeout(function () {
            targetNode.classList.remove('comment-focus');
        }, 2400);
    }

    function getCommentNodeDepth(node) {
        if (!node) {
            return 0;
        }

        const style = node.getAttribute('style') || '';
        const m = style.match(/margin-left\s*:\s*(\d+)px/i);
        if (!m) {
            return 0;
        }

        const px = Number(m[1]);
        if (!isFinite(px) || px < 0) {
            return 0;
        }

        return Math.round(px / 20);
    }

    function ensureRepliesUi(parentId) {
        const parentNode = document.querySelector('.comment-node[data-comment-id="' + parentId + '"]');
        if (!parentNode) {
            return null;
        }

        let toggleBtn = document.querySelector('.comment-replies-toggle[data-comment-id="' + parentId + '"]');
        let childrenBox = document.querySelector('.comment-children[data-parent-id="' + parentId + '"]');

        let depth = 1;

        if (!toggleBtn || !childrenBox) {
            const parentDepth = getCommentNodeDepth(parentNode);
            depth = parentDepth + 1;

            const target = parentNode.querySelector('.comment-body') || parentNode;

            const wrap = document.createElement('div');
            wrap.innerHTML = `
                <div>
                    <button type="button"
                            class="comment-replies-toggle"
                            data-comment-id="${parentId}"
                            data-reply-count="0"
                            data-depth="${depth}">
                        답글 <span>0</span>개
                    </button>

                    <div class="comment-children" data-parent-id="${parentId}"></div>
                </div>
`;

            target.appendChild(wrap.firstElementChild);

            toggleBtn = document.querySelector('.comment-replies-toggle[data-comment-id="' + parentId + '"]');
            childrenBox = document.querySelector('.comment-children[data-parent-id="' + parentId + '"]');
        } else {
            depth = Number(toggleBtn.dataset.depth || '1');
            if (!isFinite(depth) || depth < 1) {
                depth = 1;
            }
        }

        return {
            toggleBtn: toggleBtn,
            childrenBox: childrenBox,
            depth: depth
        };
    }

    function findReplyNodeById(childrenBox, commentId) {
        if (!childrenBox || !commentId) {
            return null;
        }

        return childrenBox.querySelector('.comment-node[data-comment-id="' + commentId + '"]');
    }

    async function loadChildrenUntilFoundById(parentId, depth, childrenBox, targetId) {
        let page = 0;
        let guard = 0;
        let totalPages = 0;

        while (guard < 200) {
            const ok = await loadChildrenIntoBox(parentId, depth, childrenBox, page);
            if (!ok) {
                return null;
            }

            const found = findReplyNodeById(childrenBox, targetId);
            if (found) {
                return found;
            }

            totalPages = Number(childrenBox.dataset.totalPages || '0');
            if (!isFinite(totalPages) || totalPages < 1) {
                break;
            }

            if (page >= totalPages - 1) {
                break;
            }

            page += 1;
            guard += 1;
        }

        return null;
    }

    async function submitReplyFormAjax(replyForm, parentId) {
        const ctx = getCommentContext();
        if (!ctx) {
            return;
        }

        const textarea = replyForm.querySelector('textarea[name="content"]');
        const content = textarea ? textarea.value : '';

        const submitBtn = replyForm.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
        }

        try {
            const saved = await createCommentApi(ctx.postId, content, parentId);
            if (!saved || !saved.id) {
                showReplySubmitFail();
                return;
            }

            const ui = ensureRepliesUi(parentId);
            if (!ui || !ui.childrenBox) {
                showReplySubmitFail();
                return;
            }

            ui.childrenBox.classList.add('is-open');
            if (ui.toggleBtn) {
                ui.toggleBtn.textContent = '답글 숨기기';
            }

            invalidateChildrenCache(parentId);

            const found = await loadChildrenUntilFoundById(parentId, ui.depth, ui.childrenBox, String(saved.id));

            const count = ui.childrenBox.querySelectorAll('.comment-node').length;
            if (ui.toggleBtn) {
                ui.toggleBtn.dataset.replyCount = String(Math.max(Number(ui.toggleBtn.dataset.replyCount || '0'), count));
            }

            if (found && found.scrollIntoView) {
                found.scrollIntoView({ behavior: 'smooth', block: 'center' });
                flashNode(found);
            } else {
                const parentNode = document.querySelector('.comment-node[data-comment-id="' + parentId + '"]');
                if (parentNode && parentNode.scrollIntoView) {
                    parentNode.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    flashNode(parentNode);
                }
            }

            if (textarea) {
                textarea.value = '';
            }
            replyForm.style.display = 'none';

        } catch (e) {
            showReplySubmitFail();
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
            }
        }
    }

    async function submitRootCommentAjax(form) {
        const ctx = getCommentContext();
        if (!ctx) {
            return;
        }

        const textarea = form.querySelector('textarea[name="content"]');
        const content = textarea ? textarea.value : '';

        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
        }

        try {
            const saved = await createCommentApi(ctx.postId, content, null);
            if (!saved || !saved.id) {
                showCommentSubmitFail();
                return;
            }

            const tree = document.querySelector('.comment-tree');
            if (!tree) {
                showCommentSubmitFail();
                return;
            }

            const html = renderCommentNode(saved, 0);
            tree.insertAdjacentHTML('beforeend', html);

            updateTimeAgoAll();

            const node = document.querySelector('.comment-node[data-comment-id="' + saved.id + '"]');
            if (node && node.scrollIntoView) {
                node.scrollIntoView({ behavior: 'smooth', block: 'center' });
                flashNode(node);
            }

            if (textarea) {
                textarea.value = '';
            }

            form.classList.remove('comment-form-active');

        } catch (e) {
            showCommentSubmitFail();
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
            }
        }
    }

    async function submitEditFormAjax(editForm, commentId) {
        const textarea = editForm.querySelector('textarea[name="content"]');
        const content = textarea ? textarea.value : '';

        const submitBtn = editForm.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
        }

        try {
            const updated = await updateCommentApi(commentId, content);
            if (!updated) {
                showCommentEditFail();
                return;
            }

            const node = document.querySelector('.comment-node[data-comment-id="' + commentId + '"]');
            if (!node) {
                showCommentEditFail();
                return;
            }

            const contentEl = node.querySelector('.comment-content[data-comment-id="' + commentId + '"]');
            if (contentEl) {
                contentEl.innerHTML = escapeHtml(updated.content || '').replaceAll('\n', '<br>');
                contentEl.style.display = '';
            }

            editForm.style.display = 'none';

            const writerBox = node.querySelector('.comment-writer');
            if (writerBox) {
                let editedEl = writerBox.querySelector('.comment-edited');
                if (!editedEl) {
                    editedEl = document.createElement('span');
                    editedEl.className = 'comment-edited';
                    editedEl.textContent = '(수정됨)';
                    writerBox.appendChild(editedEl);
                }
            }

            if (node.scrollIntoView) {
                node.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }

        } catch (e) {
            showCommentEditFail();
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
            }
        }
    }

    // 댓글 삭제 공통 모달 + AJAX
    document.addEventListener('click', function (e) {
        if (!e.target.classList.contains('comment-delete-btn')) {
            return;
        }

        const form = e.target.closest('form');
        if (!form) {
            return;
        }

        const idInput = form.querySelector('input[name="id"]');
        const commentId = idInput ? idInput.value : '';
        if (!commentId) {
            return;
        }

        e.preventDefault();

        function doDelete() {
            deleteCommentApi(commentId).then(function (ok) {
                if (!ok) {
                    showCommentDeleteFail();
                    return;
                }

                const node = document.querySelector('.comment-node[data-comment-id="' + commentId + '"]');
                if (!node) {
                    return;
                }

                const childrenBox = node.closest('.comment-children[data-parent-id]');
                const removedCount = 1 + node.querySelectorAll('.comment-node').length;

                node.remove();

                if (childrenBox) {
                    const parentId = childrenBox.dataset.parentId;
                    const toggleBtn = document.querySelector('.comment-replies-toggle[data-comment-id="' + parentId + '"]');
                    if (toggleBtn) {
                        const prev = Number(toggleBtn.dataset.replyCount || '0');
                        const next = Math.max(0, prev - removedCount);
                        toggleBtn.dataset.replyCount = String(next);

                        if (toggleBtn.textContent !== '답글 숨기기') {
                            toggleBtn.textContent = '답글 ' + next + '개';
                        }
                    }
                }
            });
        }

        const msg = '댓글을 삭제하시겠습니까?';

        if (window.showDangerConfirm) {
            showDangerConfirm(msg, doDelete);
        } else if (window.AppModal && window.AppModal.confirm) {
            AppModal.confirm(msg, doDelete);
        } else {
            if (confirm(msg)) {
                doDelete();
            }
        }
    });

    // 댓글 수정/답글 토글 공통 처리
    document.addEventListener('click', async function (e) {

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

        // 답글 n개 보기/숨기기
        const toggleBtn = e.target.closest('.comment-replies-toggle');
        if (toggleBtn) {
            const commentId = toggleBtn.dataset.commentId;
            const replyCount = toggleBtn.dataset.replyCount;
            const depth = toggleBtn.dataset.depth;

            const childrenBox = document.querySelector('.comment-children[data-parent-id="' + commentId + '"]');
            if (!childrenBox) {
                return;
            }

            const isOpen = childrenBox.classList.toggle('is-open');

            if (isOpen) {
                toggleBtn.textContent = '답글 숨기기';

                // 답글이 처음 열릴 때만 서버에서 가져와서 붙이기 (page=0)
                if (childrenBox.dataset.loaded !== 'true') {
                    const ok = await loadChildrenIntoBox(commentId, depth, childrenBox, 0);
                    if (!ok) {
                        childrenBox.classList.remove('is-open');
                        toggleBtn.textContent = '답글 ' + replyCount + '개';
                    }
                }
            } else {
                toggleBtn.textContent = '답글 ' + replyCount + '개';
            }
            return;
        }

        // 답글 더보기
        const moreBtn = e.target.closest('.comment-children-more');
        if (moreBtn) {
            const parentId = moreBtn.dataset.parentId;
            const nextPage = Number(moreBtn.dataset.nextPage);
            const depth = moreBtn.dataset.depth;

            const childrenBox = document.querySelector('.comment-children[data-parent-id="' + parentId + '"]');
            if (!childrenBox) {
                return;
            }

            await loadChildrenIntoBox(parentId, depth, childrenBox, nextPage);
            return;
        }

        // 댓글 작성창 취소
        const cancelBtn = e.target.closest('.js-comment-cancel');
        if (cancelBtn) {
            const form = cancelBtn.closest('.js-comment-form');
            if (!form) {
                return;
            }
            const textarea = form.querySelector('.js-comment-input');
            if (textarea) {
                textarea.value = '';
            }
            form.classList.remove('comment-form-active');
            return;
        }

    });

    // 댓글/답글: 비로그인 시 공통 로그인 안내
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

    // 댓글 작성창: 포커스 시 버튼 노출
    document.addEventListener('focusin', function (e) {
        const input = e.target.closest('.js-comment-input');
        if (!input) {
            return;
        }

        const form = input.closest('.js-comment-form');
        if (!form) {
            return;
        }

        form.classList.add('comment-form-active');
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

    // 댓글 작성 AJAX (루트)
    document.addEventListener('submit', function (e) {
        const form = e.target.closest('.js-comment-form');
        if (!form) {
            return;
        }

        const ctx = getCommentContext();
        if (!ctx) {
            return;
        }

        if (!isLoggedIn) {
            e.preventDefault();
            requireLogin('댓글 작성은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return;
        }

        e.preventDefault();
        submitRootCommentAjax(form);
    });

    // 댓글 수정 AJAX
    document.addEventListener('submit', function (e) {
        const editForm = e.target.closest('.comment-edit-form');
        if (!editForm) {
            return;
        }

        const commentId = editForm.dataset.commentId;
        if (!commentId) {
            return;
        }

        const ctx = getCommentContext();
        if (!ctx) {
            return;
        }

        if (!isLoggedIn) {
            e.preventDefault();
            requireLogin('댓글 수정은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return;
        }

        e.preventDefault();
        submitEditFormAjax(editForm, commentId);
    });

    // 답글 작성 AJAX
    document.addEventListener('submit', function (e) {
        const replyForm = e.target.closest('.comment-reply-form');
        if (!replyForm) {
            return;
        }

        const parentId = replyForm.dataset.parentId;
        if (!parentId) {
            return;
        }

        const ctx = getCommentContext();
        if (!ctx) {
            return;
        }

        if (!isLoggedIn) {
            e.preventDefault();
            requireLogin('댓글 작성은 로그인 후 이용 가능합니다.\n로그인 페이지로 이동합니다.');
            return;
        }

        e.preventDefault();
        submitReplyFormAjax(replyForm, parentId);
    });

    // 답글 작성 후 자동으로 해당 답글 영역 펼치기
    async function openRepliesFromQuery() {
        const ctx = getCommentContext();
        if (!ctx) {
            return;
        }

        const params = new URLSearchParams(window.location.search);

        const openReplyPath = params.get('openReplyPath');
        const focusCommentId = params.get('focusCommentId');

        if (openReplyPath) {
            let path = [];
            try {
                path = decodeURIComponent(openReplyPath).split(',').map(function (v) {
                    return String(v || '').trim();
                }).filter(Boolean);
            } catch (e) {
                path = [];
            }

            if (path.length) {
                for (let i = 0; i < path.length; i++) {
                    const parentId = path[i];
                    const ui = ensureRepliesUi(parentId);
                    if (!ui || !ui.childrenBox || !ui.toggleBtn) {
                        continue;
                    }

                    ui.childrenBox.classList.add('is-open');
                    ui.toggleBtn.textContent = '답글 숨기기';

                    const nextId = (i + 1 < path.length) ? path[i + 1] : null;

                    if (nextId) {
                        await loadChildrenUntilFoundById(parentId, ui.depth, ui.childrenBox, nextId);
                    } else {
                        if (focusCommentId) {
                            await loadChildrenUntilFoundById(parentId, ui.depth, ui.childrenBox, String(focusCommentId));
                        } else {
                            await loadChildrenIntoBox(parentId, ui.depth, ui.childrenBox, 0);
                        }
                    }
                }

                const targetId = focusCommentId ? String(focusCommentId) : String(path[path.length - 1]);
                const targetNode = document.querySelector('.comment-node[data-comment-id="' + targetId + '"]');
                if (targetNode && targetNode.scrollIntoView) {
                    targetNode.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    flashNode(targetNode);
                }
            }

            params.delete('openReplyPath');
            params.delete('focusCommentId');
            const qs = params.toString();
            const nextUrl = window.location.pathname + (qs ? ('?' + qs) : '') + window.location.hash;
            if (window.history && window.history.replaceState) {
                window.history.replaceState({}, document.title, nextUrl);
            }

            return;
        }

        if (focusCommentId) {
            const targetId = String(focusCommentId);
            const targetNode = document.querySelector('.comment-node[data-comment-id="' + targetId + '"]');
            if (targetNode && targetNode.scrollIntoView) {
                targetNode.scrollIntoView({ behavior: 'smooth', block: 'center' });
                flashNode(targetNode);
            }

            params.delete('focusCommentId');
            const qs = params.toString();
            const nextUrl = window.location.pathname + (qs ? ('?' + qs) : '') + window.location.hash;
            if (window.history && window.history.replaceState) {
                window.history.replaceState({}, document.title, nextUrl);
            }

            return;
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        updateTimeAgoAll();
        setInterval(updateTimeAgoAll, 60000);

        // 답글 작성 후 펼치기
        openRepliesFromQuery();
    });

})();
