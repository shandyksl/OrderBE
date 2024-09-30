package com.example.OrderBE.services;
import com.example.OrderBE.aop.error.ErrorCode;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.ProductInfo;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.ProductInfoRepository;
import com.example.OrderBE.repositories.SalesOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

@Service
public class OrderServiceImplementation implements OrderService {

    private static final Logger logger = Logger.getLogger(OrderServiceImplementation.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SalesOrderRepository salesOrderRepository;

    private final ProductInfoRepository productInfoRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RocketMQTemplate rocketMQTemplate;


    @Autowired
    public OrderServiceImplementation(SalesOrderRepository salesOrderRepository, ProductInfoRepository productInfoRepository, RedisTemplate<String, Object> redisTemplate,RocketMQTemplate rocketMQTemplate) {
        this.salesOrderRepository = salesOrderRepository;
        this.productInfoRepository = productInfoRepository;
        this.redisTemplate = redisTemplate;
        this.rocketMQTemplate = rocketMQTemplate;
    }


    @Override
    @Transactional
    public void placeOrder(List<MessageExt> messageExts) {
        for (MessageExt messageExt : messageExts) {
            try {
                List<SalesOrder> salesOrders = convertMessageToSalesOrder(messageExt);
                processSalesOrders(salesOrders);
            } catch (IOException e) {
                logger.severe("Failed to convert message to sales order"+ e.getMessage());
                throw new RuntimeException("Error converting message to sales order: " + e.getMessage());
            }
        }
    }

    @Override
    public List<SalesOrder> getSalesOrderDetails(String orderId){
        List<SalesOrder> list = salesOrderRepository.findByOrderId(orderId);
        if(list.isEmpty()) {
            throw new ErrorCodeException(ErrorCode.ORDERID_NOT_EXIST);
        }else {
            return list;
        }
    }

    @Override
    @Transactional
    public void orderPayment(String orderId){
        List<SalesOrder> salesOrders = salesOrderRepository.findByOrderId(orderId);

        if(salesOrders.isEmpty()) {
            throw new ErrorCodeException(ErrorCode.ORDERID_NOT_EXIST);
        }

        updateOrderStatus(salesOrders, 1);
        saveOrdersAndSendMessage(salesOrders);
    }

    private void updateOrderStatus(List<SalesOrder> salesOrders, int status) {
        for (SalesOrder salesOrder : salesOrders) {
            salesOrder.setStatus(status);
        }
    }

    private void saveOrdersAndSendMessage(List<SalesOrder> salesOrders) {
        try {
            List<SalesOrder> savedOrders = salesOrderRepository.saveAll(salesOrders);

            if (!savedOrders.isEmpty()) {
                sendTransactionMessage("Order transaction successful");
            } else {
                sendTransactionMessage("Order transaction failed");
            }
        } catch (Exception e) {
            logger.severe("An error occurred while saving orders: " + e.getMessage());
            sendTransactionMessage("Order transaction failed");
        }
    }

    private void sendTransactionMessage(String message) {
        rocketMQTemplate.convertAndSend("transactionMessage", message);
    }


    private List<SalesOrder> convertMessageToSalesOrder(MessageExt messageExt) throws IOException {
        List<SalesOrder> salesOrders = new ArrayList<>();
        // Extract the message body as a byte array
        byte[] body = messageExt.getBody();

        String bodyString = new String(body, StandardCharsets.UTF_8);
        if (!bodyString.isEmpty()) {
            salesOrders = objectMapper.readValue(bodyString, new TypeReference<List<SalesOrder>>() {
            });
        }
        return salesOrders;
    }

    private void processSalesOrders(List<SalesOrder> salesOrders) {
        for (SalesOrder salesOrder : salesOrders) {
            // Deduct quantity from Redis
            redisTemplate.opsForHash().increment(salesOrder.getProductId(), "quantity", -salesOrder.getQuantity());

            // Deduct quantity from product table
            ProductInfo product = productInfoRepository.findByProductId(salesOrder.getProductId());
            product.setQuantity(product.getQuantity() - salesOrder.getQuantity());
            productInfoRepository.save(product);
        }
        // Save orders and log the operation
        salesOrderRepository.saveAll(salesOrders);
        for (SalesOrder salesOrder : salesOrders) {
            logger.info("Saved sales order with ID: " + salesOrder.getOrderId());
        }
    }
}
