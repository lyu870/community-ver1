// modal.js
(function () {

    const overlay = document.getElementById('appModal');
    const messageEl = document.getElementById('appModalMessage');
    const okBtn = document.getElementById('appModalOk');
    const cancelBtn = document.getElementById('appModalCancel');
    const dialog = overlay ? overlay.querySelector('.modal-dialog') : null;

    // 모달 DOM 이 없으면 아무 것도 하지 않음
    if (!overlay || !messageEl || !okBtn || !cancelBtn || !dialog) {
        return;
    }

    let okHandler = null;
    let cancelHandler = null;

    function setDanger(isDanger) {
        if (!dialog) return;
        if (isDanger) dialog.classList.add('modal-dialog--danger');
        else dialog.classList.remove('modal-dialog--danger');
    }

    function closeModal() {
        overlay.style.display = 'none';
        okHandler = null;
        cancelHandler = null;
        setDanger(false);
    }

    okBtn.addEventListener('click', function () {
        if (typeof okHandler === 'function') {
            okHandler();
        }
        closeModal();
    });

    cancelBtn.addEventListener('click', function () {
        if (typeof cancelHandler === 'function') {
            cancelHandler();
        }
        closeModal();
    });

    // 배경 클릭 시 닫기
    overlay.addEventListener('click', function (e) {
        if (e.target === overlay) {
            closeModal();
        }
    });

    // 외부에서 쓰는 전역 유틸
    window.AppModal = {
        // 단순 alert
        alert: function (text, onOk) {
            messageEl.textContent = text || '';
            cancelBtn.style.display = 'none';
            okHandler = typeof onOk === 'function' ? onOk : null;
            cancelHandler = null;
            setDanger(false);
            overlay.style.display = 'flex';
        },

        // 확인/취소 선택 모달
        // options: { danger: true } 형태로 전달하면 빨간 상단 라인 표시
        confirm: function (text, onOk, onCancel, options) {
            messageEl.textContent = text || '';
            cancelBtn.style.display = 'inline-flex';
            okHandler = typeof onOk === 'function' ? onOk : null;
            cancelHandler = typeof onCancel === 'function' ? onCancel : null;

            const isDanger = options && options.danger === true;
            setDanger(isDanger);

            overlay.style.display = 'flex';
        },

        // 필요 시 강제 닫기
        close: closeModal
    };

})();
