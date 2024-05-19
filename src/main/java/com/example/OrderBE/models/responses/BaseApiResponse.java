package com.example.OrderBE.models.responses;

import com.example.OrderBE.aop.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseApiResponse {

    @JsonProperty("status")
    private Integer statusCode;

    // 成功参数
    private Object data;

    // 失败参数
    private String statusID;
    private String description;

    // 成功
    public BaseApiResponse(Object data) {
        this.data = data;
        this.statusCode = ErrorCode.OK.getStatus();
    }

    // 失败
    public BaseApiResponse(ErrorCode errorCode) {
        this.statusID = errorCode.name();
        this.statusCode = errorCode.getStatus();
        this.description = errorCode.getMessage();
    }
}
