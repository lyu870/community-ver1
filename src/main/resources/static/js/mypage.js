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

})();
