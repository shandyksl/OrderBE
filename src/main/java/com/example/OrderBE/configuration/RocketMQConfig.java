package com.example.OrderBE.configuration;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class RocketMQConfig {


    @Bean
    public RocketMQPushConsumerLifecycleListener customRocketMQPushConsumerLifecycleListener() {
        return new RocketMQPushConsumerLifecycleListener() {
            @Override
            public void prepareStart(DefaultMQPushConsumer consumer) {
                consumer.setInstanceName(UUID.randomUUID().toString());
            }
        };
    }




}
