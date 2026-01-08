// common-ui.js
(function () {

    var toastTimer = null;

    // 단순 alert (성공/정보/에러 공통)
    function showAppAlert(message, onOk) {
        var msg = message || "";
        if (window.AppModal && AppModal.alert) {
            AppModal.alert(msg, function () {
                if (typeof onOk === "function") {
                    onOk();
                }
            });
        } else {
            alert(msg);
            if (typeof onOk === "function") {
                onOk();
            }
        }
    }

    // 확인/취소 모달 (중립)
    function showAppConfirm(message, onOk, onCancel) {
        var msg = message || "";
        if (window.AppModal && AppModal.confirm) {
            AppModal.confirm(
                msg,
                function () {
                    if (typeof onOk === "function") {
                        onOk();
                    }
                },
                function () {
                    if (typeof onCancel === "function") {
                        onCancel();
                    }
                },
                { danger: false }
            );
        } else {
            if (confirm(msg)) {
                if (typeof onOk === "function") {
                    onOk();
                }
            } else {
                if (typeof onCancel === "function") {
                    onCancel();
                }
            }
        }
    }

    // 정보용 확인 모달 (파란색)
    function showInfoConfirm(message, onOk, onCancel) {
        var msg = message || "";
        if (window.AppModal && AppModal.confirm) {
            AppModal.confirm(
                msg,
                function () {
                    if (typeof onOk === "function") {
                        onOk();
                    }
                },
                function () {
                    if (typeof onCancel === "function") {
                        onCancel();
                    }
                },
                { variant: "info" }
            );
        } else {
            if (confirm(msg)) {
                if (typeof onOk === "function") {
                    onOk();
                }
            } else {
                if (typeof onCancel === "function") {
                    onCancel();
                }
            }
        }
    }

    // 주의용 확인 모달 (노랑)
    function showWarningConfirm(message, onOk, onCancel) {
        var msg = message || "";
        if (window.AppModal && AppModal.confirm) {
            AppModal.confirm(
                msg,
                function () {
                    if (typeof onOk === "function") {
                        onOk();
                    }
                },
                function () {
                    if (typeof onCancel === "function") {
                        onCancel();
                    }
                },
                { variant: "warning" }
            );
        } else {
            if (confirm(msg)) {
                if (typeof onOk === "function") {
                    onOk();
                }
            } else {
                if (typeof onCancel === "function") {
                    onCancel();
                }
            }
        }
    }

    // 위험 액션용 확인 모달
    function showDangerConfirm(message, onOk) {
        var msg = message || "";
        if (window.AppModal && AppModal.confirm) {
            AppModal.confirm(
                msg,
                function () {
                    if (typeof onOk === "function") {
                        onOk();
                    }
                },
                null,
                { danger: true }
            );
        } else {
            if (confirm(msg)) {
                if (typeof onOk === "function") {
                    onOk();
                }
            }
        }
    }

    // 상단 토스트
    function showAppToast(message, options) {
        var msg = message || "";
        var el = document.getElementById('appToast');

        if (!el) {
            alert(msg);
            return;
        }

        var variant = options && options.variant ? options.variant : 'info';
        var bg = '#333333';

        if (variant === 'success') {
            bg = '#2e7d32';
        } else if (variant === 'warning') {
            bg = '#f9a825';
        } else if (variant === 'danger') {
            bg = '#d93025';
        } else if (variant === 'info') {
            bg = '#2c7efb';
        }

        el.textContent = msg;
        el.style.backgroundColor = bg;
        el.style.display = 'block';

        if (toastTimer) {
            clearTimeout(toastTimer);
            toastTimer = null;
        }

        var duration = options && typeof options.duration === 'number' ? options.duration : 2000;

        toastTimer = setTimeout(function () {
            el.style.display = 'none';
        }, duration);
    }

    // 저장된 토스트 표시 (페이지 이동 후에도 보이게)
    function showStoredToastOnLoad() {
        try {
            var raw = sessionStorage.getItem("APP_TOAST");
            if (!raw) {
                return;
            }

            sessionStorage.removeItem("APP_TOAST");

            var payload = null;
            try {
                payload = JSON.parse(raw);
            } catch (e) {
                payload = { message: raw };
            }

            if (!payload || !payload.message) {
                return;
            }

            var opts = payload.options || {
                variant: payload.variant,
                duration: payload.duration
            };

            showAppToast(payload.message, opts);
        } catch (e) {
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        showStoredToastOnLoad();
    });

    // 전역으로 노출
    window.showAppAlert = showAppAlert;
    window.showAppConfirm = showAppConfirm;
    window.showInfoConfirm = showInfoConfirm;
    window.showWarningConfirm = showWarningConfirm;
    window.showDangerConfirm = showDangerConfirm;
    window.showAppToast = showAppToast;

    // 필요하면 네임스페이스로도 사용 가능
    window.CommonUI = {
        showAppAlert: showAppAlert,
        showAppConfirm: showAppConfirm,
        showInfoConfirm: showInfoConfirm,
        showWarningConfirm: showWarningConfirm,
        showDangerConfirm: showDangerConfirm,
        showAppToast: showAppToast
    };

})();
