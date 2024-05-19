package com.example.OrderBE.models.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ProductInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productId;

    private String productName;

    private int quantity;
}
