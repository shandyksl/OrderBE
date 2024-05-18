package com.example.OrderBE.controllers;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.models.requests.PlaceOrderRequest;
import com.example.OrderBE.models.responses.BaseApiResponse;
import com.example.OrderBE.utils.OrderIDGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private static final Logger logger = Logger.getLogger(ServiceController.class.getName());

    private final RocketMQTemplate rocketMQTemplate;
    private final OrderIDGenerator orderIDGenerator;


@Autowired
public ServiceController(RocketMQTemplate rocketMQTemplate, OrderIDGenerator orderIDGenerator) {
    this.rocketMQTemplate = rocketMQTemplate;
    this.orderIDGenerator = orderIDGenerator;

}

    @PostMapping("/placeorder")
    public BaseApiResponse placeOrder(@RequestBody PlaceOrderRequest requestbody) throws ErrorCodeException {

        List<SalesOrder> salesOrders = requestbody.getSalesOrders();
        String orderId = orderIDGenerator.generateUniqueOrderId();
        for(SalesOrder salesOrder : salesOrders) {
            salesOrder.setOrderId(orderId);
        }
        rocketMQTemplate.convertAndSend("placeorder", salesOrders);
        logger.info("Order received and sent to RocketMQ");

        return new BaseApiResponse("Order received and sent to RocketMQ");
    }
}
