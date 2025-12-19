// PostRedirectController.java
package com.hello.community.board.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PostRedirectController {

    private final PostFinder postFinder;

    @GetMapping("/post/{id}")
    public String redirectToDetail(@PathVariable Long id) {
        return "redirect:" + postFinder.buildDetailUrl(id);
    }
}
