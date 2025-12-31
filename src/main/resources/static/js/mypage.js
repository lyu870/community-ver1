// mypage.js
(function () {

    const openProfileBtn = document.getElementById('openProfileBtn');
    const openPasswordBtn = document.getElementById('openPasswordBtn');

    const profileSection = document.getElementById('profileSection');
    const passwordSection = document.getElementById('passwordSection');

    const cancelProfileBtn = document.getElementById('cancelProfileBtn');
    const cancelPasswordBtn = document.getElementById('cancelPasswordBtn');

    const profileForm = document.getElementById('profileForm');
    const passwordForm = document.getElementById('passwordForm');

    const profileMessage = document.getElementById('profileMessage');
    const profileError = document.getElementById('profileError');

    const passwordMessage = document.getElementById('passwordMessage');
    const passwordError = document.getElementById('passwordError');

    const currentNicknameEl = document.getElementById('currentNickname');

    const newPasswordInput = document.querySelector('input[name="newPassword"]');
    const newPasswordConfirmInput = document.querySelector('input[name="newPasswordConfirm"]');
    const passwordMatchMsg = document.getElementById('passwordMatchMsg');

    const notifyPostCommentEnabled = document.getElementById('notifyPostCommentEnabled');
    const notifyCommentReplyEnabled = document.getElementById('notifyCommentReplyEnabled');
    const notifyPostRecommendEnabled = document.getElementById('notifyPostRecommendEnabled');
    const notificationSettingMsg = document.getElementById('notificationSettingMsg');

    function hideProfileMessages() {
        profileMessage.style.display = 'none';
        profileError.style.display = 'none';
        profileMessage.textContent = '';
        profileError.textContent = '';
    }

    function hidePasswordMessages() {
        passwordMessage.style.display = 'none';
        passwordError.style.display = 'none';
        passwordMessage.textContent = '';
        passwordError.textContent = '';
    }

    // 비밀번호 일치 메시지 초기화
    function resetPasswordMatchMsg() {
        if (!passwordMatchMsg) return;
        passwordMatchMsg.textContent = '';
        passwordMatchMsg.style.color = '';
    }

    function resetProfileUI() {
        hideProfileMessages();
        profileForm.reset();
        profileSection.style.display = 'none';
    }

    function resetPasswordUI() {
        hidePasswordMessages();
        passwordForm.reset();
        passwordSection.style.display = 'none';
        resetPasswordMatchMsg();
    }

    function setNotificationMsg(text) {
        if (!notificationSettingMsg) {
            return;
        }

        if (!text) {
            notificationSettingMsg.style.display = 'none';
            notificationSettingMsg.textContent = '';
            return;
        }

        notificationSettingMsg.style.display = 'block';
        notificationSettingMsg.textContent = String(text);
    }

    function setNotificationInputsDisabled(disabled) {
        if (notifyPostCommentEnabled) notifyPostCommentEnabled.disabled = disabled;
        if (notifyCommentReplyEnabled) notifyCommentReplyEnabled.disabled = disabled;
        if (notifyPostRecommendEnabled) notifyPostRecommendEnabled.disabled = disabled;
    }

    async function fetchNotificationSettings() {
        try {
            const res = await fetch('/api/notification-settings', {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
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

    async function updateNotificationSettings(payload) {
        try {
            const headers = {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            };

            if (window.CsrfUtil) {
                CsrfUtil.apply(headers);
            }

            const res = await fetch('/api/notification-settings', {
                method: 'PUT',
                headers: headers,
                body: JSON.stringify(payload || {})
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

    async function initNotificationSettings() {
        if (!notifyPostCommentEnabled || !notifyCommentReplyEnabled || !notifyPostRecommendEnabled) {
            return;
        }

        setNotificationMsg('');
        setNotificationInputsDisabled(true);

        const data = await fetchNotificationSettings();
        if (!data) {
            setNotificationMsg('알림 설정을 불러오지 못했습니다.');
            setNotificationInputsDisabled(false);
            return;
        }

        notifyPostCommentEnabled.checked = (data.postCommentEnabled === true);
        notifyCommentReplyEnabled.checked = (data.commentReplyEnabled === true);
        notifyPostRecommendEnabled.checked = (data.postRecommendEnabled === true);

        setNotificationInputsDisabled(false);
    }

    async function submitNotificationSettings() {
        if (!notifyPostCommentEnabled || !notifyCommentReplyEnabled || !notifyPostRecommendEnabled) {
            return;
        }

        const payload = {
            postCommentEnabled: notifyPostCommentEnabled.checked,
            commentReplyEnabled: notifyCommentReplyEnabled.checked,
            postRecommendEnabled: notifyPostRecommendEnabled.checked
        };

        setNotificationMsg('');
        setNotificationInputsDisabled(true);

        const data = await updateNotificationSettings(payload);
        if (!data) {
            setNotificationMsg('알림 설정 저장에 실패했습니다.');
            setNotificationInputsDisabled(false);
            return;
        }

        notifyPostCommentEnabled.checked = (data.postCommentEnabled === true);
        notifyCommentReplyEnabled.checked = (data.commentReplyEnabled === true);
        notifyPostRecommendEnabled.checked = (data.postRecommendEnabled === true);

        setNotificationInputsDisabled(false);

        if (window.showAppToast) {
            showAppToast('알림 설정이 저장되었습니다.', { variant: 'info', duration: 1500 });
        } else {
            setNotificationMsg('알림 설정이 저장되었습니다.');
            setTimeout(function () {
                setNotificationMsg('');
            }, 1500);
        }
    }

    // 닉네임 변경 버튼 클릭
    if (openProfileBtn) {
        openProfileBtn.addEventListener('click', function () {
            resetPasswordUI();
            hideProfileMessages();
            profileSection.style.display = 'block';
        });
    }

    // 비밀번호 변경 버튼 클릭
    if (openPasswordBtn) {
        openPasswordBtn.addEventListener('click', function () {
            resetProfileUI();
            hidePasswordMessages();
            passwordSection.style.display = 'block';
        });
    }

    // 닉네임 변경 취소 버튼
    if (cancelProfileBtn) {
        cancelProfileBtn.addEventListener('click', function () {
            resetProfileUI();
        });
    }

    // 비밀번호 변경 취소 버튼
    if (cancelPasswordBtn) {
        cancelPasswordBtn.addEventListener('click', function () {
            resetPasswordUI();
        });
    }

    // 새 비밀번호 / 확인 실시간 일치 체크
    function updatePasswordMatchMsg() {
        if (!newPasswordInput || !newPasswordConfirmInput || !passwordMatchMsg) return;

        const pwd = newPasswordInput.value;
        const confirm = newPasswordConfirmInput.value;

        if (!pwd && !confirm) {
            resetPasswordMatchMsg();
            return;
        }

        if (pwd === confirm) {
            passwordMatchMsg.textContent = "비밀번호가 일치합니다.";
            passwordMatchMsg.style.color = "green";
        } else {
            passwordMatchMsg.textContent = "비밀번호가 다릅니다.";
            passwordMatchMsg.style.color = "red";
        }
    }

    if (newPasswordInput && newPasswordConfirmInput && passwordMatchMsg) {
        newPasswordInput.addEventListener('input', updatePasswordMatchMsg);
        newPasswordConfirmInput.addEventListener('input', updatePasswordMatchMsg);
    }

    // 닉네임 변경 폼 AJAX 제출
    if (profileForm) {
        profileForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            hideProfileMessages();

            const formData = new FormData(profileForm);

            try {
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };

                if (window.CsrfUtil) {
                    CsrfUtil.apply(headers);
                }

                const res = await fetch(profileForm.action, {
                    method: 'POST',
                    headers: headers,
                    body: new URLSearchParams(formData)
                });

                const body = await res.json();

                if (body.success) {
                    const newDisplayName = (body.data && body.data.newDisplayName) ? body.data.newDisplayName : null;

                    if (newDisplayName && currentNicknameEl) {
                        currentNicknameEl.textContent = newDisplayName;
                    }

                    showAppAlert(body.message || '닉네임이 변경되었습니다.', function () {
                        resetProfileUI();
                    });
                } else {
                    profileError.textContent = body.error || '닉네임 변경에 실패했습니다.';
                    profileError.style.display = 'block';
                }
            } catch (err) {
                profileError.textContent = '오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                profileError.style.display = 'block';
            }
        });
    }

    // 비밀번호 변경 폼 AJAX 제출
    if (passwordForm) {
        passwordForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            hidePasswordMessages();

            const formData = new FormData(passwordForm);

            try {
                const headers = {
                    'X-Requested-With': 'XMLHttpRequest'
                };

                if (window.CsrfUtil) {
                    CsrfUtil.apply(headers);
                }

                const res = await fetch(passwordForm.action, {
                    method: 'POST',
                    headers: headers,
                    body: new URLSearchParams(formData)
                });

                const body = await res.json();

                if (body.success) {
                    showAppAlert(body.message || '비밀번호가 변경되었습니다.', function () {
                        resetPasswordUI();
                    });
                } else {
                    passwordError.textContent = body.error || '비밀번호 변경에 실패했습니다.';
                    passwordError.style.display = 'block';
                }
            } catch (err) {
                passwordError.textContent = '오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                passwordError.style.display = 'block';
            }
        });
    }

    if (notifyPostCommentEnabled && notifyCommentReplyEnabled && notifyPostRecommendEnabled) {
        notifyPostCommentEnabled.addEventListener('change', submitNotificationSettings);
        notifyCommentReplyEnabled.addEventListener('change', submitNotificationSettings);
        notifyPostRecommendEnabled.addEventListener('change', submitNotificationSettings);
        initNotificationSettings();
    }

})();
