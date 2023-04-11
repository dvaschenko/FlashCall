package com.example.FlashCall.exception;

import com.example.FlashCall.response.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value
            = { IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict() {
        return ResponseHandler.generateResponse("Для начала работы требуется авторизоваться в Plusofon!", HttpStatus.UNAUTHORIZED, null);
    }
}
