package com.example.OrderBE.Initializer;

import com.example.OrderBE.services.OrderService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class OrderMessageReceiver {

    private static final Logger logger = Logger.getLogger(OrderMessageReceiver.class.getName());

    public static volatile boolean running = true;
    private final OrderService orderService;
    private DefaultLitePullConsumer litePullConsumer;
    private ScheduledExecutorService executorService;

    @Autowired
    public OrderMessageReceiver(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() throws MQClientException {
        litePullConsumer = new DefaultLitePullConsumer("order-consumer-group-wacao");
        litePullConsumer.subscribe("placeorder", "*");
        litePullConsumer.setPullBatchSize(20);
        litePullConsumer.start();
        logger.info(" OrderMessageReceiver started and subscribed to topic 'placeorder'.");
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::pollMessages, 0, 1, TimeUnit.SECONDS);
    }

    private void pollMessages() {
        try {
            if (running) {
                logger.info("OrderMessageReceiver polling for messages...");
                List<MessageExt> messageExts = litePullConsumer.poll();
                logger.info("Number of messages polled: " + messageExts.size());
                if (!messageExts.isEmpty()) {
                    orderService.placeOrder(messageExts);
                }
            }
        } catch (Exception e) {
            // Log the exception
            logger.severe("Error while polling messages: " + e.getMessage());
        }

    }

    @PreDestroy
    public void shutdown() {
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
