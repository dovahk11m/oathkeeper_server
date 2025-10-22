package com.oath.common.exception;

import com.oath.common.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private boolean isJsonRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String xrw = request.getHeader("X-Requested-With");
        return (accept != null && accept.contains("application/json")) || (xrw != null && xrw.equalsIgnoreCase("XMLHttpRequest"));
    }

    //400 Bad Request
    @ExceptionHandler(Exception400.class)
    public Object ex400(Exception400 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "400 Bad Request");
        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(e.getMessage()));
        }
        model.addAttribute("msg", e.getMessage());
        return "err/400";
    }

    //401 Unauthorized
    @ExceptionHandler(Exception401.class)
    public Object ex401(Exception401 e, HttpServletRequest request, HttpSession session, Model model) {
        logErrorDetails(e, request, "401 Unauthorized");
        if (isJsonRequest(request)) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponse.error(e.getMessage()));
        }
        model.addAttribute("msg", e.getMessage());
        session.invalidate();
        return "redirect:/login-form";
    }

    //403 Forbidden
    @ExceptionHandler(Exception403.class)
    public Object ex403(Exception403 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "403 Forbidden");
        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponse.error(e.getMessage()));
        }
        model.addAttribute("msg", e.getMessage());
        return "err/403";
    }

    //404 Not Found
    @ExceptionHandler(Exception404.class)
    public Object ex404(Exception404 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "404 Not Found");
        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.error(e.getMessage()));
        }
        model.addAttribute("msg", e.getMessage());
        return "err/404";
    }

    //500 Internal Server Error
    @ExceptionHandler(Exception500.class)
    public Object ex500(Exception500 e, HttpServletRequest request, Model model) {
        logErrorDetails(e, request, "500 Internal Server Error");
        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error(e.getMessage()));
        }
        model.addAttribute("msg", e.getMessage());
        return "err/500";
    }

    //기타 RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request, Model model) {
        log.error("!!! 예상치 못한 런타임 에러 발생 !!!", e);
        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error("시스템 오류가 발생했습니다. 관리자에게 문의해주세요."));
        }
        model.addAttribute("msg", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");
        return "err/500";
    }
}
