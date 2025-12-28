// PageUtil.java
package com.hello.community.board.common;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class PageUtil {

    public static Map<String, Object> buildPageModel(Page<?> page, int pageBlock) {
        return buildPageModel(page, pageBlock, "");
    }

    public static Map<String, Object> buildPageModel(Page<?> page, int pageBlock, String prefix) {

        int totalPages = page.getTotalPages();
        if (totalPages < 1) {
            totalPages = 1;
        }

        int current = page.getNumber() + 1;
        if (current < 1) current = 1;
        if (current > totalPages) current = totalPages;

        int startPage = ((current - 1) / pageBlock) * pageBlock + 1;
        int endPage = startPage + pageBlock - 1;
        if (endPage > totalPages) endPage = totalPages;

        Map<String, Object> model = new HashMap<>();
        if (prefix == null) {
            prefix = "";
        }

        if (prefix.isEmpty()) {
            model.put("current", current);
            model.put("totalPages", totalPages);
            model.put("startPage", startPage);
            model.put("endPage", endPage);
        } else {
            model.put(prefix + "Current", current);
            model.put(prefix + "TotalPages", totalPages);
            model.put(prefix + "StartPage", startPage);
            model.put(prefix + "EndPage", endPage);
        }

        return model;
    }

}
