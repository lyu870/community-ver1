// youtube-preview.js
(function () {

    function extractYoutubeVideoId(text) {
        if (!text) {
            return null;
        }

        var patterns = [
            /(?:https?:\/\/)?(?:www\.)?youtube\.com\/watch\?[^\s]*v=([a-zA-Z0-9_-]{6,})/i,
            /(?:https?:\/\/)?(?:www\.)?youtu\.be\/([a-zA-Z0-9_-]{6,})/i,
            /(?:https?:\/\/)?(?:www\.)?youtube\.com\/shorts\/([a-zA-Z0-9_-]{6,})/i,
            /(?:https?:\/\/)?(?:www\.)?youtube\.com\/embed\/([a-zA-Z0-9_-]{6,})/i
        ];

        for (var i = 0; i < patterns.length; i++) {
            var match = text.match(patterns[i]);
            if (match && match[1]) {
                return match[1];
            }
        }

        return null;
    }

    function buildEmbedUrl(videoId) {
        if (!videoId) {
            return null;
        }
        return "https://www.youtube-nocookie.com/embed/" + videoId;
    }

    function initYoutubePreview() {
        var textarea = document.getElementById("content");
        var wrap = document.getElementById("youtubePreviewWrap");
        var frame = document.getElementById("youtubePreviewFrame");

        if (!textarea || !wrap || !frame) {
            return;
        }

        var lastUrl = null;

        function hidePreview() {
            wrap.style.display = "none";
            frame.removeAttribute("src");
            lastUrl = null;
        }

        function showPreview(embedUrl) {
            wrap.style.display = "block";
            if (lastUrl !== embedUrl) {
                frame.setAttribute("src", embedUrl);
                lastUrl = embedUrl;
            }
        }

        function updatePreview() {
            var text = textarea.value || "";
            var videoId = extractYoutubeVideoId(text);
            var embedUrl = buildEmbedUrl(videoId);

            if (!embedUrl) {
                hidePreview();
                return;
            }

            showPreview(embedUrl);
        }

        textarea.addEventListener("input", updatePreview);
        updatePreview();
    }

    document.addEventListener("DOMContentLoaded", initYoutubePreview);
})();
