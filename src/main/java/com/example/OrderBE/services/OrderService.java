package com.example.OrderBE.services;

import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.SalesOrderRepostory;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RocketMQMessageListener(topic = "order-topic", consumerGroup = "order-consumer-group-wacao")
public class OrderService  implements RocketMQListener<SalesOrder> {

    private final RocketMQTemplate rocketMQTemplate;
    private final SalesOrderRepostory salesOrderRepository; // A


    public OrderService(RocketMQTemplate rocketMQTemplate, SalesOrderRepostory salesOrderRepository){
        this.rocketMQTemplate  = rocketMQTemplate;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Override
    @Transactional
    public void onMessage(SalesOrder salesInfo) {
        String responseMessage;
        try {
            salesOrderRepository.save(salesInfo);
            responseMessage = "Order processed successfully: " + salesInfo.getId();
        } catch (Exception e) {
            responseMessage = "Order processing failed: " + e.getMessage();
        }
        rocketMQTemplate.convertAndSend("response-topic", responseMessage);
    }
}
