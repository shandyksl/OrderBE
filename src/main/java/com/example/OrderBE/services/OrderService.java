package com.example.OrderBE.services;

import com.example.OrderBE.models.entities.SalesOrder;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public interface OrderService {

    void placeOrder(List<MessageExt> messageExts);

    List<SalesOrder> getSalesOrderDetails(String orderId);

    void orderPayment(String orderId);
}
