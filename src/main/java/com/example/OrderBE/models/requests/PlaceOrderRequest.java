package com.example.OrderBE.models.requests;
import com.example.OrderBE.aop.error.ErrorCode;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.SalesOrder;
import com.mysql.cj.util.StringUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PlaceOrderRequest {
    private List<SalesOrder> salesOrders;

    public void validate() throws ErrorCodeException {

       for (SalesOrder salesOrder : salesOrders) {
           if(StringUtils.isNullOrEmpty(salesOrder.getProductId())){
             throw new ErrorCodeException(ErrorCode.VALIDATE_PRODUCT_ID);
           }

           BigDecimal price = salesOrder.getPrice();
           if( price == null || price.compareTo(BigDecimal.ZERO) == 0){
               throw new ErrorCodeException(ErrorCode.VALIDATE_PRICE);
           }

           BigDecimal totalPrice = salesOrder.getTotalPrice();
           if(totalPrice == null ||totalPrice.compareTo(BigDecimal.ZERO) == 0){
               throw new ErrorCodeException(ErrorCode.VALIDATE_TOTAL_PRICE);
           }

           if(salesOrder.getQuantity() == 0){
               throw new ErrorCodeException(ErrorCode.VALIDATE_QUANTITY);
           }
       }
    }
}
