// notification-ui.js
(function () {
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

    function applyCsrf(headers) {
        if (window.CsrfUtil && typeof window.CsrfUtil.apply === 'function') {
            CsrfUtil.apply(headers);
        }
    }

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
        const wrap = document.querySelector('.notify-wrap');
        if (!wrap) {
            return;
        }

        const els = wrap.querySelectorAll('.js-timeago[data-time]');
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

    function buildTitleByType(type) {
        const t = String(type || '').toUpperCase();

        if (t === 'POST_COMMENT') {
            return '댓글 알림';
        }

        if (t === 'COMMENT_REPLY') {
            return '답글 알림';
        }

        if (t === 'BOARD_POST') {
            return '새 글 알림';
        }

        if (t === 'POST_RECOMMEND') {
            return '추천 알림';
        }

        return '알림';
    }

    function isReadItem(n) {
        if (!n) {
            return true;
        }

        if (typeof n.read === 'boolean') {
            return n.read;
        }

        return (n.readAt !== null && n.readAt !== undefined && String(n.readAt) !== '');
    }

    async function fetchUnreadCount() {
        try {
            const res = await fetch('/api/notifications/unread-count', {
                method: 'GET',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (!res.ok) {
                return null;
            }
            const body = await res.json();
            if (!body || body.success !== true || !body.data) {
                return null;
            }
            return body.data;
        } catch (e) {
            return null;
        }
    }

    async function fetchRecentList() {
        try {
            const res = await fetch('/api/notifications?size=10', {
                method: 'GET',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (!res.ok) {
                return null;
            }
            const body = await res.json();
            if (!body || body.success !== true || !body.data) {
                return null;
            }
            return body.data;
        } catch (e) {
            return null;
        }
    }

    async function markRead(id) {
        try {
            const headers = { 'X-Requested-With': 'XMLHttpRequest' };
            applyCsrf(headers);

            const res = await fetch('/api/notifications/' + encodeURIComponent(id) + '/read', {
                method: 'POST',
                headers: headers
            });
            if (!res.ok) {
                return false;
            }
            const body = await res.json();
            if (!body || body.success !== true) {
                return false;
            }
            return true;
        } catch (e) {
            return false;
        }
    }

    function renderEmpty(listEl) {
        listEl.innerHTML = '<div style="color:#666; font-size:0.95rem;">최근 알림이 없습니다.</div>';
    }

    function renderError(listEl) {
        listEl.innerHTML = '<div style="color:#666; font-size:0.95rem;">알림을 불러오지 못했습니다.</div>';
    }

    function renderList(listEl, items) {
        if (!Array.isArray(items) || !items.length) {
            renderEmpty(listEl);
            return;
        }

        const html = items.map(function (n) {
            const id = n.id;

            const type = n.type || '';
            const rawTitle = (n.title !== null && n.title !== undefined) ? String(n.title) : '';
            const titleText = rawTitle.trim() ? rawTitle : buildTitleByType(type);

            const title = escapeHtml(titleText);
            const message = escapeHtml(n.message || '').replaceAll('\n', '<br>');
            const linkUrl = escapeHtml(n.linkUrl || '');
            const createdAt = escapeHtml(n.createdAt || '');

            const isRead = isReadItem(n);

            return `
                <div class="notify-item"
                     data-id="${id}"
                     data-link="${linkUrl}"
                     style="padding:8px; border-top:1px solid #eee; ${isRead ? '' : 'background:rgba(52,152,219,0.06);'} cursor:pointer;">
                    <div style="display:flex; align-items:center; justify-content:space-between; gap:10px;">
                        <strong style="font-size:0.95rem;">${title}</strong>
                        <span class="js-timeago" data-time="${createdAt}" style="font-size:0.85rem; color:#777;">방금 전</span>
                    </div>
                    <div style="margin-top:4px; color:#333; font-size:0.92rem; line-height:1.35;">${message}</div>
                </div>
`;
        }).join('');

        listEl.innerHTML = '<div style="border-bottom:1px solid #eee;"></div>' + html;
    }

    function setBadge(badgeEl, count) {
        const n = Number(count || 0);
        if (n > 0) {
            badgeEl.style.display = 'inline-block';
            badgeEl.textContent = String(n);
        } else {
            badgeEl.style.display = 'none';
            badgeEl.textContent = '0';
        }
    }

    function init() {
        const wrap = document.querySelector('.notify-wrap');
        if (!wrap) {
            return;
        }

        const toggle = wrap.querySelector('.notify-toggle');
        const menu = wrap.querySelector('.notify-menu');
        const listEl = wrap.querySelector('.notify-list');
        const badgeEl = wrap.querySelector('.notify-badge');
        const refreshBtn = wrap.querySelector('.notify-refresh');

        if (!toggle || !menu || !listEl || !badgeEl) {
            return;
        }

        function closeMenu() {
            menu.style.display = 'none';
        }

        function openMenu() {
            menu.style.display = 'block';
        }

        function isOpen() {
            return menu.style.display === 'block';
        }

        async function refreshBadge() {
            const data = await fetchUnreadCount();
            if (!data) {
                return;
            }
            setBadge(badgeEl, data.unreadCount);
        }

        async function refreshList() {
            listEl.innerHTML = '<div style="color:#666; font-size:0.95rem;">알림을 불러오는 중...</div>';

            const data = await fetchRecentList();
            if (!data || !Array.isArray(data.items)) {
                renderError(listEl);
                return;
            }

            renderList(listEl, data.items);
            updateTimeAgoAll();
        }

        toggle.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();

            if (isOpen()) {
                closeMenu();
            } else {
                openMenu();
                refreshList();
            }
        });

        document.addEventListener('click', function (e) {
            if (!wrap.contains(e.target)) {
                closeMenu();
            }
        });

        if (refreshBtn) {
            refreshBtn.addEventListener('click', function (e) {
                e.preventDefault();
                refreshList();
            });
        }

        listEl.addEventListener('click', function (e) {
            const item = e.target.closest('.notify-item');
            if (!item) {
                return;
            }

            const id = item.dataset.id;
            const link = item.dataset.link || '';

            if (!id) {
                return;
            }

            item.style.background = '';

            markRead(id).then(function () {
                refreshBadge();

                if (link) {
                    window.location.href = link;
                } else {
                    refreshList();
                }
            });
        });

        refreshBadge();
        setInterval(refreshBadge, 30000);
        setInterval(updateTimeAgoAll, 60000);
    }

    document.addEventListener('DOMContentLoaded', init);
})();
