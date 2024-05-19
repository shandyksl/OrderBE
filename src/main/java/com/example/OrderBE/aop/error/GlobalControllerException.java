package com.example.OrderBE.aop.error;

import com.example.OrderBE.models.responses.BaseApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerException {

    //自定义抛掉异常
    @ExceptionHandler(value = ErrorCodeException.class)
    public ResponseEntity<BaseApiResponse> handleErrorCodeException(ErrorCodeException errorCodeException) {
        return ResponseEntity.ok().body(new BaseApiResponse(errorCodeException.getErrorCode()));
    }
}
