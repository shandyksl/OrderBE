package com.example.OrderBE.services;

import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.ProductInfo;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.ProductInfoRepository;
import com.example.OrderBE.repositories.SalesOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

@Service
public class OrderServiceImplementation {

    private static final Logger logger = Logger.getLogger(OrderServiceImplementation.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static volatile boolean running = true;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Setter
    private DefaultLitePullConsumer litePullConsumer;

    @Transactional
    public void pollMessages() throws ErrorCodeException {

        try {
            if (running) {
                logger.info("Polling for messages...");
                List<MessageExt> messageExts = litePullConsumer.poll();
                logger.info("Number of messages polled: " + messageExts.size());

                for (MessageExt messageExt : messageExts) {
                    List<SalesOrder> salesOrders = convertMessageToSalesOrder(messageExt);

                    for (SalesOrder salesOrder : salesOrders) {

                        // Deduct quantity from Redis
                        redisTemplate.opsForHash().increment(salesOrder.getProductId(), "quantity", -salesOrder.getQuantity());

                        // Deduct quantity from product table
                        ProductInfo product = productInfoRepository.findByProductId(salesOrder.getProductId());
                        product.setQuantity(product.getQuantity() - salesOrder.getQuantity());
                        productInfoRepository.save(product);
                    }
                    // save order
                    salesOrderRepository.saveAll(salesOrders);

                    for (SalesOrder salesOrder : salesOrders) {
                        logger.info("Saved sales order with ID: " + salesOrder.getOrderId());
                    }
                }
            }
        } catch (Exception e) {
            // Log the exception
            logger.severe("Error while polling messages: " + e.getMessage());
            e.printStackTrace();
        }
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
}
