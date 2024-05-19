package com.example.OrderBE.aop.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    OK(100, "OK"),
    FORBIDDEN(403, "NOT FORBIDDEN"),
    NOT_FOUND(404, "NOT FOUND"),
    SERVER_ERR(500, "SERVER ERROR"),
    VALIDATION_ERR(422, "VALIDATION ERROR"),
    BIZ_ERR(422, "BUSINESS LOGIC ERROR"),
    AUTHORIZED_ERR(401, "AUTHORIZED ERROR"),
    RMSERVICE_ERR(503, "INTERFACE SERVICE ERROR"),
    MISS_PARAM(400, "MISSING PARAMETER"),
    UN_LOGIN(-10001, "PLEASE LOGIN INTO SERVER"),

    QTY_EXCEEDED(100001, "QUANTITY EXCEEDED"),
    ORDERID_NOT_EXIST(100002, "ORDER ID NOT EXIST"),

    // 请注意!! VALIDATION PURPOSE 请用2开头 例子 200001
    VALIDATE_BLANK_ORDERID(200001, "ORDER ID CANNOT BE BLANK"),
    VALIDATE_PRODUCT_ID(20002, "PRODUCT ID CANNOT BE BLANK"),
    VALIDATE_PRICE(20003, "PRICE CANNOT BE 0"),
    VALIDATE_TOTAL_PRICE(20004, "TOTAL PRICE CANNOT BE 0"),
    VALIDATE_QUANTITY(20005, "QUANTITY CANNOT BE 0"),

    EXCEPTION_THROWN(600, "UNKNOWN REPORT");
    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public String toJson() {
        return String.format("{\"statusID\":\"%s\", \"statusCode\":%d, \"description\":\"%s\"}",
                this.name(), this.getStatus(), this.getMessage());
    }

    public String exceptionJson(String desc) {
        return String.format("{\"statusID\":\"%s\", \"statusCode\":%d, \"description\":\"%s\"}",
                this.name(), this.getStatus(), desc);
    }
}
