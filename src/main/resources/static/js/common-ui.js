// common-ui.js
(function () {

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

    // 전역으로 노출
    window.showAppAlert = showAppAlert;
    window.showDangerConfirm = showDangerConfirm;

    // 필요하면 네임스페이스로도 사용 가능
    window.CommonUI = {
        showAppAlert: showAppAlert,
        showDangerConfirm: showDangerConfirm
    };

})();
