// csrf-util.js
(function () {

    function getMeta(name) {
        const meta = document.querySelector('meta[name="' + name + '"]');
        if (!meta) {
            return '';
        }
        return meta.getAttribute('content') || '';
    }

    function apply(headers) {
        if (!headers) {
            return;
        }

        const token = getMeta('_csrf');
        const headerName = getMeta('_csrf_header');

        if (!token || !headerName) {
            return;
        }

        headers[headerName] = token;
    }

    window.CsrfUtil = window.CsrfUtil || {};
    window.CsrfUtil.apply = apply;

})();
