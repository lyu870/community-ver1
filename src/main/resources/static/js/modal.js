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

    function setVariant(variant) {
        if (!dialog) return;

        dialog.classList.remove(
            'modal-dialog--danger',
            'modal-dialog--info',
            'modal-dialog--warning',
            'modal-dialog--success'
        );

        if (!variant) {
            return;
        }

        if (variant === 'danger') {
            dialog.classList.add('modal-dialog--danger');
        } else if (variant === 'info') {
            dialog.classList.add('modal-dialog--info');
        } else if (variant === 'warning') {
            dialog.classList.add('modal-dialog--warning');
        } else if (variant === 'success') {
            dialog.classList.add('modal-dialog--success');
        }
    }

    // 기존 코드 호환용
    function setDanger(isDanger) {
        setVariant(isDanger ? 'danger' : null);
    }

    function closeModal() {
        overlay.style.display = 'none';
        okHandler = null;
        cancelHandler = null;
        setVariant(null);
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
            setVariant(null);
            overlay.style.display = 'flex';
        },

        // 확인/취소 선택 모달
        //  danger: true, variant: 'info' | 'warning'|  'success' 등
        confirm: function (text, onOk, onCancel, options) {
            messageEl.textContent = text || '';
            cancelBtn.style.display = 'inline-flex';
            okHandler = typeof onOk === 'function' ? onOk : null;
            cancelHandler = typeof onCancel === 'function' ? onCancel : null;

            const variantFromOption = options && options.variant ? options.variant : null;
            const isDanger = options && options.danger === true;
            const finalVariant = variantFromOption || (isDanger ? 'danger' : null);

            if (finalVariant) {
                setVariant(finalVariant);
            } else {
                setVariant(null);
            }

            overlay.style.display = 'flex';
        },

        // 필요 시 강제 닫기
        close: closeModal
    };

})();
