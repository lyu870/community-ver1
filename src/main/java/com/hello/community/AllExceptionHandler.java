// AllExceptionHandler.java
package com.hello.community;

import com.hello.community.board.common.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class AllExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AllExceptionHandler.class);

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotFound(Exception e,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {

        log.warn("NOT_FOUND {} {}", request.getMethod(), request.getRequestURI(), e);

        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.fail("페이지를 찾을 수 없습니다."));
        }

        response.setStatus(404);

        model.addAttribute("status", 404);
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("message", "요청하신 페이지를 찾을 수 없습니다.");
        model.addAttribute("timestamp", LocalDateTime.now());

        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Model model) {

        log.warn("BAD_REQUEST(TypeMismatch) {} {}", request.getMethod(), request.getRequestURI(), e);

        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.fail("잘못된 요청입니다."));
        }

        response.setStatus(400);

        model.addAttribute("status", 400);
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("message", "잘못된 요청입니다. 입력값을 확인해주세요.");
        model.addAttribute("timestamp", LocalDateTime.now());

        return "error/400";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException e,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        Model model) {

        log.warn("BAD_REQUEST(IllegalArgument) {} {}", request.getMethod(), request.getRequestURI(), e);

        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.fail(e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다."));
        }

        response.setStatus(400);

        model.addAttribute("status", 400);
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("message", e.getMessage() != null ? e.getMessage() : "요청 처리 중 오류가 발생했습니다.");
        model.addAttribute("timestamp", LocalDateTime.now());

        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  Model model) {

        log.error("INTERNAL_SERVER_ERROR {} {}", request.getMethod(), request.getRequestURI(), e);

        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.fail("서버 오류가 발생했습니다."));
        }

        response.setStatus(500);

        model.addAttribute("status", 500);
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        model.addAttribute("timestamp", LocalDateTime.now());

        return "error/500";
    }

    // API 요청 감지(페이지 요청이면 에러 페이지로)
    private boolean isApiRequest(HttpServletRequest request) {
        Object errUriObj = request.getAttribute("jakarta.servlet.error.request_uri");
        String errUri = (errUriObj instanceof String) ? (String) errUriObj : null;

        String uri = (errUri != null && !errUri.isBlank()) ? errUri : request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            return true;
        }

        String xrw = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) {
            return true;
        }

        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

}
