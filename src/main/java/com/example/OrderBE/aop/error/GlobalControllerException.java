package com.example.OrderBE.aop.error;

import com.example.OrderBE.models.responses.BaseApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalControllerException {

    //自定义抛掉异常
    @ExceptionHandler(value = ErrorCodeException.class)
    public ResponseEntity<BaseApiResponse> handleErrorCodeException(ErrorCodeException errorCodeException) {
        return ResponseEntity.ok().body(new BaseApiResponse(errorCodeException.getErrorCode()));
    }

    //数据库储存程序抛掉异常
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<String> handleJpaException(JpaSystemException ex) {
        //筛选出有用信息
        String msg = ex.getMostSpecificCause().getLocalizedMessage();

        return new ResponseEntity<>(ErrorCode.EXCEPTION_THROWN.exceptionJson(msg), HttpStatus.BAD_REQUEST);
    }

    //所有系统抛掉异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

}
