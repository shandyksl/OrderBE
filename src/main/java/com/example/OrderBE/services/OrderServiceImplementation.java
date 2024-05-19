package com.example.OrderBE.services;
import com.example.OrderBE.aop.error.ErrorCode;
import com.example.OrderBE.aop.error.ErrorCodeException;
import com.example.OrderBE.models.entities.ProductInfo;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.ProductInfoRepository;
import com.example.OrderBE.repositories.SalesOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.MessageExt;
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


    @Autowired
    public OrderServiceImplementation(SalesOrderRepository salesOrderRepository, ProductInfoRepository productInfoRepository, RedisTemplate<String, Object> redisTemplate) {
        this.salesOrderRepository = salesOrderRepository;
        this.productInfoRepository = productInfoRepository;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void placeOrder(List<MessageExt> messageExts) throws ErrorCodeException {
        for (MessageExt messageExt : messageExts) {
            List<SalesOrder> salesOrders = null;
            try {
                salesOrders = convertMessageToSalesOrder(messageExt);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
    public void orderPayment(String orderId){
        List<SalesOrder> list = salesOrderRepository.findByOrderId(orderId);

        if(list.isEmpty()) {
            throw new ErrorCodeException(ErrorCode.ORDERID_NOT_EXIST);
        }

        for(SalesOrder salesOrder : list){
            salesOrder.setStatus(1);
        }
        salesOrderRepository.saveAll(list);
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
