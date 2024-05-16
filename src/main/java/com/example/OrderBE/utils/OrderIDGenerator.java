package com.example.OrderBE.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderIDGenerator {

    public String generateUniqueOrderId() {
        return UUID.randomUUID().toString();
    }
}
