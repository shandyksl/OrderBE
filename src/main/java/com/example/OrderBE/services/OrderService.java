package com.example.OrderBE.services;

import com.example.OrderBE.models.entities.SalesOrder;

import java.util.List;

public interface OrderService {

    List<SalesOrder> getSalesOrderDetails(String orderId);

    void orderPayment(String orderId);
}
