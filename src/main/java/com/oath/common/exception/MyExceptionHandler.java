package com.oath.common.exception;

import com.oath.common.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MyExceptionHandler.class);

    //공통 로깅 로직
    private void logErrorDetails(
            Exception e,
            HttpServletRequest request,
            String errorType
    ) {
        log.warn(
                "=== {} 에러 발생 ===",
                errorType
        );
        log.warn(
                "요청 URL : {}",
                request.getRequestURL()
        );
        log.warn(
                "오류 메시지: {}",
                e.getMessage()
        );
    }

    //400 Bad Request
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<CommonResponse<?>> ex400(
            Exception400 e,
            HttpServletRequest request
    ) {
        logErrorDetails(
                e,
                request,
                "400 Bad Request"
        );
        return new ResponseEntity<>(
                CommonResponse.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    //401 Unauthorized
    @ExceptionHandler(Exception401.class)
    public ResponseEntity<CommonResponse<?>> ex401(
            Exception401 e,
            HttpServletRequest request
    ) {
        logErrorDetails(
                e,
                request,
                "401 Unauthorized"
        );
        return new ResponseEntity<>(
                CommonResponse.error(e.getMessage()),
                HttpStatus.UNAUTHORIZED
        );
    }

    //403 Forbidden
    @ExceptionHandler(Exception403.class)
    public ResponseEntity<CommonResponse<?>> ex403(
            Exception403 e,
            HttpServletRequest request
    ) {
        logErrorDetails(
                e,
                request,
                "403 Forbidden"
        );
        return new ResponseEntity<>(
                CommonResponse.error(e.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    //404 Not Found
    @ExceptionHandler(Exception404.class)
    public ResponseEntity<CommonResponse<?>> ex404(
            Exception404 e,
            HttpServletRequest request
    ) {
        logErrorDetails(
                e,
                request,
                "404 Not Found"
        );
        return new ResponseEntity<>(
                CommonResponse.error(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    //409 Conflict
    @ExceptionHandler(Exception409.class)
    public ResponseEntity<CommonResponse<?>> ex409(
            Exception409 e,
            HttpServletRequest request
    ) {
        logErrorDetails(
                e,
                request,
                "409 Conflict"
        );
        return new ResponseEntity<>(
                CommonResponse.error(e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    //500 Internal Server Error
    @ExceptionHandler(Exception500.class)
    public ResponseEntity<CommonResponse<?>> ex500(
            Exception500 e,
            HttpServletRequest request
    ) {
        logErrorDetails(
                e,
                request,
                "500 Internal Server Error"
        );
        return new ResponseEntity<>(
                CommonResponse.error(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    //기타 RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<?>> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request
    ) {
        log.error(
                "!!! 예상치 못한 런타임 에러 발생 !!!",
                e
        );
        return new ResponseEntity<>(
                CommonResponse.error("시스템 오류가 발생했습니다. 관리자에게 문의해주세요."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
