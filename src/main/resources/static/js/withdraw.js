// /js/withdraw.js
(function () {

    const withdrawCodeInput = document.getElementById('withdrawCodeInput');
    const sendWithdrawCodeBtn = document.getElementById('sendWithdrawCodeBtn');
    const withdrawBtnBottom = document.getElementById('withdrawBtnBottom');
    const withdrawMessage = document.getElementById('withdrawMessage');
    const withdrawError = document.getElementById('withdrawError');
    const withdrawConfirmCheckbox = document.getElementById('withdrawConfirmCheckbox');
    const withdrawTimerEl = document.getElementById('withdrawTimer');

    let withdrawTimer = null;

    function hideWithdrawMessages() {
        if (withdrawMessage) {
            withdrawMessage.style.display = 'none';
            withdrawMessage.textContent = '';
        }
        if (withdrawError) {
            withdrawError.style.display = 'none';
            withdrawError.textContent = '';
        }
    }

    // 체크박스 상태에 따라 탈퇴 버튼 활성/비활성
    function updateWithdrawButtonState() {
        if (!withdrawBtnBottom || !withdrawConfirmCheckbox) return;
        withdrawBtnBottom.disabled = !withdrawConfirmCheckbox.checked;
    }

    if (withdrawConfirmCheckbox) {
        withdrawConfirmCheckbox.addEventListener('change', updateWithdrawButtonState);
    }

    // 회원 탈퇴 인증번호 메일 요청
    async function requestWithdrawCode() {
        hideWithdrawMessages();

        if (withdrawMessage) {
            withdrawMessage.textContent = '인증번호 전송 중... 잠시만 기다려주세요.';
            withdrawMessage.style.display = 'block';
        }

        if (window.EmailCodeUtil && sendWithdrawCodeBtn) {
            EmailCodeUtil.setButtonLoading(sendWithdrawCodeBtn, true, '전송 중...');
        } else if (sendWithdrawCodeBtn) {
            sendWithdrawCodeBtn.disabled = true;
        }

        try {
            const res = await fetch('/api/member/withdraw/email-code', {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            const body = await res.json();

            if (body.success) {
                if (withdrawMessage) {
                    withdrawMessage.textContent = body.message || '인증번호를 이메일로 전송했습니다.';
                    withdrawMessage.style.display = 'block';
                }

                if (window.EmailCodeUtil && withdrawTimerEl) {
                    if (!withdrawTimer) {
                        withdrawTimer = EmailCodeUtil.createTimer(
                            withdrawTimerEl,
                            {
                                prefix: '인증번호 남은시간: ',
                                expiredText: '인증번호가 만료되었습니다. 다시 인증번호를 요청해 주세요.'
                            }
                        );
                    }
                    withdrawTimer.start(300);
                }
            } else {
                if (withdrawError) {
                    withdrawError.textContent = body.error || '인증번호 전송에 실패했습니다.';
                    withdrawError.style.display = 'block';
                }
            }
        } catch (err) {
            if (withdrawError) {
                withdrawError.textContent = '오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                withdrawError.style.display = 'block';
            }
        } finally {
            if (window.EmailCodeUtil && sendWithdrawCodeBtn) {
                EmailCodeUtil.setButtonLoading(sendWithdrawCodeBtn, false);
            } else if (sendWithdrawCodeBtn) {
                sendWithdrawCodeBtn.disabled = false;
            }
        }
    }

    // 실제 탈퇴 요청 AJAX
    async function doWithdrawRequest(code) {
        try {
            const res = await fetch('/api/member/withdraw', {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: new URLSearchParams({ code: code })
            });

            const body = await res.json();

            if (body.success) {
                showAppAlert(body.message || '회원탈퇴가 완료되었습니다.', function () {
                    window.location.href = "/login";
                });
            } else {
                if (withdrawError) {
                    withdrawError.textContent = body.error || '회원탈퇴에 실패했습니다.';
                    withdrawError.style.display = 'block';
                }
            }
        } catch (err) {
            if (withdrawError) {
                withdrawError.textContent = '오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                withdrawError.style.display = 'block';
            }
        }
    }

    // 회원 탈퇴 버튼 클릭 핸들러
    function requestWithdraw() {
        hideWithdrawMessages();

        const code = withdrawCodeInput ? withdrawCodeInput.value.trim() : "";

        if (!code) {
            if (withdrawError) {
                withdrawError.textContent = '이메일로 받은 인증번호를 입력해주세요.';
                withdrawError.style.display = 'block';
            }
            return;
        }

        if (!withdrawConfirmCheckbox || !withdrawConfirmCheckbox.checked) {
            if (withdrawError) {
                withdrawError.textContent = '탈퇴 안내 내용을 확인 후, 동의 체크박스를 선택해주세요.';
                withdrawError.style.display = 'block';
            }
            return;
        }

        showDangerConfirm(
            "정말로 회원탈퇴 하시겠습니까?\n작성한 게시글과 댓글, 회원 정보가 모두 삭제되며 복구할 수 없습니다.",
            function () {
                doWithdrawRequest(code);
            }
        );
    }

    if (sendWithdrawCodeBtn) {
        sendWithdrawCodeBtn.addEventListener('click', requestWithdrawCode);
    }
    if (withdrawBtnBottom) {
        withdrawBtnBottom.addEventListener('click', requestWithdraw);
    }

    // 초기 버튼 상태 설정
    updateWithdrawButtonState();

})();
