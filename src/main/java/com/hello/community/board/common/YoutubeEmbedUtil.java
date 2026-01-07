// YouTubeEmbedUtil.java
package com.hello.community.board.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YouTubeEmbedUtil {

    private static final Pattern YOUTUBE_ID_PATTERN_1 =
            Pattern.compile("(?i)(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/watch\\?[^\\s]*v=([a-zA-Z0-9_-]{6,})");

    private static final Pattern YOUTUBE_ID_PATTERN_2 =
            Pattern.compile("(?i)(?:https?://)?(?:www\\.|m\\.)?youtu\\.be/([a-zA-Z0-9_-]{6,})");

    private static final Pattern YOUTUBE_ID_PATTERN_3 =
            Pattern.compile("(?i)(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/shorts/([a-zA-Z0-9_-]{6,})");

    private static final Pattern YOUTUBE_ID_PATTERN_4 =
            Pattern.compile("(?i)(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/embed/([a-zA-Z0-9_-]{6,})");

    private static final Pattern YOUTUBE_URL_PATTERN =
            Pattern.compile("(?i)(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?[^\\s]*v=|shorts/|embed/)|youtu\\.be/)[a-zA-Z0-9_-]{6,}[^\\s]*");

    private static final Pattern URL_PATTERN =
            Pattern.compile("(?i)\\b(https?://[^\\s<]+|www\\.[^\\s<]+)");

    private YouTubeEmbedUtil() {
    }

    // 게시글 본문에서 유튜브 링크가 있으면 embed URL을 만들어 반환
    public static String extractEmbedUrl(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String videoId = extractVideoId(text);
        if (videoId == null) {
            return null;
        }

        return "https://www.youtube-nocookie.com/embed/" + videoId;
    }

    // 게시글 본문을 화면용 HTML로 변환 (유튜브 링크 제거 + 줄바꿈 유지 + 일반 링크 클릭)
    public static String renderContentHtml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = normalizeNewlines(text);
        String stripped = stripYouTubeUrls(normalized);

        return linkifyAndPreserveBreaks(stripped);
    }

    private static String extractVideoId(String text) {
        String normalized = normalizeNewlines(text);

        String id = findFirstGroup(normalized, YOUTUBE_ID_PATTERN_1);
        if (id != null) {
            return id;
        }

        id = findFirstGroup(normalized, YOUTUBE_ID_PATTERN_2);
        if (id != null) {
            return id;
        }

        id = findFirstGroup(normalized, YOUTUBE_ID_PATTERN_3);
        if (id != null) {
            return id;
        }

        return findFirstGroup(normalized, YOUTUBE_ID_PATTERN_4);
    }

    private static String findFirstGroup(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private static String normalizeNewlines(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    private static String stripYouTubeUrls(String text) {
        String removed = YOUTUBE_URL_PATTERN.matcher(text).replaceAll("");
        return cleanupDanglingWhitespace(removed);
    }

    private static String cleanupDanglingWhitespace(String text) {
        String trimmedLines = text.replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n[ \\t]+", "\n");
        return trimmedLines.trim();
    }

    private static String linkifyAndPreserveBreaks(String text) {
        String escaped = escapeHtml(text);

        Matcher matcher = URL_PATTERN.matcher(escaped);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String rawUrl = matcher.group(1);

            String trimmedUrl = trimUrlSuffix(rawUrl);
            String suffix = rawUrl.substring(trimmedUrl.length());

            String href = trimmedUrl;
            if (href.startsWith("www.")) {
                href = "http://" + href;
            }

            String replacement =
                    "<a href=\"" + href + "\" target=\"_blank\" rel=\"noopener noreferrer\">" +
                            trimmedUrl +
                            "</a>" + suffix;

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);

        return sb.toString().replace("\n", "<br>");
    }

    private static String trimUrlSuffix(String url) {
        int end = url.length();

        while (end > 0) {
            char ch = url.charAt(end - 1);
            if (ch == '.' || ch == ',' || ch == ';' || ch == ':' || ch == '!' || ch == '?' ||
                    ch == ')' || ch == ']' || ch == '}' || ch == '"' || ch == '\'') {
                end--;
                continue;
            }
            break;
        }

        if (end <= 0) {
            return "";
        }

        return url.substring(0, end);
    }

    private static String escapeHtml(String text) {
        StringBuilder sb = new StringBuilder(text.length() + 32);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }

        return sb.toString();
    }
}
