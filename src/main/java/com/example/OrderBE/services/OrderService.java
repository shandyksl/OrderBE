package com.example.OrderBE.services;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.SalesOrderRepostory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.mysql.cj.conf.PropertyKey.logger;

@Service
public class OrderService   {

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();


    public static volatile boolean running = true;


    private final SalesOrderRepostory salesOrderRepository; // A
    private DefaultLitePullConsumer litePullConsumer;
    private ScheduledExecutorService executorService;


    public OrderService(SalesOrderRepostory salesOrderRepository){
        this.salesOrderRepository = salesOrderRepository;
    }


    @PostConstruct
    public void init() throws MQClientException {
        litePullConsumer = new DefaultLitePullConsumer("order-consumer-group-wacao");
        litePullConsumer.subscribe("placeorder", "*");
        litePullConsumer.setPullBatchSize(20);
        litePullConsumer.start();
        logger.info("Consumer started and subscribed to topic 'placeorder'.");


        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::pollMessages, 0, 1, TimeUnit.SECONDS);
    }

    @Transactional
    public void pollMessages() {
        try {
            if (running) {
                logger.info("Polling for messages...");
                List<MessageExt> messageExts = litePullConsumer.poll();
                logger.info("Number of messages polled: " + messageExts.size());

                for (MessageExt messageExt : messageExts) {
                    List<SalesOrder> salesOrders = convertMessageToSalesOrder(messageExt);

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

        // Extract the message body as a byte array
        byte[] body = messageExt.getBody();

        String bodyString = new String(body, StandardCharsets.UTF_8);

        List<SalesOrder> salesOrders = objectMapper.readValue(bodyString, new TypeReference<List<SalesOrder>>() {});
        return salesOrders;
    }


    @PreDestroy
    public void shutdown() {
        running = false;
        if (litePullConsumer != null) {
            litePullConsumer.shutdown();
            logger.info("Consumer shut down.");

        }
        if (executorService != null) {
            executorService.shutdown();
            logger.info("Executor service shut down.");

        }
    }

}
