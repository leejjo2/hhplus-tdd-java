package io.hhplus.tdd;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 REST 컨트롤러에 대한 예외 처리를 담당하는 어드바이스 클래스입니다.
 * 이 클래스는 @RestControllerAdvice 어노테이션을 사용하여
 * 컨트롤러에서 발생하는 예외를 처리할 수 있도록 합니다.
 */
@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * 모든 예외를 처리하는 핸들러 메서드입니다.
     * @param e 발생한 예외
     * @return 500 상태 코드와 함께 에러 메시지를 담은 ErrorResponse 객체를 반환합니다.
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", e.getMessage()));
    }

    /**
     * IllegalArgumentException을 처리하는 핸들러 메서드입니다.
     * @param e 발생한 IllegalArgumentException
     * @return 400 상태 코드와 함께 에러 메시지를 담은 ErrorResponse 객체를 반환합니다.
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }
}
