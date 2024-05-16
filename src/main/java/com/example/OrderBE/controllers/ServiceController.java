package com.example.OrderBE.controllers;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.models.requests.PlaceOrderRequest;
import com.example.OrderBE.services.OrderService;
import com.example.OrderBE.utils.OrderIDGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private final RocketMQTemplate rocketMQTemplate;
    private final OrderIDGenerator orderIDGenerator;

@Autowired
public ServiceController(RocketMQTemplate rocketMQTemplate, OrderIDGenerator orderIDGenerator) {
    this.rocketMQTemplate = rocketMQTemplate;
    this.orderIDGenerator = orderIDGenerator;
}

    @PostMapping("/placeorder")
    public ResponseEntity<String> placeOrder(@RequestBody PlaceOrderRequest requestbody) {
        List<SalesOrder> salesOrders = requestbody.getSalesOrders();

        for(SalesOrder salesOrder : salesOrders) {
            String orderId = orderIDGenerator.generateUniqueOrderId();
            salesOrder.setOrderId(orderId);
        }
        rocketMQTemplate.convertAndSend("order-topic", salesOrders);
        return ResponseEntity.ok("Order received and sent to RocketMQ");
    }
}
