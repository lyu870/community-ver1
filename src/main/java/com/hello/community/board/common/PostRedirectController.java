// PostRedirectController.java
package com.hello.community.board.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PostRedirectController {

    private final PostFinder postFinder;

    @GetMapping("/post/{id}")
    public String redirectToDetail(@PathVariable Long id, HttpServletRequest request) {

        String detailUrl = postFinder.buildDetailUrl(id);
        String qs = request.getQueryString();

        if (qs == null || qs.isBlank()) {
            return "redirect:" + detailUrl;
        }

        if (detailUrl.contains("?")) {
            return "redirect:" + detailUrl + "&" + qs;
        }

        return "redirect:" + detailUrl + "?" + qs;
    }
}
