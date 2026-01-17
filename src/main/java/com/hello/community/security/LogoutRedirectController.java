// LogoutRedirectController.java
package com.hello.community.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutRedirectController {

    @GetMapping("/logout")
    public String logoutGet() {
        return "redirect:/";
    }
}
