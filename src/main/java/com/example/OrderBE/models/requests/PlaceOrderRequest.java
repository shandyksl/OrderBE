package com.example.OrderBE.models.requests;

import com.example.OrderBE.models.entities.ProductInfo;
import com.example.OrderBE.models.entities.SalesOrder;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {
    private List<SalesOrder> salesOrders;
}
