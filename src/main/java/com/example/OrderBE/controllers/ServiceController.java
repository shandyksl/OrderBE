package com.example.OrderBE.controllers;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.models.requests.GetSalesOrderRequest;
import com.example.OrderBE.models.requests.PlaceOrderRequest;
import com.example.OrderBE.models.responses.BaseApiResponse;
import com.example.OrderBE.services.OrderService;
import com.example.OrderBE.utils.OrderIDGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/service")
public class ServiceController {

    private static final Logger logger = Logger.getLogger(ServiceController.class.getName());

    private final RocketMQTemplate rocketMQTemplate;

    private final OrderIDGenerator orderIDGenerator;

    private final RedisTemplate<String, Object> redisTemplate;

    private final OrderService orderService;

    @Autowired
    public ServiceController(RocketMQTemplate rocketMQTemplate, OrderIDGenerator orderIDGenerator, RedisTemplate<String, Object> redisTemplate, OrderService orderService) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.orderIDGenerator = orderIDGenerator;
        this.redisTemplate = redisTemplate;
        this.orderService = orderService;
    }

    @PostMapping("/placeorder")
    public BaseApiResponse placeOrder(@RequestBody PlaceOrderRequest requestbody) throws ErrorCodeException {

        requestbody.validate();

        List<SalesOrder> salesOrders = requestbody.getSalesOrders();
        List<String> errorMessages = new ArrayList<>();

        for (SalesOrder salesOrder : salesOrders) {
            // Check whether product key is existing in redis server
            Boolean hasKey = redisTemplate.opsForHash().hasKey(salesOrder.getProductId(), "quantity");
            if (!hasKey) {
                String errorMessage = String.format("Product with ID: %s not found in stock.", salesOrder.getProductId());
                return new BaseApiResponse(errorMessage);
            }

            // Retrieve stock quantity from Redis
            int stockQuantity = Integer.parseInt((String) redisTemplate.opsForHash().get(salesOrder.getProductId(), "quantity"));
            String productName = (String) redisTemplate.opsForHash().get(salesOrder.getProductId(), "productName");

            // Check if stock quantity is sufficient
            if (stockQuantity < salesOrder.getQuantity()) {
                String errorMessage = String.format("Insufficient stock for product %s (ID: %s). Requested: %d, Available: %d",
                        productName, salesOrder.getProductId(), salesOrder.getQuantity(), stockQuantity);
                errorMessages.add(errorMessage);
            }
        }

        // Return error if stock quantity is not enough
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

    @GetMapping("/getsalesorderdetails")
    public BaseApiResponse getSalesOrderDetails(@RequestBody GetSalesOrderRequest requestbody) throws ErrorCodeException {

        requestbody.validate();
        List<SalesOrder> salesOrderDetail = orderService.getSalesOrderDetails(requestbody.getOrderId());
        return new BaseApiResponse(salesOrderDetail);
    }

    @PostMapping("/orderpayment")
    public BaseApiResponse orderPayment(@RequestBody GetSalesOrderRequest requestbody){
        requestbody.validate();

        orderService.orderPayment(requestbody.getOrderId());

        return new BaseApiResponse("Order Payment Succesfully");
    }
}
