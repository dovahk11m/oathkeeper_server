package com.oath.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MyExceptionHandler.class);

    //공통 로깅 로직
    private void logErrorDetails(Exception e, HttpServletRequest request, String errorType) {
        log.warn("=== {} 에러 발생 ===", errorType);
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("오류 메시지: {}", e.getMessage());
    }

    //400 Bad Request
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "400 Bad Request");
        model.addAttribute("msg", e.getMessage());
        return "err/400";
    }

    //401 Unauthorized
    @ExceptionHandler(Exception401.class)
    public String ex401(Exception401 e, HttpServletRequest request, HttpSession session, Model model) {
        logErrorDetails(e, request, "401 Unauthorized");
        model.addAttribute("msg", e.getMessage());
        session.invalidate();
        return "redirect:/login-form";
    }

    //403 Forbidden
    @ExceptionHandler(Exception403.class)
    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "403 Forbidden");
        model.addAttribute("msg", e.getMessage());
        return "err/403";
    }

    //404 Not Found
    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "404 Not Found");
        model.addAttribute("msg", e.getMessage());
        return "err/404";
    }

    //500 Internal Server Error
    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "500 Internal Server Error");
        model.addAttribute("msg", e.getMessage());
        return "err/500";
    }

    //기타 RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, HttpServletRequest request, Model model) {
        log.error("!!! 예상치 못한 런타임 에러 발생 !!!", e);
        model.addAttribute("msg", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");
        return "err/500";
    }
}
