package com.example.TODAIT__BE.global.apiPayload.handler;

import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Comparator;

@RestControllerAdvice
@Slf4j
public class GeneralExceptionAdvice {

    // 프로젝트에서 발생한 예외 처리
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberException(
            ProjectException e
    ) {
        BaseErrorCode errorCode = e.getErrorCode();
        if (e.getCause() != null) {
            log.warn(
                    "프로젝트 예외가 원인 예외와 함께 발생했습니다. code={}",
                    errorCode.getCode(),
                    e
            );
        }
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        String message = e.getBindingResult().getAllErrors().stream()
                .sorted(Comparator.comparingInt(this::validationMessagePriority))
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(this::hasText)
                .findFirst()
                .orElse(code.getMessage());

        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, message, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        String message = e.getConstraintViolations().stream()
                .sorted(Comparator.comparingInt(this::validationMessagePriority))
                .map(ConstraintViolation::getMessage)
                .filter(this::hasText)
                .findFirst()
                .orElse(code.getMessage());

        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, message, null));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e
    ) {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        String message = e.getAllErrors().stream()
                .sorted(Comparator.comparingInt(this::validationMessagePriority))
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(this::hasText)
                .findFirst()
                .orElse(code.getMessage());

        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, message, null));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException() {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }

    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex
    ) {
        log.error("처리되지 않은 서버 예외가 발생했습니다.", ex);

        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int validationMessagePriority(MessageSourceResolvable error) {
        String[] codes = error.getCodes();
        String message = error.getDefaultMessage();
        if (hasCode(codes, "NotBlank") || contains(message, "필수")) {
            return 0;
        }
        if (hasCode(codes, "Size") || contains(message, "이상") || contains(message, "이하")) {
            return 1;
        }
        if (hasCode(codes, "Pattern") || contains(message, "사용할 수 있습니다")) {
            return 2;
        }

        return 3;
    }

    private int validationMessagePriority(ConstraintViolation<?> violation) {
        String constraintName = violation.getConstraintDescriptor()
                .getAnnotation()
                .annotationType()
                .getSimpleName();
        String message = violation.getMessage();
        if ("NotBlank".equals(constraintName) || contains(message, "필수")) {
            return 0;
        }
        if ("Size".equals(constraintName) || contains(message, "이상") || contains(message, "이하")) {
            return 1;
        }
        if ("Pattern".equals(constraintName) || contains(message, "사용할 수 있습니다")) {
            return 2;
        }

        return 3;
    }

    private boolean hasCode(String[] codes, String constraintName) {
        if (codes == null) {
            return false;
        }
        for (String code : codes) {
            if (code.equals(constraintName) || code.startsWith(constraintName + ".")) {
                return true;
            }
        }

        return false;
    }

    private boolean contains(String value, String pattern) {
        return value != null && value.contains(pattern);
    }
}
