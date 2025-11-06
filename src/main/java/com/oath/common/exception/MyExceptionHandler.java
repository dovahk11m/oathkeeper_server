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
        // 혹시나 400 에러 핸들러가 의도와 다르게 안걸렸을 경우
        Exception400 ex400 = findCause(e.getCause(), Exception400.class);

        if (ex400 != null) {
            String message = ex400.getMessage();
            log.warn("[400 Bad Request] (JSON Parse Error) : {}", message);

            return new ResponseEntity<>(
                    CommonResponse.error(message),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 이후로는 진짜 예상하지 못한 기타 예외 던짐
        log.error(
                "!!! 예상치 못한 런타임 에러 발생 !!!",
                e
        );
        return new ResponseEntity<>(
                CommonResponse.error("시스템 오류가 발생했습니다. 관리자에게 문의해주세요."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * 예외 체인(Cause chain)을 재귀적으로 탐색하여
     * 특정 타입의 예외가 있는지 찾아 반환합니다.
     *
     * @param throwable  최상위 예외
     * @param targetType 찾고 싶은 예외의 클래스
     * @return 찾은 예외 (없으면 null)
     */
    @SuppressWarnings("unchecked")
    private <T extends Throwable> T findCause(Throwable throwable, Class<T> targetType) {
        Throwable cause = throwable;

        // 1. 현재 예외가 null이 아닐 때까지 계속 반복
        while (cause != null) {

            // 2. 현재 예외가 내가 찾는 타입(targetType)과 일치하는지 확인
            if (targetType.isInstance(cause)) {
                return (T) cause;
            }

            // 3. (안전장치) 더 이상 하위 예외가 없으면 멈춤
            if (cause.getCause() == null) {
                break;
            }

            // 4. (안전장치) 무한 루프 방지 (A가 B를, B가 A를 원인으로 삼는 이상한 경우)
            if (cause == cause.getCause()) {
                break;
            }

            // 5. 다음 하위 예외로 이동
            cause = cause.getCause();
        }

        // 6. 끝까지 못 찾으면 null 반환
        return null;
    }
}
