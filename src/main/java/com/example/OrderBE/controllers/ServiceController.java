package com.example.OrderBE.controllers;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.models.requests.PlaceOrderRequest;
import com.example.OrderBE.models.responses.BaseApiResponse;
import com.example.OrderBE.utils.OrderIDGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private static final Logger logger = Logger.getLogger(ServiceController.class.getName());

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private OrderIDGenerator orderIDGenerator;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @PostMapping("/placeorder")
    public BaseApiResponse placeOrder(@RequestBody PlaceOrderRequest requestbody) throws ErrorCodeException {

        List<SalesOrder> salesOrders = requestbody.getSalesOrders();
        List<String> errorMessages = new ArrayList<>();

        for (SalesOrder salesOrder : salesOrders) {
            // Retrieve stock quantity from Redis
            Integer stockQuantity = Integer.valueOf((String) redisTemplate.opsForHash().get(salesOrder.getProductId(), "quantity"));
            String productName = (String) redisTemplate.opsForHash().get(salesOrder.getProductId(), "productName");

            // Check if stock quantity is sufficient
            if (stockQuantity == null || stockQuantity < salesOrder.getQuantity()) {
                String errorMessage = String.format("Insufficient stock for product %s (ID: %s). Requested: %d, Available: %d",
                        productName, salesOrder.getProductId(), salesOrder.getQuantity(), stockQuantity == null ? 0 : stockQuantity);
                errorMessages.add(errorMessage);
            }
        }

        // Return error is stock quantity is not enough
        if (!errorMessages.isEmpty()) {
            return new BaseApiResponse(String.join("; ", errorMessages));
        }

        String orderId = orderIDGenerator.generateUniqueOrderId();
        for (SalesOrder salesOrder : salesOrders) {
            salesOrder.setOrderId(orderId);
        }
        rocketMQTemplate.convertAndSend("placeorder", salesOrders);
        logger.info("Order received and sent to RocketMQ");

        return new BaseApiResponse("Order received and sent to RocketMQ");
    }
}
