package com.example.OrderBE.Initializer;

import com.example.OrderBE.services.OrderServiceImplementation;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class OrderServiceInitializer {

    private static final Logger logger = Logger.getLogger(OrderServiceInitializer.class.getName());

    private final OrderServiceImplementation orderService;
    private DefaultLitePullConsumer litePullConsumer;
    private ScheduledExecutorService executorService;

    @Autowired
    public OrderServiceInitializer(OrderServiceImplementation orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void init() throws MQClientException {
        litePullConsumer = new DefaultLitePullConsumer("order-consumer-group-wacao");
        litePullConsumer.subscribe("placeorder", "*");
        litePullConsumer.setPullBatchSize(20);
        litePullConsumer.start();
        logger.info("Consumer started and subscribed to topic 'placeorder'.");

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(orderService::pollMessages, 0, 1, TimeUnit.SECONDS);

        orderService.setLitePullConsumer(litePullConsumer);
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
