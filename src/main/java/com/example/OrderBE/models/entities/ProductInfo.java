package com.example.OrderBE.models.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProductInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "productID")
    private String productID;
    @Column(name = "productName")
    private String productName;
    @Column(name = "quantity")
    private int quantity;

}
