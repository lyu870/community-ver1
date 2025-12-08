// emailCodeUtil.js
(function () {

    function CodeTimer(timerEl, options) {
        this.timerEl = timerEl;
        this.intervalId = null;
        this.remaining = 0;
        this.prefix = options && options.prefix ? options.prefix : '남은시간: ';
        this.expiredText = options && options.expiredText ? options.expiredText : '❌ 인증번호가 만료되었습니다.';
    }

    CodeTimer.prototype.start = function (seconds) {
        if (!this.timerEl) return;

        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }

        this.remaining = seconds;
        this.timerEl.style.display = 'block';

        var self = this;

        function tick() {
            if (self.remaining <= 0) {
                clearInterval(self.intervalId);
                self.intervalId = null;
                self.timerEl.textContent = self.expiredText;
                return;
            }

            var m = String(Math.floor(self.remaining / 60)).padStart(2, '0');
            var s = String(self.remaining % 60).padStart(2, '0');
            self.timerEl.textContent = self.prefix + m + ':' + s;
            self.remaining--;
        }

        tick();
        this.intervalId = setInterval(tick, 1000);
    };

    CodeTimer.prototype.clear = function () {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
        if (this.timerEl) {
            this.timerEl.textContent = '';
            this.timerEl.style.display = 'none';
        }
    };

    function setButtonLoading(button, isLoading, loadingText) {
        if (!button) return;

        if (isLoading) {
            if (!button.dataset.originalText) {
                button.dataset.originalText = button.textContent;
            }
            button.disabled = true;
            button.classList.add('btn-loading');
            if (loadingText) {
                button.textContent = loadingText;
            }
        } else {
            button.disabled = false;
            button.classList.remove('btn-loading');
            if (button.dataset.originalText) {
                button.textContent = button.dataset.originalText;
            }
        }
    }

    window.EmailCodeUtil = {
        createTimer: function (timerEl, options) {
            return new CodeTimer(timerEl, options || {});
        },
        setButtonLoading: setButtonLoading
    };

})();
