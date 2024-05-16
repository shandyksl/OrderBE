package com.example.OrderBE.models.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orderID")
    private String orderId;

    @Column(name = "productID")
    private String productID;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "totalPrice")
    private BigDecimal totalPrice;

    @Column(name = "quantity")
    private int quantity;

}
