package com.example.OrderBE.models.requests;
import com.example.OrderBE.aop.error.ErrorCode;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.mysql.cj.util.StringUtils;
import lombok.Data;

@Data
public class GetSalesOrderRequest {

    private String orderId;

    public void validate() throws ErrorCodeException {

        if(StringUtils.isNullOrEmpty(orderId)) {
            throw new ErrorCodeException(ErrorCode.VALIDATE_BLANK_ORDERID);
        }
    }
}
