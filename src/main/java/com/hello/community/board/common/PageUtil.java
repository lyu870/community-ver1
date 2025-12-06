// PageUtil.java
package com.hello.community.board.common;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class PageUtil {
    // 게시판 페이지 계산
    public static Map<String, Integer> buildPageModel(Page<?> page, int pageLinks) {

        int current = page.getNumber() + 1;
        int totalPages = Math.max(page.getTotalPages(), 1);

        int half = pageLinks / 2;
        int startPage = Math.max(1, current - half);
        int endPage = Math.min(totalPages, startPage + pageLinks - 1);

        if (endPage - startPage + 1 < pageLinks) {
            startPage = Math.max(1, endPage - pageLinks + 1);
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("current", current);
        result.put("totalPages", totalPages);
        result.put("startPage", startPage);
        result.put("endPage", endPage);

        return result;
    }
}
