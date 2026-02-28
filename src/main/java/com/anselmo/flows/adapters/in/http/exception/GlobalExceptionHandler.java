package com.anselmo.flows.adapters.in.http.exception;

import com.anselmo.flows.adapters.in.http.dto.FlowErrorDto;
import com.anselmo.flows.adapters.out.crypto.CryptoOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FlowErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.ok(new FlowErrorDto(
                MDC.get("correlationId"),
                "validation_error",
                errors
        ));
    }

    @ExceptionHandler(CryptoOperationException.class)
    public ResponseEntity<FlowErrorDto> handleCrypto(CryptoOperationException ex) {
        LOGGER.warn("crypto operation failed correlationId={}", MDC.get("correlationId"));
        return ResponseEntity.ok(new FlowErrorDto(
                MDC.get("correlationId"),
                "unable_to_process_encrypted_payload",
                Map.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FlowErrorDto> handleGeneric(Exception ex) {
        LOGGER.error("unexpected error while processing request correlationId={}", MDC.get("correlationId"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new FlowErrorDto(
                        MDC.get("correlationId"),
                        "internal_server_error",
                        Map.of()
                ));
    }
}
